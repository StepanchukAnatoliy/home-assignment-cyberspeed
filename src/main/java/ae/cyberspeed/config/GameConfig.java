package ae.cyberspeed.config;

import ae.cyberspeed.probability.Probability;
import ae.cyberspeed.util.GameConfigDeserializer;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.GameConfigException;
import ae.cyberspeed.wincombination.WinCombination;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Represents the configuration of a game board including dimensions,
 * probabilities for symbols, and win conditions.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
@JsonDeserialize(using = GameConfigDeserializer.class)
public record GameConfig (
    int matrixRows,
    int matrixColumns,
    List<Probability> probabilities,
    List<WinCombination> winCombinations
) {
    /**
     * Constructor for GameConfig which validates the configuration.
     *
     * @throws GameConfigException If the matrix size is not positive, if the probabilities
     *                             list is null or empty, if any probability coordinates are
     *                             out of bounds, or if the win combinations list is null or empty.
     */
    public GameConfig {
        // Validates that the number of rows and columns are positive integers.
        Validation.checkMatrixSizeIsNegative(matrixRows, matrixColumns);

        // Ensures that the probabilities collection is neither null nor empty.
        Validation.requireNonEmptyCollection(probabilities,
            new GameConfigException("Probabilities list cannot be null or empty"));

        // Verifies that all probability coordinates are within the bounds of the game board's matrix.
        Validation.checkProbabilityCoordinatesWithMatrix(probabilities, matrixRows, matrixColumns);

        // Confirms that the collection of win combinations is not null or empty.
        Validation.requireNonEmptyCollection(winCombinations,
            new GameConfigException("Win combinations list cannot be null or empty"));
    }
}


