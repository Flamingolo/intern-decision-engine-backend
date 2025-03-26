package ee.taltech.inbankbackend.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditModifier {

    SEGMENT_0(0, 2499, 0),
    SEGMENT_1(2500, 4999, 100),
    SEGMENT_2(5000, 7499, 300),
    SEGMENT_3(7500, Integer.MAX_VALUE, 1000);

    private final int min;
    private final int max;
    private final int modifier;

    public static int from(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));
        for (CreditModifier cm : values()) {
            if (segment >= cm.min && segment <= cm.max) {
                return cm.getModifier();
            }
        }
        throw new IllegalArgumentException("Invalid segment value: " + segment);
    }
}
