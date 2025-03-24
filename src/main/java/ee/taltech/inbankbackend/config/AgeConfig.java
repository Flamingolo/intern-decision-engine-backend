package ee.taltech.inbankbackend.config;

import java.util.Map;

public class AgeConfig {
    private static final Map<String, Integer> EXPECTED_LIFE_SPAN = Map.of(
            "EE", 78,
            "LV", 75,
            "LT", 76
    );
    private static final int DEFAULT_VALUE = 75;

    public static int getExpectedLifeSpan(String countryCode) {
        return EXPECTED_LIFE_SPAN.getOrDefault(countryCode, DEFAULT_VALUE);
    }
}

// This is useless, but I will keep it for now
