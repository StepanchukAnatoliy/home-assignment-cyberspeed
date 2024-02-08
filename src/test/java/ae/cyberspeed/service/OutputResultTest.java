package ae.cyberspeed.service;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for verifying the OutputResult functionality in the ae.cyberspeed.service package.
 * The tests cover scenarios including full output results with and without bonus symbols,
 * and output results with zero rewards.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class OutputResultTest {
    @Mock private Board mockBoard;
    @Mock private Symbol mockSymbol;
    @Mock private Symbol mockBonusSymbol;
    @Mock private WinCombination mockWinCombination;
    @Mock private Queue<WinCombination> mockWinCombinationQueue;

    private Symbol[][] matrix;
    private double calculatedReward;
    private Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations;

    /**
     * Sets up common mock behavior for all test methods, specifically initializing
     * symbol names and configuring common mock responses.
     */
    @BeforeEach
    public void setUp() {
        when(mockSymbol.name()).thenReturn("A");
    }

    /**
     * Prepares a 3x3 matrix filled with a single type of symbol for testing.
     */
    public void prepareMatrix() {
        matrix = new Symbol[3][3];
        // Fill the matrix with the mockSymbol
        IntStream.range(0, matrix.length).forEach(i -> Arrays.fill(matrix[i], mockSymbol));
    }

    /**
     * Tests printing the full output result, including a matrix representation, calculated reward,
     * applied winning combinations, and applied bonus symbols.
     */
    @Test
    public void testPrintFullOutputResult() {
        // Setup mock behaviors for symbols and winning combinations
        when(mockSymbol.isBonus()).thenReturn(false);
        when(mockBonusSymbol.name()).thenReturn("+500");
        when(mockBonusSymbol.isBonus()).thenReturn(true);
        when(mockWinCombination.name()).thenReturn("same_symbol_5_times");

        // Prepare the matrix and override one symbol to be a bonus symbol
        prepareMatrix();
        matrix[2][2] = mockBonusSymbol;

        calculatedReward = 1000.0;
        when(mockBoard.getMatrixBoard()).thenReturn(matrix);

        // Setup winning combinations
        Map<WinCombinationGroup, Queue<WinCombination>> winCombinationGroupMap = new HashMap<>();
        winCombinationGroupMap.put(WinCombinationGroup.SAME_SYMBOLS, mockWinCombinationQueue);
        when(mockWinCombinationQueue.peek()).thenReturn(mockWinCombination);

        appliedWinCombinations = new HashMap<>();
        appliedWinCombinations.put(mockSymbol, winCombinationGroupMap);

        // Assert the expected output result
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals("""
                {
                  "matrix" : [
                    [ "A", "A", "A" ],
                    [ "A", "A", "A" ],
                    [ "A", "A", "+500" ]
                  ],
                  "reward" : 1000.0,
                  "applied_winning_combinations" : {
                    "A" : [ "same_symbol_5_times" ]
                  },
                  "applied_bonus_symbol" : [ "+500" ]
                }""", new OutputResult(mockBoard, calculatedReward, appliedWinCombinations).print());

            // Verify validation checks were performed
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(5));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()));
        }

        // Verify interactions with mocks
        verify(mockSymbol, times(9)).name();
        verify(mockSymbol, times(8)).isBonus();
        verify(mockBonusSymbol, times(2)).name();
        verify(mockBonusSymbol, times(1)).isBonus();
        verify(mockWinCombination).name();
        verify(mockBoard, times(2)).getMatrixBoard();
        verify(mockWinCombinationQueue, times(1)).peek();
    }

    /**
     * Tests the output result when no bonus symbols are applied. This scenario simulates
     * a board filled with standard symbols and verifies if the output correctly represents
     * the board's state along with the calculated reward and applied winning combinations.
     *
     * @throws AssertionError if the actual output does not match the expected output
     */
    @Test
    public void testPrintOutputResultWithoutAppliedBonusSymbol() {
        // Mock behavior for standard symbols and winning combination
        when(mockSymbol.isBonus()).thenReturn(false);
        when(mockWinCombination.name()).thenReturn("same_symbol_5_times");

        // Prepare the game matrix with standard symbols
        prepareMatrix();

        // Define expected reward
        calculatedReward = 500.0;
        when(mockBoard.getMatrixBoard()).thenReturn(matrix);

        // Setup mock for winning combinations
        Map<WinCombinationGroup, Queue<WinCombination>> winCombinationGroupMap = new HashMap<>();
        winCombinationGroupMap.put(WinCombinationGroup.SAME_SYMBOLS, mockWinCombinationQueue);
        when(mockWinCombinationQueue.peek()).thenReturn(mockWinCombination);

        appliedWinCombinations = new HashMap<>();
        appliedWinCombinations.put(mockSymbol, winCombinationGroupMap);

        // Validate output
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals("""
                {
                  "matrix" : [
                    [ "A", "A", "A" ],
                    [ "A", "A", "A" ],
                    [ "A", "A", "A" ]
                  ],
                  "reward" : 500.0,
                  "applied_winning_combinations" : {
                    "A" : [ "same_symbol_5_times" ]
                  }
                }""", new OutputResult(mockBoard, calculatedReward, appliedWinCombinations).print());

            // Verify validation checks
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(5));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()));
        }

        // Verify interactions with mock objects
        verify(mockSymbol, times(10)).name();
        verify(mockSymbol, times(9)).isBonus();
        // Bonus symbol should not be involved in this test
        verify(mockBonusSymbol, never()).name();
        verify(mockBonusSymbol, never()).isBonus();
        verify(mockWinCombination).name();
        verify(mockBoard, times(2)).getMatrixBoard();
        verify(mockWinCombinationQueue, times(1)).peek();
    }

    /**
     * Tests the output result when the reward is zero. This scenario checks whether the OutputResult
     * class can correctly handle cases where no winning combinations are applied, and the reward is set to zero.
     *
     * @throws AssertionError if the actual output does not match the expected output
     */
    @Test
    public void testPrintOutputResultRewardZero() {
        // Prepare the game matrix with standard symbols
        prepareMatrix();

        // Set reward to zero
        calculatedReward = 0;
        when(mockBoard.getMatrixBoard()).thenReturn(matrix);
        appliedWinCombinations = new HashMap<>();

        // Validate output
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals("""
                {
                  "matrix" : [
                    [ "A", "A", "A" ],
                    [ "A", "A", "A" ],
                    [ "A", "A", "A" ]
                  ],
                  "reward" : 0.0
                }""", new OutputResult(mockBoard, calculatedReward, appliedWinCombinations).print());

            // Verify validation checks
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(2));
            mockValidation.verify(() -> Validation.checkIsNegative(anyDouble(), any()));
        }

        // Verify interactions with mock objects
        verify(mockSymbol, times(9)).name();
        // No bonus or win combination interactions in this scenario
        verify(mockSymbol, never()).isBonus();
        verify(mockBonusSymbol, never()).name();
        verify(mockBonusSymbol, never()).isBonus();
        verify(mockWinCombination, never()).name();
        verify(mockBoard, times(1)).getMatrixBoard();
        verify(mockWinCombinationQueue, never()).peek();
    }
}
