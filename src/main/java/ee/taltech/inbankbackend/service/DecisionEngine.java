package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.CreditModifier;
import ee.taltech.inbankbackend.config.LoanParameters;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.factory.DecisionFactory;
import ee.taltech.inbankbackend.pojo.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DecisionEngine {

    private static final float LOWEST_CREDIT_SCORE = 0.1f;

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final DecisionFactory decisionFactory;
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

        int highestLoanAmount = getHighestLoanAmount(
                creditModifier,
                requestedLoanPeriod,
                requestedLoanAmount.intValue()
        );
        Loan biggestAmountForRequestedPeriod = new Loan()
                .setAmount(highestLoanAmount)
                .setPeriodMonths(requestedLoanPeriod)
                .setCreditModifier(creditModifier);

        if (isEligibleForLoan(biggestAmountForRequestedPeriod) && biggestAmountForRequestedPeriod.getAmount() >= requestedLoanAmount) {
            return decisionFactory.createDecision(biggestAmountForRequestedPeriod, null);
        }

        int longestPeriodForRequestedAmount = getLongestLoanPeriodMonths(
                creditModifier,
                requestedLoanAmount.intValue()
        );
        Loan requestedLoanLongestPeriod = new Loan()
                .setPeriodMonths(longestPeriodForRequestedAmount)
                .setAmount(Math.toIntExact(requestedLoanAmount))
                .setCreditModifier(creditModifier);
        if (isEligibleForLoan(requestedLoanLongestPeriod)) {
            return decisionFactory.createDecision(requestedLoanLongestPeriod, null);

        }

        Loan highestAmountForRequestedPeriod = new Loan()
                .setPeriodMonths(requestedLoanPeriod)
                .setAmount(Math.toIntExact(biggestAmountForRequestedPeriod.getAmount()))
                .setCreditModifier(creditModifier);
        if (isEligibleForLoan(highestAmountForRequestedPeriod)) {
            return decisionFactory.createDecision(highestAmountForRequestedPeriod, null);
        }

        return decisionFactory.createDecision(null,"No valid loan found after all attempts");
    }

    private float getCreditScore(int creditModifier, int loanAmount, int loanPeriod) {
        return ((creditModifier / (float) loanAmount) * loanPeriod) / 10;
    }

    private int getLongestLoanPeriodMonths(int creditModifier, int loanAmount) {
        return (int) Math.ceil((LOWEST_CREDIT_SCORE * 10.0 * loanAmount) / creditModifier);
    }

    private int getHighestLoanAmount(int creditModifier, int loanPeriodMonths, int requestedLoanAmount) {
        int calculatedAmount = (int) ((creditModifier * loanPeriodMonths) / (LOWEST_CREDIT_SCORE * 10));
        if (calculatedAmount <= requestedLoanAmount) {
            return calculatedAmount;
        }
        return Math.max(calculatedAmount, requestedLoanAmount);
    }

    private boolean isEligibleForLoan(Loan loan) {
        if (loan.getPeriodMonths() > LoanParameters.MAXIMUM.getPeriodMonths()) {
            return false;
        }
        float score = getCreditScore(loan.getCreditModifier(), loan.getAmount(), loan.getPeriodMonths());
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
