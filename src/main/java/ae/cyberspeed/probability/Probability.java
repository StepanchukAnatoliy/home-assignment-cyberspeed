package ae.cyberspeed.probability;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.error.ProbabilityException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Defines the behavior of probabilities associated with symbols in a game.
 * This interface allows the definition of the probability of symbols appearing on the board,
 * checking if a symbol matches certain coordinates, and if a symbol is considered a bonus.
 * Implementations of this interface should provide the symbol probability mapping.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public interface Probability {
    /**
     * Defines a symbol based on a random value and the probabilities of each symbol.
     *
     * @param random The random generator to use for selecting a symbol.
     * @return The selected symbol based on the defined probabilities.
     * @throws ProbabilityException If the symbols probability map is empty.
     */
    default Symbol defineSymbol(Random random) {
        double randomValue = random.nextDouble(100) + 1;
        double totalSum = symbolsProbability().values().stream().mapToDouble(Double::doubleValue).sum();

        // To keep track of the cumulative percentages sum
        final double[] cumulativeSum = {0};

        // Stream through the symbol probability entries and find the first one that exceeds the random value
        Optional<Symbol> result = symbolsProbability().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                // Calculate cumulative sum to turn stream of original Symbol probabilities to correct
                // cumulative percentages:
                // 'A' - 1       'A' - 4.76      'A' - 4.76,
                // 'B' - 2       'B' - 9.55      'B' - 14.31,
                // 'C' - 3       'C' - 14.28     'C' - 28.59,
                // 'D' - 4       'D' - 19.04     'D' - 47.63,
                // 'E' - 5       'E' - 23.80     'E' - 71.43,
                // 'F' - 6       'F' - 28.57     'F' - 100.00
                // Total - 21
                entry -> cumulativeSum[0] += 100 * entry.getValue() / totalSum,
                (e1, e2) -> e1, // In case of duplicates, keep the first
                LinkedHashMap::new // Maintain insertion order
            )).entrySet().stream()
            .filter(entry -> Double.compare(entry.getValue(), randomValue) > 0)
            .map(Map.Entry::getKey)
            .findFirst();

        // Return the selected symbol or find the symbol with the maximum probability if none matched
        return result.orElseGet(() -> symbolsProbability().entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> new ProbabilityException("Symbols probability map is empty!"))
            .getKey()
        );
    }

    /**
     * Checks if a symbol is applicable to specific coordinates on the board.
     *
     * @param row The row index of the board cell.
     * @param column The column index of the board cell.
     * @return true if the symbol is applicable, false otherwise.
     */
    boolean coordinatesMatch(int row, int column);

    /**
     * Determines if the symbol associated with this probability instance is a bonus symbol.
     *
     * @return true if it is a bonus symbol, false otherwise.
     */
    boolean isBonus();

    /**
     * Provides the mapping of symbols to their corresponding probabilities.
     *
     * @return A map where the key is a symbol and the value is its probability.
     */
    Map<Symbol, Double> symbolsProbability();
}


