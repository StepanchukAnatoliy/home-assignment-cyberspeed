package ae.cyberspeed.service;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.OutputResultException;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class encapsulates the result of a game round, containing the final state
 * of the game board, total calculated rewards, and details of any winning and bonus
 * combinations that were applied.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public final class OutputResult {
    @JsonProperty("matrix")
    private final Symbol[][] matrix;
    @JsonProperty("reward")
    private final double reward;
    @JsonProperty("applied_winning_combinations")
    private Map<Symbol, WinCombination[]> appliedWinningCombinations;
    @JsonProperty("applied_bonus_symbol")
    private Symbol[] appliedBonusSymbols;

    /**
     * Constructs an OutputResult object with the board state, calculated reward, and applied win combinations.
     *
     * @param board The game board after a round has been completed.
     * @param calculatedReward The total reward calculated for the round.
     * @param appliedWinCombinations The win combinations applied during the round.
     * @throws OutputResultException If any of the inputs are invalid or null.
     */
    public OutputResult(Board board,
                        double calculatedReward,
                        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations) {
        // Validate input parameters to ensure they are not null and meet expected criteria.
        Validation.requireNonNull(board, new OutputResultException("Board cannot be null"));
        Validation.checkIsNegative(calculatedReward, new OutputResultException("Calculated reward cannot be negative"));
        Validation.requireNonNull(appliedWinCombinations,
            new OutputResultException("Applied win combinations cannot be null"));

        // Initialize class fields with validated parameters.
        this.matrix = board.getMatrixBoard();
        this.reward = calculatedReward;

        // Process and sort applied winning combinations to ensure they are stored in a predictable order.
        this.appliedWinningCombinations = appliedWinCombinations.entrySet().stream()
            // Validate that each key (symbol) and value (map of win combination groups to queues of win combinations)
            // in the applied win combinations map is not null to ensure data integrity before processing.
            .peek(entry -> {
                Validation.requireNonNull(entry.getKey(),
                    new OutputResultException("Symbols inside applied win combinations must not be null"));
                Validation.requireNonNull(entry.getValue(),
                    new OutputResultException("Map of winning combinations divided by groups must not be null"));
            })
            .collect(Collectors.toMap(
                // The symbol remains the same
                Map.Entry::getKey,
                entry -> entry.getValue().values().stream()
                    // Validate each queue of win combinations is not null to ensure that we have valid win
                    // conditions to process.
                    .peek(queue -> Validation.requireNonNull(queue,
                        new OutputResultException("Queues of winning combinations must not be null"))
                    )
                    // Extract the first element from each queue to represent the applied win combination for
                    // each symbol. This assumes that if multiple win combinations are applicable, the first is
                    // the most relevant or significant.
                    .map(Queue::peek)
                    // Ensure the queue was not empty
                    .filter(Objects::nonNull)
                    .sorted()
                    // Collect into an array
                    .toArray(WinCombination[]::new),
                (existing, replacement) -> existing, // Merge function, in case of key collision
                TreeMap::new // Supply a TreeMap to keep it sorted
            ));

        // If there are winning combinations, extract and store any applied bonus symbols.
        if (appliedWinningCombinations.size() > 0) {
            // Collect keys (Bonus Symbols) into an array
            this.appliedBonusSymbols = Arrays.stream(board.getMatrixBoard())
                // Flatten the game board matrix to a stream of symbols, filtering out nulls and focusing
                // on bonus symbols.
                .flatMap(Arrays::stream)
                .filter(s -> Objects.nonNull(s) && s.isBonus())
                // Convert the filtered stream of bonus symbols into an array for inclusion in the output result.
                .toArray(Symbol[]::new);
        }
    }

    /**
     * Serializes the output result to a JSON string, formatted for readability.
     *
     * @return A JSON-formatted string representation of the output result.
     */
    public String print() {
        String json = "";

        try {
            // Initialize ObjectMapper to convert the output result object into a JSON string.
            ObjectMapper objectMapper = new ObjectMapper();
            // Enable pretty printing to make the JSON output more readable.
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Set serialization to exclude any empty collections or optional fields from the JSON output,
            // making the result cleaner and more concise.
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            // Additional string manipulation for enhanced readability in the final JSON output.
            json = objectMapper.writeValueAsString(this);
            json = json.replace("[ [", "[\n    [");
            json = json.replace("], [", "],\n    [");
            json = json.replace("] ],", "]\n  ],");

        } catch (Exception e) {
            e.printStackTrace();

        }

        return json;
    }
}
