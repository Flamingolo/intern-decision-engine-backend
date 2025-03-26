package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.LoanParameters;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.factory.DecisionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Case 1 - Give person the biggest loan amount for the period asked.
 * Case 2 - Give person the requested loan amount but increase the period.
 * Case 3 - Give person the biggest loan amount for the requested period.
 */

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest extends BaseTest {

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void TestPerson1Case1() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment1PersonalCode, 2000L, 48);
        assertEquals(4800, decision.getLoanAmount());
        assertEquals(48, decision.getLoanPeriod());
    }


    @Test
    void testPerson1Case2() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(4000, decision.getLoanAmount());
        assertEquals(41, decision.getLoanPeriod());
    }

    @Test
    void testPerson1Case3() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment1PersonalCode, 10000L, 48);
        assertEquals(4800, decision.getLoanAmount());
        assertEquals(48, decision.getLoanPeriod());
    }

    @Test
    void testPerson2Case1() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment2PersonalCode, 3000L, 24);
        assertEquals(7200, decision.getLoanAmount());
        assertEquals(24, decision.getLoanPeriod());
    }

    @Test
    void testPerson2Case2() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(4000, decision.getLoanAmount());
        assertEquals(14, decision.getLoanPeriod());
    }

    @Test
    void testPerson3InvalidAge() {
        assertThrows(InvalidAgeException.class,
                () -> decisionEngine.calculateLoan(segment3PersonalCode, 4000L, 12));
    }


    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "1234567890187987897";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateLoan(invalidPersonalCode, 4000L, 12));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = LoanParameters.MINIMUM.getAmount() - 1L;
        Long tooHighLoanAmount = LoanParameters.MAXIMUM.getAmount() + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateLoan(segment1PersonalCode, tooLowLoanAmount, 12));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateLoan(segment1PersonalCode, tooHighLoanAmount, 12));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = LoanParameters.MINIMUM.getPeriodMonths() - 1;
        int tooLongLoanPeriod = LoanParameters.MAXIMUM.getPeriodMonths() + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod));
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidAgeException {
        Decision decision = decisionEngine.calculateLoan(segment2PersonalCode, 2000L, 12);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }


    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateLoan(debtorPersonalCode, 10000L, 60));
    }

}

