package ae.cyberspeed.wincombination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the WinCombinationGroup enum, ensuring that the fromString method correctly interprets
 * string representations of win combination groups into their respective enum constants. These tests
 * verify the method's ability to handle valid inputs, case insensitivity, and invalid or null inputs,
 * ensuring robust and predictable behavior.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
class WinCombinationGroupTest {
    /**
     * Tests that fromString method returns the correct enum constant for valid text representations.
     * This verifies that each win combination group can be accurately identified from its string representation.
     */
    @Test
    public void fromStringShouldReturnCorrectEnumConstantForValidText() {
        assertEquals(WinCombinationGroup.SAME_SYMBOLS, WinCombinationGroup.fromString("same_symbols"));
        assertEquals(WinCombinationGroup.HORIZONTALLY_LINEAR_SYMBOLS,
            WinCombinationGroup.fromString("horizontally_linear_symbols"));
        assertEquals(WinCombinationGroup.VERTICALLY_LINEAR_SYMBOLS,
            WinCombinationGroup.fromString("vertically_linear_symbols"));
        assertEquals(WinCombinationGroup.LTR_DIAGONALLY_LINEAR_SYMBOLS,
            WinCombinationGroup.fromString("ltr_diagonally_linear_symbols"));
        assertEquals(WinCombinationGroup.RTL_DIAGONALLY_LINEAR_SYMBOLS,
            WinCombinationGroup.fromString("rtl_diagonally_linear_symbols"));
    }

    /**
     * Tests that the fromString method is case-insensitive when matching text to enum constants.
     * This ensures flexibility in interpreting string representations regardless of their case.
     */
    @Test
    public void fromStringShouldIgnoreCaseWhenMatchingText() {
        assertEquals(WinCombinationGroup.SAME_SYMBOLS, WinCombinationGroup.fromString("SAME_SYMBOLS"));
        assertEquals(WinCombinationGroup.HORIZONTALLY_LINEAR_SYMBOLS,
            WinCombinationGroup.fromString("Horizontally_Linear_Symbols"));
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown when fromString is given an invalid text representation.
     * This test ensures that only valid representations are accepted, enhancing data integrity.
     *
     * @throws IllegalArgumentException if the text representation does not match any known win combination group
     */
    @Test
    public void fromStringShouldThrowExceptionForInvalidText() {
        assertThrows(IllegalArgumentException.class, () -> WinCombinationGroup.fromString("non_existent_group"));
    }

    /**
     * Tests that an {@link IllegalArgumentException} is thrown when fromString is provided with null text.
     * This ensures that null values are handled appropriately, maintaining method contract integrity.
     *
     * @throws IllegalArgumentException if the provided text is null
     */
    @Test
    public void fromStringShouldThrowExceptionForNullText() {
        assertThrows(IllegalArgumentException.class, () -> WinCombinationGroup.fromString(null));
    }
}

