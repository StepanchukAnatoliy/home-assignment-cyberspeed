package ae.cyberspeed.service;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for the RewardCalculator functionality, focusing on various game outcomes
 * including wins with single and multiple bonus symbols, and losses with no reward.
 * Verifies correct reward calculation based on symbol multipliers and bonus symbol rewards.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class RewardCalculatorTest {
    @Mock private Board mockBoard;
    @Mock private Symbol mockSymbol;
    @Mock private Symbol mockBonusSymbol;
    @Mock private WinCombination mockWinCombination;
    @Mock private Queue<WinCombination> mockWinCombinationQueue;

    private final double bettingAmount = 100.0;
    private Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations;

    /**
     * Test the reward calculation for a winning game scenario with a single bonus symbol.
     * Verifies that the reward is correctly calculated using the symbol's reward multiplier
     * and the bonus symbol's calculated reward.
     *
     * @throws AssertionError if the calculated reward does not match the expected value
     */
    @Test
    public void testCalculateReward_WinGamePlusOneBonus() {
        // Mock symbol and bonus symbol behavior
        when(mockSymbol.isBonus()).thenReturn(false);
        when(mockSymbol.rewardMultiplier()).thenReturn(10D);
        when(mockBonusSymbol.isBonus()).thenReturn(true);
        when(mockBonusSymbol.calculateReward(anyDouble())).thenReturn(1000D);
        when(mockWinCombination.rewardMultiplier()).thenReturn(2D);
        when(mockBoard.getMatrixBoard()).thenReturn(new Symbol[][] {
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockBonusSymbol}
        });

        // Setup winning combination map
        Map<WinCombinationGroup, Queue<WinCombination>> winCombinationGroupMap = new HashMap<>();
        winCombinationGroupMap.put(WinCombinationGroup.SAME_SYMBOLS, mockWinCombinationQueue);
        when(mockWinCombinationQueue.peek()).thenReturn(mockWinCombination);

        appliedWinCombinations = new HashMap<>();
        appliedWinCombinations.put(mockSymbol, winCombinationGroupMap);

        // Validate reward calculation
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals(1000D, RewardCalculator.calculateReward(bettingAmount, mockBoard, appliedWinCombinations));

            // Verify validation checks
            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(5));
        }

        // Verify mock interactions
        verify(mockSymbol, times(9)).isBonus();
        verify(mockSymbol).rewardMultiplier();
        verify(mockBonusSymbol).isBonus();
        verify(mockBonusSymbol).calculateReward(anyDouble());
        verify(mockWinCombination).rewardMultiplier();
        verify(mockBoard).getMatrixBoard();
    }

    /**
     * Tests the reward calculation for winning games with multiple bonus symbols.
     * Ensures that the total reward reflects both the base symbol multipliers and
     * the additional rewards from multiple bonus symbols.
     *
     * @throws AssertionError if the calculated reward does not match the expected outcome
     */
    @Test
    public void testCalculateReward_WinGamePlusMultipleBonuses() {
        // Mock responses for symbol and bonus symbol behavior
        when(mockSymbol.isBonus()).thenReturn(false);
        when(mockSymbol.rewardMultiplier()).thenReturn(10D);
        // Return different rewards for subsequent calls to simulate multiple bonuses
        when(mockBonusSymbol.isBonus()).thenReturn(true);
        when(mockBonusSymbol.calculateReward(anyDouble())).thenReturn(1000D, 2000D);
        when(mockWinCombination.rewardMultiplier()).thenReturn(2D);

        // Prepare the board with two bonus symbols
        when(mockBoard.getMatrixBoard()).thenReturn(new Symbol[][] {
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockSymbol, mockSymbol},
            {mockSymbol, mockBonusSymbol, mockBonusSymbol}
        });

        // Setup winning combination queue and map
        Map<WinCombinationGroup, Queue<WinCombination>> winCombinationGroupMap = new HashMap<>();
        winCombinationGroupMap.put(WinCombinationGroup.SAME_SYMBOLS, mockWinCombinationQueue);
        when(mockWinCombinationQueue.peek()).thenReturn(mockWinCombination);

        appliedWinCombinations = new HashMap<>();
        appliedWinCombinations.put(mockSymbol, winCombinationGroupMap);

        // Assert the expected total reward with validation checks
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals(2000D, RewardCalculator.calculateReward(bettingAmount, mockBoard, appliedWinCombinations));

            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(5));
        }

        // Verify symbol and bonus symbol interactions
        verify(mockSymbol, times(8)).isBonus();
        verify(mockSymbol).rewardMultiplier();
        verify(mockBonusSymbol, times(2)).isBonus();
        verify(mockBonusSymbol, times(2)).calculateReward(anyDouble());
        verify(mockWinCombination).rewardMultiplier();
        verify(mockBoard).getMatrixBoard();
    }

    /**
     * Tests the reward calculation for losing games, expecting no reward.
     * This test verifies that the reward calculator correctly returns a zero reward
     * when no winning combinations are present.
     *
     * @throws AssertionError if the calculated reward is not zero as expected
     */
    @Test
    public void testCalculateReward_LoseGameNoReward() {
        // Initialize win combinations map as empty to simulate a loss
        appliedWinCombinations = new HashMap<>();

        // Assert that the calculated reward is zero with validation checks
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            assertEquals(0D, RewardCalculator.calculateReward(bettingAmount, mockBoard, appliedWinCombinations));

            mockValidation.verify(() -> Validation.checkIsNegativeOrZero(anyDouble(), any()));
            mockValidation.verify(() -> Validation.requireNonNull(any(), any()), times(2));
        }

        // Verify there are no interactions with symbols or win combinations as expected in a loss
        verify(mockSymbol, never()).isBonus();
        verify(mockSymbol, never()).rewardMultiplier();
        verify(mockBonusSymbol, never()).isBonus();
        verify(mockBonusSymbol, never()).calculateReward(anyDouble());
        verify(mockWinCombination, never()).rewardMultiplier();
        verify(mockBoard, never()).getMatrixBoard();
    }
}
