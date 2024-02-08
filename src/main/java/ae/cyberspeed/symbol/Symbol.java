package ae.cyberspeed.symbol;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the contract for symbols used in the game. Symbols can affect the game's outcome by modifying
 * the rewards based on specific rules. This interface also allows symbols to be compared, ensuring they
 * can be sorted or managed in collections that require ordering.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public interface Symbol extends Comparable<Symbol> {

    /**
     * Calculates the reward modification for this symbol based on a given partial amount.
     * This method must be implemented to define how each symbol affects the game's reward.
     *
     * @param partialAmount The initial amount before applying the symbol's effect.
     * @return The modified amount after applying the symbol's effect.
     */
    double calculateReward(double partialAmount);

    /**
     * Provides the name of the symbol. This is used for identification and serialization purposes.
     *
     * @return The name of the symbol.
     */
    @JsonValue
    String name();

    /**
     * Returns the reward multiplier associated with this symbol. This value is used in reward calculation.
     *
     * @return The reward multiplier for the symbol.
     */
    double rewardMultiplier();

    /**
     * Indicates whether this symbol is a bonus symbol. Bonus symbols might have special effects or rules
     * different from standard symbols.
     *
     * @return true if this symbol is a bonus symbol, false otherwise.
     */
    boolean isBonus();

    /**
     * Compares this symbol with another symbol to determine their ordering. The comparison is based on the
     * alphabetical order of their names.
     *
     * @param other The other symbol to compare with.
     * @return A negative integer, zero, or a positive integer as this symbol is less than, equal to, or greater
     *         than the specified symbol.
     */
    default int compareTo(Symbol other) {
        // Compare names alphabetically for consistent ordering.
        return this.name().compareTo(other.name());
    }
}
