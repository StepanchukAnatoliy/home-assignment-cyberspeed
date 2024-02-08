package ae.cyberspeed.wincombination;

import ae.cyberspeed.service.Board;
import ae.cyberspeed.symbol.Symbol;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Queue;

/**
 * Defines the contract for win combinations in the game. Implementations of this interface
 * must provide mechanisms to apply win conditions to a game board and to retrieve the reward
 * multiplier and name associated with the win combination.
 *
 * Implementations can vary based on the specific rules for winning (e.g., linear combinations,
 * multi-symbol combinations) and are compared based on their names alphabetically.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public interface WinCombination extends Comparable<WinCombination> {

    /**
     * Applies the win condition defined by this win combination to the provided game board.
     * It checks if the win condition is met and updates the provided map of applied win combinations
     * accordingly.
     *
     * @param board The game board to check for win conditions.
     * @param appliedWinCombinations A map containing symbols and their corresponding win combinations
     *                               that have already been applied. This method updates the map if
     *                               the win condition is met.
     */
    void applyWinCondition(Board board,
                           Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations);

    /**
     * Retrieves the reward multiplier for this win combination.
     *
     * @return The reward multiplier.
     */
    double rewardMultiplier();

    /**
     * Returns the name of the win combination. The name is used for identification and comparison purposes.
     *
     * @return The name of the win combination.
     */
    @JsonValue
    String name();

    /**
     * Compares this win combination with another based on their names alphabetically.
     * This default implementation is used for sorting and ordering win combinations by name.
     *
     * @param other The other win combination to compare to.
     * @return A negative integer, zero, or a positive integer as this name is less than,
     *         equal to, or greater than the specified name, respectively.
     */
    default int compareTo(WinCombination other) {
        // Compare names alphabetically to enforce a consistent ordering of win combinations.
        return this.name().compareTo(other.name());
    }
}
