package ae.cyberspeed.wincombination;

import ae.cyberspeed.service.Board;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.error.WinCombinationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Unit tests for {@link MultiTimesWinCombination} class to ensure that win conditions are correctly
 * applied or not applied based on the game board's state and that exceptions are thrown for invalid inputs.
 * These tests verify the functionality of handling multiple occurrences of a symbol as part of win conditions.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class MultiTimesWinCombinationTest {
    /**
     * Tests that a win combination is added to the applied win combinations map when the win condition is met.
     * This scenario simulates a game board filled with the same symbol, meeting the condition for a win.
     */
    @Test
    public void applyWinConditionShouldAddCombinationWhenConditionIsMet() {
        // Mock dependencies
        Board mockBoard = mock(Board.class);
        Symbol mockSymbol = mock(Symbol.class);

        // Setup mock behavior
        when(mockSymbol.isBonus()).thenReturn(false);

        Symbol[][] matrix = {
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol}
        };
        when(mockBoard.getMatrixBoard()).thenReturn(matrix);

        // Initialize the win combination with a condition that is met by the mock setup
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
        MultiTimesWinCombination winCombination = new MultiTimesWinCombination(
            "test", 2.0, "same_symbols", "same_symbols", 3);

        // Apply win condition
        winCombination.applyWinCondition(mockBoard, appliedWinCombinations);

        // Verify outcomes
        assertTrue(appliedWinCombinations.containsKey(mockSymbol));

        Queue<WinCombination> queue = appliedWinCombinations.get(mockSymbol)
            .get(WinCombinationGroup.fromString("same_symbols"));

        assertNotNull(queue);
        assertFalse(queue.isEmpty());
        assertEquals(winCombination, queue.peek());
    }

    /**
     * Tests that a win combination is not added when the win condition is not met, such as when the required
     * number of occurrences is not reached.
     */
    @Test
    public void applyWinConditionShouldNotAddCombinationWhenConditionIsNotMet() {
        // Mock dependencies and setup mock behavior
        Board mockBoard = mock(Board.class);
        Symbol mockSymbol = mock(Symbol.class);
        when(mockSymbol.isBonus()).thenReturn(false);
        Symbol[][] matrix = {
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol}
        };
        when(mockBoard.getMatrixBoard()).thenReturn(matrix);

        // Initialize the win combination with a high occurrence requirement that is not met
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
        MultiTimesWinCombination winCombination = new MultiTimesWinCombination(
            "test", 2.0, "same_symbols", "test_group", 10);

        // Apply win condition
        winCombination.applyWinCondition(mockBoard, appliedWinCombinations);

        // Assert that no win combinations are applied due to unmet condition
        assertTrue(appliedWinCombinations.isEmpty(), "No win combinations should be applied for unmet condition");
    }

    /**
     * Verifies that a {@link WinCombinationException} is thrown when attempting to apply a win condition to a null board.
     *
     * @throws WinCombinationException if the board provided is null
     */
    @Test
    public void applyWinConditionShouldThrowExceptionForNullBoard() {
        // Initialize win combination
        MultiTimesWinCombination winCombination = new MultiTimesWinCombination(
            "test", 2.0, "same_symbols", "test_group", 3);

        // Assert exception for null board
        assertThrows(WinCombinationException.class, () ->
            winCombination.applyWinCondition(null, new HashMap<>()), "Should throw WinCombinationException for null board");
    }

    /**
     * Verifies that a {@link WinCombinationException} is thrown when the map to record applied win combinations is null.
     *
     * @throws WinCombinationException if the win combinations map provided is null
     */
    @Test
    public void applyWinConditionShouldThrowExceptionForNullWinCombinationsMap() {
        // Mock board and initialize win combination
        Board mockBoard = mock(Board.class);
        MultiTimesWinCombination winCombination = new MultiTimesWinCombination(
            "test", 2.0, "same_symbols", "test_group", 3);

        // Assert exception for null win combinations map
        assertThrows(WinCombinationException.class, () ->
            winCombination.applyWinCondition(mockBoard, null)
        );
    }

    /**
     * Tests that an {@link IllegalStateException} is thrown for an incorrectly specified win condition.
     * This ensures that the win combination logic correctly handles invalid configurations.
     *
     * @throws IllegalStateException if the win condition specified is incorrect
     */
    @Test
    public void applyWinConditionShouldThrowExceptionForIncorrectCondition() {
        // Mock board and initialize win combination with an incorrect condition
        Board mockBoard = mock(Board.class);
        MultiTimesWinCombination winCombination = new MultiTimesWinCombination(
            "test", 2.0, "incorrect_condition", "test_group", 3);

        // Assert that an incorrect condition triggers an exception
        assertThrows(IllegalStateException.class, () ->
            winCombination.applyWinCondition(mockBoard, new HashMap<>())
        );
    }
}

