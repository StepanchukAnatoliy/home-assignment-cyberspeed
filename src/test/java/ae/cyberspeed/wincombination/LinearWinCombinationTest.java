package ae.cyberspeed.wincombination;

import ae.cyberspeed.service.Board;
import ae.cyberspeed.symbol.Symbol;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the LinearWinCombination class, ensuring correct application of win conditions based on
 * predefined linear combinations. These tests validate the behavior for valid win scenarios, no win scenarios,
 * and invalid win condition specifications.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class LinearWinCombinationTest {
    /**
     * Tests that a valid linear win condition applies the expected win combinations to the symbol.
     * This scenario simulates a win by aligning symbols in a horizontal line across a 3x3 board.
     */
    @Test
    public void testApplyWinConditionWithValidLinearWin() {
        // Mock a Board and a Symbol for testing
        Board mockBoard = mock(Board.class);
        Symbol mockSymbol = mock(Symbol.class);

        // Setup symbol and board behavior
        when(mockSymbol.isBonus()).thenReturn(false);
        when(mockBoard.getCellValue(anyInt(), anyInt())).thenReturn(mockSymbol);

        // Setup a horizontal linear win condition
        String[][] coveredAreas = new String[][]{{"0:0", "0:1", "0:2"}};
        LinearWinCombination winCombination = new LinearWinCombination(
            "testWin",
            1.0,
            "linear_symbols",
            "horizontally_linear_symbols",
            coveredAreas
        );

        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();

        // Execute the win condition application
        winCombination.applyWinCondition(mockBoard, appliedWinCombinations);

        // Verify the win combinations are correctly applied
        assertFalse(appliedWinCombinations.isEmpty(), "Win combinations should be applied");
        assertTrue(appliedWinCombinations.containsKey(mockSymbol), "Win combinations map should contain the symbol");
        assertNotNull(appliedWinCombinations.get(mockSymbol)
            .get(WinCombinationGroup.fromString("horizontally_linear_symbols")), "Group queue should not be null");
        assertFalse(appliedWinCombinations
            .get(mockSymbol)
            .get(WinCombinationGroup.fromString("horizontally_linear_symbols")).isEmpty(),
            "Group queue should not be empty");
    }

    /**
     * Verifies that no win combinations are applied when the win condition is not met.
     * This test simulates a scenario where symbols do not align according to the win condition, resulting in no win.
     */
    @Test
    public void testApplyWinConditionWithNoWin() {
        // Mock a Board and two distinct Symbols
        Board mockBoard = mock(Board.class);
        Symbol mockSymbol1 = mock(Symbol.class);
        Symbol mockSymbol2 = mock(Symbol.class);

        // Setup board to return different symbols, preventing a win
        when(mockBoard.getCellValue(0, 0)).thenReturn(mockSymbol1);
        when(mockBoard.getCellValue(0, 1)).thenReturn(mockSymbol2);

        // Define a linear win condition that cannot be satisfied with the mock setup
        String[][] coveredAreas = new String[][]{{"0:0", "0:1"}};
        LinearWinCombination winCombination = new LinearWinCombination(
            "testWin",
            1.0,
            "linear_symbols",
            "testGroup",
            coveredAreas
        );

        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();

        // Execute the win condition application
        winCombination.applyWinCondition(mockBoard, appliedWinCombinations);

        // Verify that no win combinations are applied due to the unsatisfied condition
        assertTrue(appliedWinCombinations.isEmpty(), "No win combinations should be applied");
    }

    /**
     * Tests that specifying an invalid win condition results in an exception being thrown.
     * This scenario ensures that the system correctly handles and rejects win conditions that are not recognized.
     *
     * @throws IllegalStateException if the win condition is invalid
     */
    @Test
    public void testApplyWinConditionWithInvalidConditionThrowsException() {
        // Mock a Board for testing
        Board mockBoard = mock(Board.class);
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();

        // Define a win combination with an invalid condition
        LinearWinCombination winCombination = new LinearWinCombination(
            "testWin",
            1.0,
            "invalid_condition",
            "testGroup",
            new String[][]{{"0:0"}}
        );

        // Assert that an exception is thrown when applying an invalid win condition
        assertThrows(IllegalStateException.class, () ->
            winCombination.applyWinCondition(mockBoard, appliedWinCombinations));
    }
}

