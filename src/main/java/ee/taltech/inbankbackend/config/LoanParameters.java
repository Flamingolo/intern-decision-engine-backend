package ee.taltech.inbankbackend.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoanParameters {

    MINIMUM(2000, 12),
    MAXIMUM(10000, 60);

    private final int amount;
    private final int periodMonths;

}
