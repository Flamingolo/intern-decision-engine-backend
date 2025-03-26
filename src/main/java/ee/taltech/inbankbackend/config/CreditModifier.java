package ee.taltech.inbankbackend.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditModifier {

    SEGMENT_0(0L, 2499L, 0L),
    SEGMENT_1(2500L, 4999L, 100L),
    SEGMENT_2(5000L, 7499L, 300L),
    SEGMENT_3(7500L, Long.MAX_VALUE, 1000L);

    private final long min;
    private final long max;
    private final long modifier;

    public static long from(String personalCode) {
        long segment = Long.parseLong(personalCode.substring(personalCode.length() - 4));
        for (CreditModifier cm : values()) {
            if (segment >= cm.min && segment <= cm.max) {
                return cm.getModifier();
            }
        }
        throw new IllegalArgumentException("Invalid segment value: " + segment);
    }
}