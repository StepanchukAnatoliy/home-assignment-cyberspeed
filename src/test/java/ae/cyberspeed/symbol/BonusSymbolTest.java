package ae.cyberspeed.symbol;

import ae.cyberspeed.util.Validation;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Tests for the BonusSymbol class focusing on reward calculation and bonus identification.
 * Verifies the correct calculation of rewards based on the symbol's impact type and ensures
 * the correct identification of a symbol as a bonus based on its impact.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class BonusSymbolTest {
    /**
     * Test the reward calculation with 'multiply_reward' impact.
     * Ensures the calculateReward method correctly multiplies the base reward
     * by the symbol's multiplier.
     */
    @Test
    public void calculateRewardShouldMultiply() {
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Create a BonusSymbol with a multiplier effect
            BonusSymbol bonusSymbol = new BonusSymbol("Bonus", 2.0, "multiply_reward", 0.0);
            double result = bonusSymbol.calculateReward(50);

            // Assert the reward is doubled as expected
            assertEquals(100.0, result, "Reward should be correctly multiplied");

            // Verify validation checks were performed as expected
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()), times(2));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()), times(2));
            mockValidation.verify(() -> Validation.checkTwoValuesPositive(anyDouble(), anyDouble(), any()));
        }
    }

    /**
     * Test the reward calculation with 'extra_bonus' impact.
     * Verifies that the calculateReward method correctly adds an extra bonus amount
     * to the base reward.
     */
    @Test
    public void calculateRewardShouldAddExtra() {
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Create a BonusSymbol with an extra bonus effect
            BonusSymbol bonusSymbol = new BonusSymbol("Bonus", 0.0, "extra_bonus", 20.0);
            double result = bonusSymbol.calculateReward(50);

            // Assert the reward includes the extra bonus as expected
            assertEquals(70.0, result, "Extra bonus should be correctly added");

            // Verify validation checks were performed
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()), times(2));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()), times(2));
            mockValidation.verify(() -> Validation.checkTwoValuesPositive(anyDouble(), anyDouble(), any()));
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
        }
    }

    /**
     * Test the reward calculation with 'miss' impact.
     * Ensures that the calculateReward method returns the base reward amount without any modifications.
     */
    @Test
    public void calculateRewardShouldReturnPartialForMiss() {
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Create a BonusSymbol with a 'miss' effect
            BonusSymbol bonusSymbol = new BonusSymbol("Bonus", 0.0, "miss", 0.0);
            double result = bonusSymbol.calculateReward(50);

            // Assert the reward remains unchanged as expected for 'miss' impact
            assertEquals(50.0, result, "Reward should be the partial amount for 'miss' impact");

            // Verify only relevant validation checks were performed
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()), times(2));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()), times(2));
            mockValidation.verify(() -> Validation.checkTwoValuesPositive(anyDouble(), anyDouble(), any()), never());
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
        }
    }

    /**
     * Test the isBonus method for a symbol with 'miss' impact.
     * Verifies that the method correctly identifies the symbol as not a bonus for 'miss' impact.
     */
    @Test
    public void isBonusShouldReturnFalseForMiss() {
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            // Create a BonusSymbol with a 'miss' effect
            BonusSymbol bonusSymbol = new BonusSymbol("Bonus", 0.0, "miss", 0.0);

            // Assert that the symbol is not considered a bonus
            assertFalse(bonusSymbol.isBonus(), "isBonus should return false for 'miss' impact");

            // Verify that unnecessary validation checks were not performed
            mockValidation.verify(() -> Validation.checkIsBlankString(anyString(), any()), times(2));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()), times(2));
            mockValidation.verify(() -> Validation.checkTwoValuesPositive(anyDouble(), anyDouble(), any()), never());
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()), never());
        }
    }
}

