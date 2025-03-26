package ee.taltech.inbankbackend.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Loan {

    private Integer amount;
    private Integer periodMonths;
    private int creditModifier;

}
