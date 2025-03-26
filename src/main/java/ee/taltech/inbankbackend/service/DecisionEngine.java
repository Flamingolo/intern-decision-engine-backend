package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.CreditModifier;
import ee.taltech.inbankbackend.config.LoanParameters;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.pojo.Loan;
import org.springframework.stereotype.Service;

@Service
public class DecisionEngine {

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final AgeService ageService;

    public DecisionEngine(AgeService ageService) {
        this.ageService = ageService;
    }

    public float creditScoreCalc(int creditModifier, int loanAmount, int loanPeriod) {
        return ((creditModifier / (float) loanPeriod) * loanAmount) / 10;
    }

    public Decision calculateApprovedLoan(String personalCode, Long requestedLoanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, InvalidAgeException {
        try {
            verifyInputs(personalCode, requestedLoanAmount, loanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        // Age validation
        if (!ageService.isEligible(personalCode)) {
            throw new InvalidAgeException("Invalid age");
        }

        // Credit validation
        int creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        // First try - maximum loan for requested period
        Loan firstLoanTry = getBiggestLoanByAmount(creditModifier, loanPeriod);
        if (canGetLoan(creditModifier, firstLoanTry)) {
            return new Decision(
                    Math.min(firstLoanTry.getAmount(), LoanParameters.MAXIMUM.getAmount()),
                    firstLoanTry.getPeriodMonths(),
                    null
            );
        }

        // Second try - longer periods
        int longestLoanPeriodMonths = getLongestLoanPeriodMonths(creditModifier, creditScoreCalc(creditModifier, requestedLoanAmount.intValue(), loanPeriod), firstLoanTry);
        Loan secondLoanTry = new Loan()
                .setPeriodMonths(longestLoanPeriodMonths)
                .setAmount(Math.toIntExact(requestedLoanAmount));
        if (canGetLoan(creditModifier, secondLoanTry)) {
            return new Decision(
                    Math.min(secondLoanTry.getAmount(), LoanParameters.MAXIMUM.getAmount()),
                    secondLoanTry.getPeriodMonths(),
                    null
            );
        }

//        int currentPeriod = loanPeriod;
//        Loan secondLoanTry = getBiggestLoanByAmount(creditModifier, loanPeriod);
//        while (isLessThanLongestPeriod(currentPeriod)) {
//            currentPeriod++;
//            maxLoan = getBiggestLoanForPeriod(creditModifier, currentPeriod, loanAmount.intValue());
//            if (canGetLoan(creditModifier, maxLoan)) {
//                return new Decision(
//                        Math.min(maxLoan.getAmount(), LoanParameters.MAXIMUM.getAmount()),
//                        currentPeriod,
//                        null
//                );
//            }
//        }

        // Third try - smaller amount with original period
        try {
            Loan adjustedLoan = getBiggestLoanForPeriod(creditModifier, loanPeriod, requestedLoanAmount.intValue());
            return new Decision(adjustedLoan.getAmount(), loanPeriod, null);
        } catch (NoValidLoanException e) {
            throw new NoValidLoanException("No valid loan found after all attempts");
        }
    }

    private boolean canGetLoan(int creditModifier, Loan loan) {
        float score = creditScoreCalc(creditModifier, loan.getAmount(), loan.getPeriodMonths());
        return score >= 0.1f;
    }

    private int getLongestLoanPeriodMonths(int creditModifier, float creditScore, Loan loan) {
        // LoanPeriod=(CreditScore×10×LoanAmount)÷CreditModifier
        return (int) ((creditScore * 10 * loan.getAmount()) / creditModifier);
    }

    private int getHighestValidLoanAmount(int creditModifier, int loanPeriodMonths) {
        return creditModifier * loanPeriodMonths;
    }

    private int getLongestLoanPeriod(int creditModifier, int loanPeriodMonths) {
        return creditModifier * loanPeriodMonths;
    }

    private Loan getBiggestLoanByAmount(int creditModifier, int loanPeriodMonths) {
        int maxLoanAmount = getHighestValidLoanAmount(creditModifier, loanPeriodMonths);
        while (isLessThanMinimumLoan(maxLoanAmount) && isLessThanLongestPeriod(loanPeriodMonths)) {
            loanPeriodMonths++;
            maxLoanAmount = getHighestValidLoanAmount(creditModifier, loanPeriodMonths);
        }

        return new Loan()
                .setAmount(maxLoanAmount)
                .setPeriodMonths(loanPeriodMonths);
    }

    private Loan getBiggestLoanForPeriod(int creditModifier, int loanPeriodMonths, int originalLoanAmount) throws NoValidLoanException {
        int maxLoanAmount = creditModifier * loanPeriodMonths;
        int approvedMax = Math.min(maxLoanAmount, LoanParameters.MAXIMUM.getAmount());
        int finalLoanAmount = Math.min(approvedMax, originalLoanAmount);

        if (finalLoanAmount < LoanParameters.MINIMUM.getAmount()) {
            throw new NoValidLoanException("No valid loan found!");
        }
//
//        float creditScore = canGetLoan(creditModifier, finalLoanAmount, loanPeriodMonths);
//        if (creditScore < 0.1f) {
//            throw new NoValidLoanException("No valid loan found!");
//        }

        return new Loan()
                .setAmount(finalLoanAmount)
                .setPeriodMonths(loanPeriodMonths);

    }

    private boolean isLessThanLongestPeriod(int loanPeriodMonths) {
        return loanPeriodMonths <= LoanParameters.MAXIMUM.getPeriodMonths();
    }

    private boolean isLessThanMinimumLoan(int maxLoanAmount) {
        return maxLoanAmount < LoanParameters.MINIMUM.getAmount();
    }

    private int getCreditModifier(String personalCode) {
        return CreditModifier.from(personalCode);
    }

    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(LoanParameters.MINIMUM.getAmount() <= loanAmount)
                || !(loanAmount <= LoanParameters.MAXIMUM.getAmount())) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(LoanParameters.MINIMUM.getPeriodMonths() <= loanPeriod)
                || !(loanPeriod <= LoanParameters.MAXIMUM.getPeriodMonths())) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
