package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.LoanParameters;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AgeService {

    private static final int EXPECTED_LIFE_SPAN = 75;
    private static final int TWELVE_MONTHS = 12;

    private int calculateAgeFromPersonalCode(String personalCode) {
        char centuryPrefix = personalCode.charAt(0);
        int yearPrefix = (centuryPrefix == '3' || centuryPrefix == '4') ? 1900 : 2000;

        int birthYear = yearPrefix + Integer.parseInt(personalCode.substring(1, 3));
        int birthMonth = Integer.parseInt(personalCode.substring(3, 5));
        int birthDay = Integer.parseInt(personalCode.substring(5, 7));

        LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public boolean isEligible(String personalCode) {
        int age = calculateAgeFromPersonalCode(personalCode);
        int maximumLoanPeriod = EXPECTED_LIFE_SPAN - (LoanParameters.MAXIMUM.getPeriodMonths() / TWELVE_MONTHS);
        if (age < 18) {
            return false;
        }
        return age <= maximumLoanPeriod;
    }

}
