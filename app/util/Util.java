package util;

public class Util {

    /**
     * Validates if an amount is a positive float (non-zero, not NaN nor infinite).
     * @param amount
     */
    public static void validateAmount(Float amount) {
        if (!isAmountPositive(amount))
            throw new IllegalArgumentException("Amount must be greater than zero.");
    }

    public static boolean isAmountPositive(Float amount) {
        return amount != null && amount > 0 && !amount.isNaN() && !amount.isInfinite();
    }

    public static boolean isAmountAtLeastZero(Float amount) {
        return amount != null && amount >= 0 && !amount.isNaN() && !amount.isInfinite();
    }
}
