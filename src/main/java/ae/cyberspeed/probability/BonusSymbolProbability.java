package ae.cyberspeed.probability;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.ProbabilityException;

import java.util.Map;

/**
 * Represents the probability mapping for bonus symbols within a game configuration.
 * Bonus symbols typically affect the gameplay in special ways, different from standard symbols.
 * This record stores a mapping of each bonus symbol to its corresponding probability.
 * All bonus symbols are considered to match any board cell coordinates for the purposes of gameplay.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record BonusSymbolProbability(Map<Symbol, Double> symbolsProbability) implements Probability {

    /**
     * Constructs a new BonusSymbolProbability instance with the given probability mapping.
     *
     * @throws ProbabilityException If the provided symbol probabilities map is null, empty, or contains negative values.
     */
    public BonusSymbolProbability {
        // Validates that the symbol probabilities are not null, empty, and do not contain negative values.
        Validation.validateSymbolsProbability(symbolsProbability);
    }

    /**
     * Always returns true as bonus symbols are considered to match any coordinates.
     *
     * @param row The row index of the cell.
     * @param column The column index of the cell.
     * @return true, indicating that bonus symbols are universally applicable.
     */
    @Override
    public boolean coordinatesMatch(int row, int column) {
        // Bonus symbols are not tied to specific coordinates, so this always returns true.
        return true;
    }

    /**
     * Always returns true as this is a bonus symbol probability.
     *
     * @return true, confirming that this probability is for a bonus symbol.
     */
    @Override
    public boolean isBonus() {
        // This implementation is specifically for bonus symbols, so it always returns true.
        return true;
    }
}
