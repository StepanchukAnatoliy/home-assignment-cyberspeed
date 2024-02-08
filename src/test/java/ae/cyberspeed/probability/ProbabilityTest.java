package ae.cyberspeed.probability;

import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.util.error.ProbabilityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests for validating the probability calculation logic in the context of a game,
 * specifically focusing on the functionality that determines which symbol is selected
 * based on predefined probabilities.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class ProbabilityTest {
    @Mock private Random mockRandom;
    @Mock private Symbol mockSymbolA;
    @Mock private Symbol mockSymbolB;
    @Mock private Symbol mockSymbolC;

    private Probability probability;

    /**
     * Sets up the environment for testing the symbol selection logic, with mocked symbols and their associated probabilities.
     */
    private void setUpForDefineSymbolSelectedTest() {
        // Mocking symbolsProbability to return a specific map cumulative percentages:
        // 'A' - 1       'A' - 16.67      'A' - 16.67,
        // 'B' - 2       'B' - 33.33      'B' - 50.00,
        // 'C' - 3       'C' - 50.00      'C' - 100.00,
        // Total - 6
        Map<Symbol, Double> mockedMap = new HashMap<>();
        mockedMap.put(mockSymbolA, 1D); // Simulates 16.67% chance.
        mockedMap.put(mockSymbolB, 2D); // Simulates 33.33% (cumulative 50%) chance.
        mockedMap.put(mockSymbolC, 3D); // Simulates 50.00% (cumulative 100%) chance.

        // Setup probability instance with the mocked symbol probabilities.
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            probability = new StandardSymbolProbability(0, 0, mockedMap);
            // Verify that validation methods are called during object initialization.
            mockValidation.verify(() -> Validation.validateSymbolsProbability(any()));
            mockValidation.verify(() -> Validation.checkSymbolCoordinatesIsNegative(anyInt(), anyInt()));
        }
    }

    /**
     * Tests the scenario where the random value corresponds to selecting the first symbol based on its probability.
     */
    @Test
    public void testDefineSymbolFirstSymbolSelected() {
        setUpForDefineSymbolSelectedTest();
        // Simulate a random value within the range for the first symbol.
        when(mockRandom.nextDouble()).thenReturn(15.0);

        Symbol result = probability.defineSymbol(mockRandom);
        assertEquals(mockSymbolA, result);

        verify(mockRandom).nextDouble();
    }

    /**
     * Tests the scenario where the random value corresponds to selecting the middle symbol based on
     * cumulative probabilities.
     */
    @Test
    public void testDefineSymbolMiddleSymbolSelected() {
        setUpForDefineSymbolSelectedTest();
        // Simulate a random value within the range for the second symbol.
        when(mockRandom.nextDouble(anyDouble())).thenReturn(45.0);

        Symbol result = probability.defineSymbol(mockRandom);
        assertEquals(mockSymbolB, result);

        verify(mockRandom).nextDouble(anyDouble());
    }

    /**
     * Tests the behavior of the defineSymbol method when the last symbol is selected
     * by the random number generator.
     * <p>
     * This test simulates a scenario where the random number generator returns a high value,
     * expecting the last symbol in the sequence to be selected based on the probability distribution.
     * </p>
     *
     * @throws AssertionError if the expected symbol is not returned
     */
    @Test
    public void testDefineSymbolLastSymbolSelected() {
        // Setup the environment for testing symbol selection
        setUpForDefineSymbolSelectedTest();
        // Simulate the random number generator always returning 100.0
        when(mockRandom.nextDouble(anyDouble())).thenReturn(100.0);

        // Call the method under test and capture the result
        Symbol result = probability.defineSymbol(mockRandom);
        // Assert that the expected symbol is returned
        assertEquals(mockSymbolC, result);

        // Verify the interaction with the mock random number generator
        verify(mockRandom).nextDouble(anyDouble());
    }

    /**
     * Tests the behavior of the defineSymbol method when the symbols probability map is empty.
     * <p>
     * This test checks that a ProbabilityException is thrown when attempting to define a symbol
     * with an empty probability map, ensuring the system correctly handles invalid configurations.
     * </p>
     *
     * @throws ProbabilityException if the symbols probability map is empty
     */
    @Test
    public void testDefineSymbolWithEmptyMap() {
        // Mock the random number generator to return a specific value
        when(mockRandom.nextDouble(anyDouble())).thenReturn(77.77);

        // Expecting a ProbabilityException when the map is empty
        Map<Symbol, Double> emptyMap = new HashMap<>();
        emptyMap.put(mockSymbolA, 1D); //to avoid record creation symbols probability test

        // Mock static utility methods to verify their invocation
        try (MockedStatic<Validation> mockValidation = Mockito.mockStatic(Validation.class)) {
            probability = new StandardSymbolProbability(0, 0, emptyMap);

            mockValidation.verify(() -> Validation.validateSymbolsProbability(any()));
            mockValidation.verify(() -> Validation.checkSymbolCoordinatesIsNegative(anyInt(), anyInt()));
        }

        // Clear the map to simulate the condition being tested
        probability.symbolsProbability().clear();

        // Attempt to define a symbol, expecting a ProbabilityException to be thrown
        assertThrows(ProbabilityException.class, () -> probability.defineSymbol(mockRandom));
    }
}

