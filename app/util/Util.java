package util;

public class Util {

    /**
     * Validates if an amount is a positive float (non-zero, not NaN nor infinite).
     * @param amount
     */
    public static void validateAmount(Float amount) {
        if (amount <= 0 || amount.isNaN() || amount.isInfinite())
            throw new IllegalArgumentException("Amount must be greater than zero.");
    }
}
