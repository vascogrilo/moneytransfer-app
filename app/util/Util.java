package util;

import org.apache.commons.lang3.StringUtils;

public class Util {

    /**
     * Validates if a string is not null nor empty.
     * @param input The string.
     */
    public static void validateString(String input){
        if (StringUtils.isEmpty(input))
            throw new IllegalArgumentException("String must be not null and non-empty.");
    }

    /**
     * Validates if an amount is a positive float (non-zero, not NaN nor infinite).
     * @param amount
     */
    public static void validateAmount(Float amount) {
        if (amount <= 0 || amount.isNaN() || amount.isInfinite())
            throw new IllegalArgumentException("Amount must be greater than zero.");
    }
}
