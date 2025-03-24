package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/// Current problem is that I can't temper system time so in the future, these tests will fail.
@ExtendWith(MockitoExtension.class)
class AgeServiceTest {

    @InjectMocks
    private AgeService ageService;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {

        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    @Test
    void testPersonalCode() {
        assertTrue(ageService.isEligible(debtorPersonalCode));
        assertTrue(ageService.isEligible(segment1PersonalCode));
        assertTrue(ageService.isEligible(segment2PersonalCode));
        assertFalse(ageService.isEligible(segment3PersonalCode));
    }

}

