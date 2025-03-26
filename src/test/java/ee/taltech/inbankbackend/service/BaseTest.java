package ee.taltech.inbankbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Case 1 - Give person the biggest loan amount for the period asked.
 * Case 2 - Give person the requested loan amount but increase the period.
 * Case 3 - Give person the biggest loan amount for the requested period.
 */

@SpringBootTest
public abstract class BaseTest {

    protected String debtorPersonalCode;
    protected String segment1PersonalCode;
    protected String segment2PersonalCode;
    protected String segment3PersonalCode;


    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    @Autowired
    protected DecisionEngine decisionEngine;

}

