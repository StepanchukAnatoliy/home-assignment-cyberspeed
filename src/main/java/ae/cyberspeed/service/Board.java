package ae.cyberspeed.service;

import ae.cyberspeed.config.GameConfig;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.error.BoardGenerationException;
import ae.cyberspeed.util.Validation;

import java.util.Random;

/**
 * Represents the game board with a matrix of symbols based on the provided game configuration.
 * The board is generated with symbols according to their defined probabilities, which may include
 * a mix of standard and bonus symbols.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public final class Board {
    private Symbol[][] board;
    private final Random random;

    /**
     * Constructs a game board based on the given game configuration and random generator.
     *
     * @param config The game configuration to use for generating the board.
     * @param random The random generator for symbol selection.
     * @throws BoardGenerationException If the provided configuration is null or if no symbol probability matches.
     */
    public Board(GameConfig config, Random random) {
        // Ensures the game configuration is not null before generating the board.
        Validation.requireNonNull(config, new BoardGenerationException("GameConfig cannot be null"));

        // Initialize the board matrix and the random generator.
        this.board = new Symbol[config.matrixRows()][config.matrixColumns()];
        this.random = random;

        // Generate the symbols on the board based on the probabilities defined in the configuration.
        generateBoard(config);
    }

    /**
     * Generates the board with symbols according to the probabilities defined in the game configuration.
     *
     * @param config The game configuration containing the probabilities for each symbol.
     */
    private void generateBoard(GameConfig config) {
        // Loop through each cell in the board matrix and assign symbols.
        for (int row = 0; row < config.matrixRows(); row++) {
            for (int column = 0; column < config.matrixColumns(); column++) {
                // Determine if the current cell should contain a bonus symbol (10% chance).
                boolean shouldBeBonusSymbol = (random.nextInt(100) + 1 <= 10);
                int r = row, c = column; // Final variables for use in lambda expression.
                // Assign a symbol to the current cell based on the probabilities and symbol type (bonus or standard).
                board[row][column] = config.probabilities().stream()
                    .filter(p -> p.isBonus() == shouldBeBonusSymbol && p.coordinatesMatch(r, c))
                    .findAny()
                    .orElseThrow(() -> new BoardGenerationException(String.format(
                        "No symbol probability matched for board cell with coordinates: %d:%d!", r, c
                    )))
                    .defineSymbol(random);
            }
        }
        // Validates that the board has been generated correctly.
        Validation.checkBoardAfterGeneration(board);
    }

    /**
     * Retrieves the symbol at the specified cell location on the board.
     *
     * @param row The row index of the cell.
     * @param column The column index of the cell.
     * @return The symbol at the specified cell.
     */
    public Symbol getCellValue(int row, int column) {
        return board[row][column];
    }

    /**
     * Retrieves the entire matrix of symbols representing the board.
     *
     * @return The matrix of symbols.
     */
    public Symbol[][] getMatrixBoard() {
        return board;
    }

    /**
     * Sets the board with a new matrix of symbols.
     *
     * @param matrix The new matrix of symbols to set.
     */
    public void setBoard(Symbol[][] matrix) {
        this.board = matrix;
    }
}
