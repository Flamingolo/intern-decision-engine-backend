package ee.taltech.inbankbackend.factory;

import ee.taltech.inbankbackend.pojo.Loan;
import ee.taltech.inbankbackend.service.Decision;
import org.springframework.stereotype.Service;

@Service
public class DecisionFactory {

    public Decision createDecision(Loan loan, String errorMessage) {
        if (loan == null) {
            return new Decision(null, null, errorMessage);
        }
        return new Decision(loan.getAmount(), loan.getPeriodMonths(), errorMessage);
    }

}
