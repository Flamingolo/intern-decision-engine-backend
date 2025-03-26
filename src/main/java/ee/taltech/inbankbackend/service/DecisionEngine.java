package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.CreditModifier;
import ee.taltech.inbankbackend.config.LoanParameters;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.pojo.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DecisionEngine {

    private static final float LOWEST_CREDIT_SCORE = 0.1f;

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final AgeService ageService;

    public Decision calculateLoan(String personalCode, Long requestedLoanAmount, int requestedLoanPeriod) throws
            InvalidPersonalCodeException,
            InvalidLoanAmountException,
            InvalidLoanPeriodException,
            NoValidLoanException,
            InvalidAgeException {
        try {
            verifyInputs(personalCode, requestedLoanAmount, requestedLoanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        if (!ageService.isEligible(personalCode)) {
            throw new InvalidAgeException("Invalid age");
        }

        int creditModifier = CreditModifier.from(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        Loan biggestAmountForRequestedPeriod = getBiggestLoan(creditModifier, requestedLoanPeriod);
        if (canGetLoan(biggestAmountForRequestedPeriod, creditModifier) && biggestAmountForRequestedPeriod.getAmount() >= requestedLoanAmount) {
            return new Decision(
                    (int) Math.max(biggestAmountForRequestedPeriod.getAmount(), requestedLoanAmount),
                    biggestAmountForRequestedPeriod.getPeriodMonths(),
                    null
            );
        }

        int longestPeriodForRequestedAmount = getLongestLoanPeriodMonths(
                creditModifier,
                requestedLoanAmount.intValue()
        );
        Loan requestedLoanLongestPeriod = new Loan()
                .setPeriodMonths(longestPeriodForRequestedAmount)
                .setAmount(Math.toIntExact(requestedLoanAmount));
        if (canGetLoan(requestedLoanLongestPeriod, creditModifier)) {
            return new Decision(
                    Math.min(requestedLoanLongestPeriod.getAmount(), LoanParameters.MAXIMUM.getAmount()),
                    requestedLoanLongestPeriod.getPeriodMonths(),
                    null
            );
        }

        Loan highestAmountForRequestedPeriod = new Loan()
                .setPeriodMonths(requestedLoanPeriod)
                .setAmount(Math.toIntExact(biggestAmountForRequestedPeriod.getAmount()));
        if (canGetLoan(highestAmountForRequestedPeriod, creditModifier)) {
            return new Decision(
                    highestAmountForRequestedPeriod.getAmount(),
                    highestAmountForRequestedPeriod.getPeriodMonths(),
                    null
            );
        }

        return new Decision(null, null, "No valid loan found after all attempts");
    }

    private float getCreditScore(int creditModifier, int loanAmount, int loanPeriod) {
        return ((creditModifier / (float) loanAmount) * loanPeriod) / 10;
    }

    private int getLongestLoanPeriodMonths(int creditModifier, int loanAmount) {
        return (int) Math.ceil((LOWEST_CREDIT_SCORE * 10.0 * loanAmount) / creditModifier);
    }

    private int getHighestLoanAmount(int creditModifier, int loanPeriodMonths) {
        return (int) ((creditModifier * loanPeriodMonths) / (LOWEST_CREDIT_SCORE * 10));
    }

    private Loan getBiggestLoan(int creditModifier, int loanPeriodMonths) {
        int highestLoanAmount = getHighestLoanAmount(
                creditModifier,
                loanPeriodMonths
        );
        return new Loan()
                .setAmount(highestLoanAmount)
                .setPeriodMonths(loanPeriodMonths);
    }

    private boolean canGetLoan(Loan loan, int creditModifier) {
        if (loan.getPeriodMonths() > LoanParameters.MAXIMUM.getPeriodMonths()) {
            return false;
        }
        float score = getCreditScore(creditModifier, loan.getAmount(), loan.getPeriodMonths());
        return score >= LOWEST_CREDIT_SCORE;
    }

    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod) throws
            InvalidPersonalCodeException,
            InvalidLoanAmountException,
            InvalidLoanPeriodException {
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
