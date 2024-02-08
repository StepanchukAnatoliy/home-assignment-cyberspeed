package ae.cyberspeed.util;

import ae.cyberspeed.probability.Probability;
import ae.cyberspeed.probability.StandardSymbolProbability;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.error.BoardGenerationException;
import ae.cyberspeed.util.error.GameConfigException;
import ae.cyberspeed.util.error.ProbabilityException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the Validation utility class in the ae.cyberspeed.util package. These tests verify
 * the functionality of input argument validation, betting amount validation, symbols probability validation,
 * and various other utility validation methods critical for ensuring correct application behavior and data integrity.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class ValidationTest {
    /**
     * Verifies that the correct path type is returned when arguments are correctly provided.
     * This test simulates the scenario where a user provides a valid configuration file path and a valid betting amount.
     */
    @Test
    public void whenArgumentsAreCorrectThenReturnPathType() {
        // Setup the expected input arguments for the test
        String[] args = {"--config", "config.json", "--betting-amount", "100"};
        String expectedPath = "ArbitraryFilePath"; // Expected result for this test scenario

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(Paths.get("config.json"))).thenReturn(new byte[0]);
            String result = Validation.checkInputArgumentsAndWhereFindConfigFile(args);

            // Assert that the expected path is returned
            assertEquals(expectedPath, result);
        }
    }

    /**
     * Tests that an {@link IllegalArgumentException} is thrown when the provided arguments are insufficient.
     * This scenario represents missing arguments which are necessary for the application to run correctly.
     */
    @Test
    public void whenArgumentsAreInsufficientThenThrowException() {
        // Insufficient arguments provided for the configuration
        String[] args = {"--config", "config.json"}; // Missing betting amount argument

        // Assert that an exception is thrown with the correct message
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            Validation.checkInputArgumentsAndWhereFindConfigFile(args));

        // Expected user instruction message
        String expectedMessage =
            """
            Usage:
            java -jar <your-jar-file> --config <config file> --betting-amount <betting amount>
            
            For example:
            java -jar <your-jar-file> --config config.json --betting-amount 100""";

        assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Validates that an {@link IllegalArgumentException} is thrown when the betting amount is invalid.
     * This test ensures that users are prompted to provide a valid betting amount.
     */
    @Test
    public void whenBettingAmountIsInvalidThenThrowException() {
        // Providing an invalid betting amount argument
        String[] args = {"--config", "config.json", "--betting-amount", "invalid"};

        // Assert that an exception is thrown with the correct message
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            Validation.checkInputArgumentsAndWhereFindConfigFile(args));

        String expectedMessage = "Please specify a valid betting amount after --betting-amount";
        assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Tests that a {@link ProbabilityException} is thrown when attempting to validate symbols' probabilities
     * with a null value. This ensures that the application correctly handles null inputs for probability validations.
     */
    @Test
    public void validateSymbolsProbabilityWithNullThrowsException() {
        assertThrows(ProbabilityException.class, () -> Validation.validateSymbolsProbability(null));
    }

    /**
     * Verifies that a {@link ProbabilityException} is thrown when the symbols' probability map is empty.
     * This test ensures that the validation process correctly identifies and rejects empty probability maps.
     */
    @Test
    public void validateSymbolsProbabilityWithEmptyMapThrowsException() {
        assertThrows(ProbabilityException.class, () -> Validation.validateSymbolsProbability(new HashMap<>()));
    }

    /**
     * Tests that a {@link ProbabilityException} is thrown when a symbol's probability value is negative.
     * This ensures that negative values, which are not valid in this context, are correctly identified and rejected.
     */
    @Test
    public void validateSymbolsProbabilityWithNegativeValueThrowsException() {
        Map<Symbol, Double> symbolsProbability = new HashMap<>();
        symbolsProbability.put(mock(Symbol.class), -0.1);
        assertThrows(ProbabilityException.class, () -> Validation.validateSymbolsProbability(symbolsProbability));
    }

    /**
     * Verifies that an exception is thrown when symbol coordinates are negative in the row dimension.
     * Ensures the validation logic correctly identifies invalid symbol placement within the game board.
     */
    @Test
    public void checkSymbolCoordinatesIsNegativeWithNegativeRowThrowsException() {
        assertThrows(ProbabilityException.class, () -> Validation.checkSymbolCoordinatesIsNegative(-1, 0));
    }

    /**
     * Verifies that an exception is thrown when symbol coordinates are negative in the column dimension.
     * This test confirms that the validation catches improper symbol configurations.
     */
    @Test
    public void checkSymbolCoordinatesIsNegativeWithNegativeColumnThrowsException() {
        assertThrows(ProbabilityException.class, () -> Validation.checkSymbolCoordinatesIsNegative(0, -1));
    }

    /**
     * Tests that a negative row value for the matrix size triggers an exception,
     * indicating improper configuration of the game board's dimensions.
     */
    @Test
    public void checkMatrixSizeIsNegativeWithNegativeRowsThrowsException() {
        assertThrows(GameConfigException.class, () -> Validation.checkMatrixSizeIsNegative(-1, 1));
    }

    /**
     * Tests that a negative column value for the matrix size triggers an exception,
     * highlighting incorrect setup for the game board's width.
     */
    @Test
    public void checkMatrixSizeIsNegativeWithNegativeColumnsThrowsException() {
        assertThrows(GameConfigException.class, () -> Validation.checkMatrixSizeIsNegative(1, -1));
    }

    /**
     * Confirms that a non-empty, non-null collection passes validation without throwing an exception,
     * ensuring collections used in the application are properly populated.
     */
    @Test
    public void requireNonEmptyCollectionWithNonNullNonEmptyCollectionDoesNotThrow() {
        assertDoesNotThrow(() ->
            Validation.requireNonEmptyCollection(Collections.singletonList("item"), new RuntimeException("Collection is empty")));
    }

    /**
     * Verifies that providing a null collection to the validation method triggers an exception,
     * ensuring that essential collection-based configurations are not omitted.
     */
    @Test
    public void requireNonEmptyCollectionWithNullThrows() {
        assertThrows(RuntimeException.class, () ->
            Validation.requireNonEmptyCollection(null, new RuntimeException("Collection is null")));
    }

    /**
     * Checks that an empty collection results in an exception, emphasizing the requirement for
     * non-empty collections in game configuration and logic.
     */
    @Test
    public void requireNonEmptyCollectionWithEmptyCollectionThrows() {
        assertThrows(RuntimeException.class, () ->
            Validation.requireNonEmptyCollection(Collections.emptyList(), new RuntimeException("Collection is empty")));
    }

    /**
     * Ensures that passing a non-null object to the validation method does not result in an exception,
     * affirming the object's presence where it is mandatory.
     */
    @Test
    public void requireNonNullWithNonNullObjectDoesNotThrow() {
        assertDoesNotThrow(() ->
            Validation.requireNonNull(new Object(), new RuntimeException("Object is null")));
    }

    /**
     * Tests that a null object triggers an exception, safeguarding against null references where objects are required.
     */
    @Test
    public void requireNonNullWithNullObjectThrows() {
        assertThrows(RuntimeException.class, () ->
            Validation.requireNonNull(null, new RuntimeException("Object is null")));
    }

    /**
     * Verifies that a valid game board does not cause an exception when checked after generation,
     * confirming the board's integrity and readiness for gameplay.
     */
    @Test
    public void checkBoardAfterGenerationWithValidBoardDoesNotThrow() {
        Symbol[][] board = new Symbol[1][1]; // Assuming Symbol is an interface or class you've defined
        assertDoesNotThrow(() ->
            Validation.checkBoardAfterGeneration(board));
    }

    /**
     * Asserts that an empty game board results in an exception, indicating a failure in board generation
     * or an incorrect game setup.
     */
    @Test
    public void checkBoardAfterGenerationWithEmptyBoardThrows() {
        Symbol[][] board = new Symbol[0][0];
        assertThrows(BoardGenerationException.class, () ->
            Validation.checkBoardAfterGeneration(board));
    }

    /**
     * Verifies that a positive value does not trigger an exception when checking for negative values.
     */
    @Test
    public void checkIsNegativeWithPositiveValueDoesNotThrow() {
        // No exception expected for a positive value
        assertDoesNotThrow(() ->
            Validation.checkIsNegative(1.0, new RuntimeException("Value is negative")));
    }

    /**
     * Verifies that a negative value triggers a RuntimeException when checking for negative values.
     */
    @Test
    public void checkIsNegativeWithNegativeValueThrows() {
        // Expect an exception for a negative value
        assertThrows(RuntimeException.class, () ->
            Validation.checkIsNegative(-1.0, new RuntimeException("Value is negative")));
    }

    /**
     * Verifies that a positive value does not trigger an exception when checking for negative or zero values.
     */
    @Test
    public void checkIsNegativeOrZeroWithPositiveValueDoesNotThrow() {
        // No exception expected for a positive value
        assertDoesNotThrow(() ->
            Validation.checkIsNegativeOrZero(1.0, new RuntimeException("Value is negative or zero")));
    }

    /**
     * Verifies that a value of zero triggers a RuntimeException when checking for negative or zero values.
     */
    @Test
    public void checkIsNegativeOrZeroWithZeroValueThrows() {
        // Expect an exception for a zero value
        assertThrows(RuntimeException.class, () ->
            Validation.checkIsNegativeOrZero(0.0, new RuntimeException("Value is negative or zero")));
    }

    /**
     * Verifies that a non-blank string does not trigger an exception when checking for blank strings.
     */
    @Test
    public void checkIsBlankStringWithNonBlankStringDoesNotThrow() {
        // No exception expected for a non-blank string
        assertDoesNotThrow(() ->
            Validation.checkIsBlankString("valid", new RuntimeException("String is blank")));
    }

    /**
     * Verifies that a blank string triggers a RuntimeException when checking for blank strings.
     */
    @Test
    public void checkIsBlankStringWithBlankStringThrows() {
        // Expect an exception for a blank string
        assertThrows(RuntimeException.class, () ->
            Validation.checkIsBlankString(" ", new RuntimeException("String is blank")));
    }

    /**
     * Verifies that having both values positive triggers a RuntimeException when at least one negative value is expected.
     */
    @Test
    public void checkTwoValuesPositiveWithBothPositiveValuesThrows() {
        // Expect an exception when both values are positive and at least one negative is expected
        assertThrows(RuntimeException.class, () ->
            Validation.checkTwoValuesPositive(1.0, 1.0, new RuntimeException("Both values are positive")));
    }

    /**
     * Verifies that one negative and one positive value do not trigger an exception when at least one negative value is expected.
     */
    @Test
    public void checkTwoValuesPositiveWithOneNegativeValueDoesNotThrow() {
        // No exception expected when one value is negative
        assertDoesNotThrow(() ->
            Validation.checkTwoValuesPositive(-1.0, 1.0, new RuntimeException("Both values are positive")));
    }

    /**
     * Verifies that valid probability coordinates within the specified matrix dimensions do not trigger an exception.
     * This test ensures that the validation logic correctly identifies and accepts correct symbol placements within
     * the game board. Each {@link StandardSymbolProbability} represents a symbol's position in the matrix, and all
     * positions are within bounds for a 2x2 matrix, thus not expecting any validation errors.
     */
    @Test
    public void checkProbabilityCoordinatesWithMatrixValidCoordinatesDoesNotThrowException() {
        // Mock four StandardSymbolProbability objects with valid coordinates for a 2x2 matrix
        // Setup mock responses for each probability object to simulate valid symbol placements
        StandardSymbolProbability prob1 = mock(StandardSymbolProbability.class);
        when(prob1.isBonus()).thenReturn(false);
        when(prob1.row()).thenReturn(0);
        when(prob1.column()).thenReturn(0);

        StandardSymbolProbability prob2 = mock(StandardSymbolProbability.class);
        when(prob2.isBonus()).thenReturn(false);
        when(prob2.row()).thenReturn(0);
        when(prob2.column()).thenReturn(1);

        StandardSymbolProbability prob3 = mock(StandardSymbolProbability.class);
        when(prob3.isBonus()).thenReturn(false);
        when(prob3.row()).thenReturn(1);
        when(prob3.column()).thenReturn(0);

        StandardSymbolProbability prob4 = mock(StandardSymbolProbability.class);
        when(prob4.isBonus()).thenReturn(false);
        when(prob4.row()).thenReturn(1);
        when(prob4.column()).thenReturn(1);

        List<Probability> probabilities = Arrays.asList(prob1, prob2, prob3, prob4);

        // Validate that no exceptions are thrown for valid probability coordinates
        assertDoesNotThrow(() ->
            Validation.checkProbabilityCoordinatesWithMatrix(probabilities, 2, 2));
    }

    /**
     * Tests that probability coordinates outside the specified matrix dimensions trigger a {@link ProbabilityException}.
     * This scenario checks the validation logic for correctly identifying and rejecting symbol placements that exceed the
     * game board boundaries, ensuring that all symbols are placed within the playable area.
     *
     * @throws ProbabilityException if any probability coordinates are out of the specified matrix bounds
     */
    @Test
    public void checkProbabilityCoordinatesWithMatrixCoordinatesOutOfBoundsThrowsException() {
        // Mock a StandardSymbolProbability object with coordinates outside a 2x2 matrix to simulate an invalid placement
        StandardSymbolProbability prob1 = mock(StandardSymbolProbability.class);
        when(prob1.isBonus()).thenReturn(false);
        when(prob1.row()).thenReturn(3); // Out of bounds
        when(prob1.column()).thenReturn(0);

        List<Probability> probabilities = Arrays.asList(prob1);

        // Assert that a ProbabilityException is thrown for out-of-bounds probability coordinates
        assertThrows(ProbabilityException.class, () ->
            Validation.checkProbabilityCoordinatesWithMatrix(probabilities, 2, 2));
    }

    /**
     * Validates that missing probability coordinates for the specified matrix size trigger a {@link ProbabilityException}.
     * This test ensures that the validation process requires complete coverage of the game board by the probabilities,
     * preventing configurations where not all board cells are accounted for, which could lead to undefined game behavior.
     *
     * @throws ProbabilityException if probabilities do not cover all matrix coordinates
     */
    @Test
    public void checkProbabilityCoordinatesWithMatrixMissingCoordinatesThrowsException() {
        // Mock a single StandardSymbolProbability object, indicating incomplete coverage for a 2x2 matrix
        StandardSymbolProbability prob1 = mock(StandardSymbolProbability.class);
        when(prob1.isBonus()).thenReturn(false);
        when(prob1.row()).thenReturn(0);
        when(prob1.column()).thenReturn(0);

        // Assuming matrix size is 2x2, but we're only providing probability for one cell
        List<Probability> probabilities = List.of(prob1);

        // Assert that a ProbabilityException is thrown for incomplete probability coordinates, ensuring full board
        // coverage is required
        assertThrows(ProbabilityException.class, () ->
            Validation.checkProbabilityCoordinatesWithMatrix(probabilities, 2, 2));
    }
}

