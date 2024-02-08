package ae.cyberspeed.wincombination;

import java.util.Arrays;

/**
 * Enumerates the groups of win combinations based on their pattern of alignment on the game board.
 * This enumeration facilitates identifying the type of win combination (e.g., linear in a specific direction,
 * same symbols) to apply specific game rules and rewards.
 * <p>
 * Patterns include:
 * - SAME_SYMBOLS: A group where the same symbols are aligned without specifying the direction.
 * - HORIZONTALLY_LINEAR_SYMBOLS: Symbols aligned horizontally.
 * - VERTICALLY_LINEAR_SYMBOLS: Symbols aligned vertically.
 * - LTR_DIAGONALLY_LINEAR_SYMBOLS: Symbols aligned diagonally from left to right.
 * - RTL_DIAGONALLY_LINEAR_SYMBOLS: Symbols aligned diagonally from right to left.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public enum WinCombinationGroup {
    SAME_SYMBOLS("same_symbols"),
    HORIZONTALLY_LINEAR_SYMBOLS("horizontally_linear_symbols"),
    VERTICALLY_LINEAR_SYMBOLS("vertically_linear_symbols"),
    LTR_DIAGONALLY_LINEAR_SYMBOLS("ltr_diagonally_linear_symbols"),
    RTL_DIAGONALLY_LINEAR_SYMBOLS("rtl_diagonally_linear_symbols");

    private final String pattern; // The pattern identifier for the win combination group.

    /**
     * Constructs a WinCombinationGroup enum with the specified pattern.
     *
     * @param pattern The pattern identifier for the win combination group.
     */
    WinCombinationGroup(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Converts a string representation of the win combination group into the corresponding enum.
     *
     * @param text The string representation of the win combination group.
     * @return The corresponding WinCombinationGroup enum.
     * @throws IllegalArgumentException If no corresponding win combination group is found for the given text.
     */
    public static WinCombinationGroup fromString(String text) {
        // Iterate over WinCombinationGroup values to find a match for the provided text.
        return Arrays.stream(WinCombinationGroup.values())
            .filter(b -> b.pattern.equalsIgnoreCase(text)) // Case-insensitive comparison to find the matching group.
            .findAny() // Attempt to find at least one matching group.
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No win combination group with name '%s' found", text) // Throw if no match is found.
            ));
    }
}
