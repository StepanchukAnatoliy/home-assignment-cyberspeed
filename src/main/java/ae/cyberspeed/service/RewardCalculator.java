package ae.cyberspeed.service;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.RewardCalculatorException;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Provides functionality to calculate the total reward based on applied win combinations.
 * This class handles both standard and bonus symbols to determine the final reward.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public final class RewardCalculator {
    /**
     * Calculates the total reward for a given round of the game.
     * This method considers both standard and bonus symbols and their associated win combinations.
     *
     * @param bettingAmount          The amount bet in the game round.
     * @param board                  The game board after the round has ended.
     * @param appliedWinCombinations The win combinations that were applied during the game round.
     * @return The total reward calculated.
     * @throws RewardCalculatorException If any of the inputs are invalid.
     */
    public static double calculateReward(double bettingAmount,
                                         Board board,
                                         Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>>
                                             appliedWinCombinations) {
        // Validate input parameters.
        Validation.checkIsNegativeOrZero(bettingAmount,
            new RewardCalculatorException("The betting amount must be greater than 0"));
        Validation.requireNonNull(board, new RewardCalculatorException("Board cannot be null"));
        Validation.requireNonNull(appliedWinCombinations,
            new RewardCalculatorException("Applied win combinations cannot be null"));

        // Calculate reward for non-bonus symbols first.
        double rewardForStandardSymbols = appliedWinCombinations.entrySet().stream()
            // Validate keys (symbols) and values (win combinations) are not null.
            .peek(entry -> {
                Validation.requireNonNull(entry.getKey(),
                    new RewardCalculatorException("Symbols inside applied win combinations must not be null"));
                Validation.requireNonNull(entry.getValue(),
                    new RewardCalculatorException("Map of winning combinations divided by groups must not be null"));
            })
            // Map each symbol to its corresponding win combinations.
            .collect(Collectors.toMap(
                // The Symbol remains the same
                Map.Entry::getKey,
                entry -> entry.getValue().values().stream()
                    .peek(queue -> Validation.requireNonNull(queue,
                        new RewardCalculatorException("Queues of winning combinations must not be null")))
                    // Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
                    .map(Queue::peek)
                    // Ensure the queue was not empty
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                // In case of duplicate keys, use the new value
                (oldValue, newValue) -> newValue,
                // Collect into a TreeMap to sort by Symbol
                TreeMap::new
            )).entrySet().stream()
            // Exclude bonus symbols.
            .filter(entry -> !entry.getKey().isBonus())
            // Calculate the total reward for standard symbols.
            .mapToDouble(entry ->
                bettingAmount *
                entry.getKey().rewardMultiplier() *
                // Multiply the reward multipliers for compounded effects.
                entry.getValue().stream()
                    .mapToDouble(WinCombination::rewardMultiplier)
                    .reduce(1, (a, b) -> a * b)
            )
            .sum();

        AtomicReference<Double> reward = new AtomicReference<>(rewardForStandardSymbols);

        // Calculate additional rewards from bonus symbols, if applicable.
        if (rewardForStandardSymbols != 0) {
            Arrays.stream(board.getMatrixBoard())
                .flatMap(Arrays::stream)
                // Apply the effect of each bonus symbol on the reward.
                .filter(s -> Objects.nonNull(s) && s.isBonus() )
                .forEach(s -> reward.set(s.calculateReward(reward.get())));
        }

        return reward.get();
    }
}

