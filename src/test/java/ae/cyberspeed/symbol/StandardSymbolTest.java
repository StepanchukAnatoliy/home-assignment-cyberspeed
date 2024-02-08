package ae.cyberspeed.symbol;

import ae.cyberspeed.util.Validation;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

/**
 * Unit tests for the {@link StandardSymbol} class within the ae.cyberspeed.symbol package.
 * Tests cover the behavior of the calculateReward method and the isBonus method,
 * ensuring they function as expected for standard symbols.
 * <p>
 * The {@code calculateRewardShouldReturnCorrectResult} test verifies that rewards are calculated correctly
 * based on the symbol's multiplier. The {@code isBonusShouldAlwaysReturnFalse} test ensures that
 * standard symbols are not identified as bonus symbols.
 * </p>
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class StandardSymbolTest {
    /**
     * Verifies that the {@link StandardSymbol#calculateReward(double)} method returns the correct result
     * by multiplying the input value by the symbol's reward multiplier.
     * <p>
     * This test ensures that the calculation aligns with expected behavior, specifically that
     * the reward is accurately calculated using the symbol's defined multiplier.
     * </p>
     */
    @Test
    public void calculateRewardShouldReturnCorrectResult() {
        // Setup mock static validation to ensure that input validations are being called
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Initialize the symbol with a multiplier
            StandardSymbol symbol = new StandardSymbol("Symbol", 2.0);
            // Calculate reward based on a betting amount
            double result = symbol.calculateReward(50);

            // Assert that the calculated reward is as expected
            assertEquals(100.0, result, "Reward should be calculated correctly by multiplying with rewardMultiplier");

            // Verify that validation checks for string and double values were performed
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()));
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()), times(2));
        }
    }

    /**
     * Tests that {@link StandardSymbol#isBonus()} always returns false for standard symbols.
     * <p>
     * This test confirms the expected behavior that standard symbols are not considered bonus symbols,
     * which is critical for correctly identifying symbol types in game logic.
     * </p>
     */
    @Test
    public void isBonusShouldAlwaysReturnFalse() {
        // Setup mock static validation to ensure input validations are being called
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Initialize the symbol
            StandardSymbol symbol = new StandardSymbol("Symbol", 2.0);

            // Assert that the symbol is not identified as a bonus symbol
            assertFalse(symbol.isBonus(), "isBonus should always return false for StandardSymbol");

            // Verify that validation checks for string and double values were performed
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()));
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
        }
    }
}

