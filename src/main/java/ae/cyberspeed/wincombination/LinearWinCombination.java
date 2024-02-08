package ae.cyberspeed.wincombination;

import ae.cyberspeed.service.Board;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.WinCombinationException;

import java.util.*;

/**
 * Represents a linear win combination within a game, where symbols need to align linearly
 * on the game board to constitute a win. This record encapsulates the details necessary
 * to identify and reward such combinations.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record LinearWinCombination(String name,
                                   double rewardMultiplier,
                                   String condition,
                                   String group,
                                   String[][] coveredAreas) implements WinCombination {

    /**
     * Applies the win condition to the game board, checking for linear alignment of symbols
     * and updating the applied win combinations map accordingly.
     *
     * @param board The game board to check for win conditions.
     * @param appliedWinCombinations A map of symbols to their associated win combinations and rewards.
     * @throws WinCombinationException if either the board or appliedWinCombinations map is null.
     * @throws IllegalStateException if the condition value is unexpected or not supported.
     */
    @Override
    public void applyWinCondition(Board board,
                                  Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>>
                                      appliedWinCombinations) {
        // Ensure that neither the board nor the map of applied win combinations is null.
        Validation.requireNonNull(board, new WinCombinationException("Board cannot be null"));
        Validation.requireNonNull(appliedWinCombinations,
            new WinCombinationException("Applied win combinations cannot be null"));

        // Process only if the condition specifies a linear alignment of symbols.
        if (condition.equals("linear_symbols")) {
            // Define a comparator for win combinations based on their reward multiplier, in descending order.
            Comparator<WinCombination> comparator = Comparator.comparingDouble(
                WinCombination::rewardMultiplier
            ).reversed();

            // List to hold symbols in the current line being checked.
            List<Symbol> linearSymbols = new ArrayList<>();
            // Set to track symbols already processed to avoid duplicate win condition applications.
            Set<Symbol> processedSymbols = new HashSet<>();

            // Iterate over each row of covered areas to check for linear win conditions.
            Arrays.stream(coveredAreas)
                .forEach(coveredAreasRow -> {
                    linearSymbols.clear(); // Clear the list for the new row.

                    // Convert each coordinate pair into row and column indices, then add the symbol at that
                    // location to linearSymbols.
                    Arrays.stream(coveredAreasRow)
                        .filter(Objects::nonNull) // Ignore null values, which represent empty coordinates.
                        .forEach(coordinates -> {
                            String[] parts = coordinates.split(":");
                            int row = Integer.parseInt(parts[0]);
                            int col = Integer.parseInt(parts[1]);

                            linearSymbols.add(board.getCellValue(row, col));
                        });

                    // Check if all symbols in linearSymbols are identical (excluding bonus symbols),
                    // ensuring at least one symbol is present and hasn't been processed yet.
                    if (linearSymbols.stream().distinct().limit(2).count() == 1 &&
                        linearSymbols.size() > 0 && !linearSymbols.get(0).isBonus() &&
                        !processedSymbols.contains(linearSymbols.get(0))) {
                        // If conditions are met, add this win combination to the map under the relevant
                        // symbol and group.
                        appliedWinCombinations
                            .computeIfAbsent(linearSymbols.get(0), k -> new HashMap<>())
                            .computeIfAbsent(
                                WinCombinationGroup.fromString(group), k -> new PriorityQueue<>(comparator)
                            ).offer(this);

                        // Mark the symbol as processed to prevent duplication.
                        processedSymbols.add(linearSymbols.get(0));
                    }
                });

        } else {
            // Throw an exception if the condition is not recognized.
            throw new IllegalStateException(String.format("Unexpected 'when' value: '%s'", condition));
        }
    }
}
