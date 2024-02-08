package ae.cyberspeed.probability;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.ProbabilityException;

import java.util.Map;

/**
 * A probability mapping for standard symbols within a game configuration.
 * This record stores the row and column where the symbol probability is applicable
 * as well as a map of each standard symbol to its corresponding probability.
 * Standard symbols typically affect the gameplay in normal ways, different from bonus symbols.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record StandardSymbolProbability(int row, int column, Map<Symbol, Double> symbolsProbability)
    implements Probability {

    /**
     * Constructs a new StandardSymbolProbability instance with the given coordinates and probability mapping.
     *
     * @throws ProbabilityException If the provided symbol probabilities map is null, empty,
     *                              contains negative values, or if the provided coordinates are negative.
     */
    public StandardSymbolProbability {
        // Validates that the symbol probabilities map is correct.
        Validation.validateSymbolsProbability(symbolsProbability);
        // Checks that the provided coordinates are not negative.
        Validation.checkSymbolCoordinatesIsNegative(row, column);
    }

    /**
     * Checks if the symbol's probability matches the given board cell coordinates.
     *
     * @param row The row index of the cell.
     * @param column The column index of the cell.
     * @return true if the coordinates match the probability's applicable cell, false otherwise.
     */
    @Override
    public boolean coordinatesMatch(int row, int column) {
        // Returns true if the given coordinates match the record's row and column.
        return (this.row == row && this.column == column);
    }

    /**
     * Indicates that this is a standard symbol probability, not a bonus symbol probability.
     *
     * @return false, confirming that this probability is for a standard symbol.
     */
    @Override
    public boolean isBonus() {
        // This implementation is for standard symbols, so it always returns false.
        return false;
    }
}
