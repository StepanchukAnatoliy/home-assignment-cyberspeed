package ae.cyberspeed.wincombination;

import ae.cyberspeed.service.Board;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.WinCombinationException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a win combination that requires multiple instances of the same symbol to appear
 * on the game board. This record encapsulates details such as the name, reward multiplier,
 * condition for winning, the group it belongs to, and the count of symbols required.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public record MultiTimesWinCombination(String name,
                                       double rewardMultiplier,
                                       String condition,
                                       String group,
                                       int count) implements WinCombination {

    /**
     * Applies this win condition to the game board, evaluating if the required number of same symbols
     * are present and updates the applied win combinations map accordingly.
     *
     * @param board The game board to evaluate for win conditions.
     * @param appliedWinCombinations A map to update with the win combinations applied.
     * @throws WinCombinationException if the board or appliedWinCombinations map is null, or if any symbol in the
     * win combinations is null.
     * @throws IllegalStateException if the condition specified does not match the expected "same_symbols" value.
     */
    @Override
    public void applyWinCondition(Board board,
                                  Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>>
                                      appliedWinCombinations) {
        // Validate non-null parameters.
        Validation.requireNonNull(board, new WinCombinationException("Board cannot be null"));
        Validation.requireNonNull(appliedWinCombinations,
            new WinCombinationException("Applied win combinations cannot be null"));

        // Process only if the condition is to check for the same symbols.
        if (condition.equals("same_symbols")) {
            // Define a comparator to sort win combinations by their reward multiplier in descending order.
            Comparator<WinCombination> comparator = Comparator.comparingDouble(
                WinCombination::rewardMultiplier
            ).reversed();

            // Flatten the board's matrix into a stream of symbols, ensuring each symbol is non-null.
            Arrays.stream(board.getMatrixBoard())
                .flatMap(Arrays::stream)
                // Ensure symbols are not null to prevent null pointer exceptions.
                .peek(s -> Validation.requireNonNull(s,
                    new WinCombinationException("Symbols inside applied win combinations must not be null"))
                )
                // Group symbols by their identity, counting occurrences, to find eligible win combinations.
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                // Filter for symbols that are not bonus and meet the required count for this win condition.
                .filter(entry -> !entry.getKey().isBonus() && entry.getValue() >= count)
                // For each eligible symbol, add this win combination to the applied win combinations map.
                .forEach(entry -> appliedWinCombinations
                    .computeIfAbsent(entry.getKey(), k -> new HashMap<>())
                    .computeIfAbsent(
                        WinCombinationGroup.fromString(group), k -> new PriorityQueue<>(comparator)
                    ).offer(this)
                );
        } else {
            // If the condition does not match "same_symbols", throw an exception indicating unexpected condition value.
            throw new IllegalStateException(String.format("Unexpected 'when' value: '%s'", condition));
        }
    }
}
