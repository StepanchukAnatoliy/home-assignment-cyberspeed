package ae.cyberspeed.service;

import ae.cyberspeed.config.GameConfig;
import ae.cyberspeed.probability.Probability;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.BoardGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This test class verifies the behavior of the Board class, including the correct generation
 * of the board without bonus symbols, with bonus symbols, and handling of coordinate mismatches.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class BoardTest {
    @Mock private GameConfig mockConfig;
    @Mock private Random mockRandom;
    @Mock private Probability mockProbability;
    @Mock private Symbol mockSymbolA;
    @Mock private Symbol mockSymbolB;
    @Mock private Symbol mockSymbolC;
    @Mock private Symbol mockBonusSymbol;

    /**
     * Sets up the common mock behavior for all test methods.
     * This includes configuring the mock GameConfig to return specific values for matrix dimensions
     * and a list of Probability objects.
     */
    @BeforeEach
    public void setUp() {
        when(mockConfig.matrixRows()).thenReturn(3);
        when(mockConfig.matrixColumns()).thenReturn(3);

        List<Probability> probabilities = new ArrayList<>();
        probabilities.add(mockProbability);
        when(mockConfig.probabilities()).thenReturn(probabilities);
    }

    /**
     * Tests that the Board class generates the board matrix correctly without any bonus symbols.
     *
     * @throws BoardGenerationException if board generation fails due to invalid configuration.
     */
    @Test
    public void testGenerateBoardCorrectlyWithoutBonusSymbols() {
        // Mock behavior to simulate a standard symbol selection process
        when(mockRandom.nextInt(anyInt())).thenReturn(60);
        when(mockProbability.isBonus()).thenReturn(false);
        when(mockProbability.coordinatesMatch(anyInt(), anyInt())).thenReturn(true);
        when(mockProbability.defineSymbol(any())).thenReturn(
            mockSymbolA, mockSymbolA, mockSymbolA,
            mockSymbolB, mockSymbolB, mockSymbolB,
            mockSymbolC, mockSymbolC, mockSymbolC
        );

        Board board;

        // Ensure validation methods are called and no exceptions are thrown during board generation
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            board = new Board(mockConfig, mockRandom);

            mockValidation.verify(() -> Validation.requireNonNull(any(), any()));
            mockValidation.verify(() -> Validation.checkBoardAfterGeneration(any()));
        }

        // Verify the generated board matches the expected configuration
        Symbol[][] matrixBoard = board.getMatrixBoard();

        assertNotNull(matrixBoard);
        assertEquals(3, matrixBoard.length);
        assertEquals(3, matrixBoard[0].length);
        assertArrayEquals(
            new Symbol[][] {
                { mockSymbolA, mockSymbolA, mockSymbolA },
                { mockSymbolB, mockSymbolB, mockSymbolB },
                { mockSymbolC, mockSymbolC, mockSymbolC }
            }, matrixBoard
        );

        // Verify method calls to ensure the expected interactions occurred
        verify(mockRandom, times(9)).nextInt(anyInt());
        verify(mockProbability, times(9)).isBonus();
        verify(mockProbability, times(9)).coordinatesMatch(anyInt(), anyInt());
        verify(mockProbability, times(9)).defineSymbol(any());
        verify(mockConfig, times(5)).matrixRows();
        verify(mockConfig, times(13)).matrixColumns();
        verify(mockConfig, times(9)).probabilities();
    }

    /**
     * Tests that the Board class generates the board matrix correctly and includes bonus symbols.
     *
     * @throws BoardGenerationException if board generation fails due to invalid configuration.
     */
    @Test
    public void testGenerateBoardCorrectlyWithBonusSymbols() {
        // Mock behavior to simulate both standard and bonus symbol selection process
        when(mockRandom.nextInt(anyInt())).thenReturn(20, 30, 40, 50, 60, 70, 80, 90, 2);
        when(mockProbability.isBonus()).thenReturn(false, false, false, false, false, false, false, false, true);
        when(mockProbability.coordinatesMatch(anyInt(), anyInt())).thenReturn(true);
        when(mockProbability.defineSymbol(any())).thenReturn(
            mockSymbolA, mockSymbolA, mockSymbolA,
            mockSymbolB, mockSymbolB, mockSymbolB,
            mockSymbolC, mockSymbolC, mockBonusSymbol
        );

        Board board;

        // Ensure validation methods are called and no exceptions are thrown during board generation
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            board = new Board(mockConfig, mockRandom);

            mockValidation.verify(() -> Validation.requireNonNull(any(), any()));
            mockValidation.verify(() -> Validation.checkBoardAfterGeneration(any()));
        }

        // Verify the generated board matches the expected configuration, including a bonus symbol
        Symbol[][] matrixBoard = board.getMatrixBoard();

        assertNotNull(matrixBoard);
        assertEquals(3, matrixBoard.length);
        assertEquals(3, matrixBoard[0].length);
        assertArrayEquals(
            new Symbol[][] {
                { mockSymbolA, mockSymbolA, mockSymbolA },
                { mockSymbolB, mockSymbolB, mockSymbolB },
                { mockSymbolC, mockSymbolC, mockBonusSymbol }
            }, matrixBoard
        );

        // Verify method calls to ensure the expected interactions occurred
        verify(mockRandom, times(9)).nextInt(anyInt());
        verify(mockProbability, times(9)).isBonus();
        verify(mockProbability, times(9)).coordinatesMatch(anyInt(), anyInt());
        verify(mockProbability, times(9)).defineSymbol(any());
        verify(mockConfig, times(5)).matrixRows();
        verify(mockConfig, times(13)).matrixColumns();
        verify(mockConfig, times(9)).probabilities();
    }

    /**
     * Tests that the Board class throws a BoardGenerationException when symbol coordinates do not match.
     *
     * @throws BoardGenerationException to indicate that board generation cannot proceed due to coordinate mismatch.
     */
    @Test
    public void testGenerateBoardCoordinatesMissmatch() {
        // Mock behavior to simulate a failure in matching symbol coordinates
        when(mockRandom.nextInt(anyInt())).thenReturn(60);
        when(mockProbability.isBonus()).thenReturn(false);
        when(mockProbability.coordinatesMatch(anyInt(), anyInt())).thenReturn(false);

        // Ensure that BoardGenerationException is thrown and validation methods are correctly called or not called
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertThrows(BoardGenerationException.class, () -> new Board(mockConfig, mockRandom));

            mockValidation.verify(() -> Validation.requireNonNull(any(), any()));
            // Verify that checkBoardAfterGeneration is never called due to the coordinate mismatch
            mockValidation.verify(() -> Validation.checkBoardAfterGeneration(any()), never());
        }

        // Verify method calls to ensure the expected interactions occurred, and some methods are not called
        // due to the exception
        verify(mockRandom).nextInt(anyInt());
        verify(mockProbability).isBonus();
        verify(mockProbability).coordinatesMatch(anyInt(), anyInt());
        // defineSymbol is never called due to the coordinate mismatch preventing board generation
        verify(mockProbability, never()).defineSymbol(any());
        verify(mockConfig, times(2)).matrixRows();
        verify(mockConfig, times(2)).matrixColumns();
        verify(mockConfig).probabilities();
    }
}

