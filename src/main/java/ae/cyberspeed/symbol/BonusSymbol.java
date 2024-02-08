package ae.cyberspeed.symbol;

import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.SymbolException;

/**
 * Represents a bonus symbol in the game with specific effects on the game's outcome.
 * It can either multiply rewards, add a bonus amount, or have no impact (miss).
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record BonusSymbol(String name, double rewardMultiplier, String impact, double extra) implements Symbol {

    /**
     * Creates a new instance of BonusSymbol with validated parameters.
     *
     * @param name The name of the bonus symbol.
     * @param rewardMultiplier The multiplier to apply to the reward.
     * @param impact The type of impact the symbol has on rewards (e.g., "multiply_reward", "extra_bonus", "miss").
     * @param extra An additional bonus amount to be added to the reward.
     * @throws SymbolException If any parameter does not meet the validation criteria.
     */
    public BonusSymbol {
        // Validate input parameters.
        Validation.checkIsBlankString(name,
            new SymbolException("The name of a bonus symbol must not be blank or null"));
        Validation.checkIsBlankString(impact,
            new SymbolException("The impact of a bonus symbol must not be blank or null"));
        Validation.checkIsNegative(rewardMultiplier,
            new SymbolException("The reward multiplier of a bonus symbol cannot be negative"));
        Validation.checkIsNegative(extra,
            new SymbolException("The extra of a bonus symbol cannot be negative"));

        // Ensure that both the reward multiplier and extra are not simultaneously positive unless the impact is "miss".
        if (!impact.equals("miss")) {
            Validation.checkTwoValuesPositive(rewardMultiplier, extra,
                new SymbolException("Both the reward multiplier and the extra of a bonus symbol cannot be positive " +
                                    "simultaneously"));
        }
    }

    /**
     * Calculates the modified reward based on the symbol's impact.
     *
     * @param partialAmount The initial amount before applying the symbol's effect.
     * @return The modified reward amount after applying the symbol's effect.
     * @throws SymbolException If the calculated result is negative or zero.
     */
    @Override
    public double calculateReward(double partialAmount) {
        double result;

        // Apply the symbol's impact based on its type.
        switch (impact) {
            // Multiply the reward by the multiplier.
            case "multiply_reward" -> result = partialAmount * rewardMultiplier;
            // Add the extra amount to the reward.
            case "extra_bonus" -> result = partialAmount + extra;
            // No change to the reward.
            case "miss" -> result = partialAmount;
            // Handle unexpected impact values.
            default -> throw new SymbolException(String.format("Unexpected bonus symbol impact: '%s'", impact));
        }

        // Validate the final result.
        Validation.checkIsNegativeOrZero(result,
            new SymbolException("The result calculated by a bonus symbol's reward method cannot be negative or zero."));

        return result;
    }

    /**
     * Determines if this symbol is considered a bonus in terms of game mechanics.
     *
     * @return true if the symbol has an impact other than "miss", false otherwise.
     */
    @Override
    public boolean isBonus() {
        return !impact.equals("miss");
    }
}
