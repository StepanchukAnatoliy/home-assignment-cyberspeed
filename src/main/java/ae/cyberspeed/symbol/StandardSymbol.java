package ae.cyberspeed.symbol;

import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.SymbolException;

/**
 * Represents a standard symbol in the game that multiplies the player's reward based on its reward multiplier.
 * This symbol type is fundamental for calculating the base rewards in the game.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record StandardSymbol(String name, double rewardMultiplier) implements Symbol {

    /**
     * Constructs a StandardSymbol with validated name and reward multiplier.
     * Ensures the name is not blank and the reward multiplier is positive.
     *
     * @param name The name of the standard symbol.
     * @param rewardMultiplier The multiplier that this symbol applies to the reward.
     * @throws SymbolException If the name is blank or if the reward multiplier is negative or zero.
     */
    public StandardSymbol {
        // Validate the name and reward multiplier are appropriate.
        Validation.checkIsBlankString(name,
            new SymbolException("The name of a standard symbol must not be blank or null"));
        Validation.checkIsNegativeOrZero(rewardMultiplier,
            new SymbolException("The reward multiplier of a standard symbol cannot be negative or zero"));
    }

    /**
     * Calculates the reward for this symbol based on the partial amount.
     * Multiplies the partial amount by the symbol's reward multiplier.
     *
     * @param partialAmount The initial amount before applying the symbol's effect.
     * @return The modified reward amount after applying the symbol's effect.
     * @throws SymbolException If the calculated result is negative or zero.
     */
    @Override
    public double calculateReward(double partialAmount) {
        double result = partialAmount * rewardMultiplier;

        // Ensure the result of this calculation is valid.
        Validation.checkIsNegativeOrZero(result,
            new SymbolException("The result calculated by a standard symbol's reward method cannot be negative or zero."));

        return result;
    }

    /**
     * Indicates whether this symbol is considered a bonus.
     * For StandardSymbol, this method always returns false.
     *
     * @return false, indicating this symbol does not confer bonus effects beyond its reward multiplier.
     */
    @Override
    public boolean isBonus() {
        return false;
    }
}
