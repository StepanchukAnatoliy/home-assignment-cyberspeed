package ae.cyberspeed.util;

import ae.cyberspeed.ScratchGame;
import ae.cyberspeed.probability.Probability;
import ae.cyberspeed.probability.StandardSymbolProbability;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.error.BoardGenerationException;
import ae.cyberspeed.util.error.GameConfigException;
import ae.cyberspeed.util.error.ProbabilityException;
import org.apache.maven.surefire.shared.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for validating various aspects of game configuration and runtime parameters.
 * This includes validation of input arguments, probability values, board dimensions, and more.
 * It is designed to ensure that the game operates within the defined rules and constraints.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public final class Validation {
    /**
     * Validates the input arguments provided to the program and identifies the configuration file path.
     * It ensures the correct number and format of arguments are provided, specifically looking for the
     * configuration file path and betting amount.
     *
     * @param args The command line arguments passed to the program.
     * @return The method of finding the configuration file, either through the file system or classpath.
     * @throws InvalidPathException if the specified path to the configuration file is invalid.
     * @throws IllegalArgumentException if the arguments do not meet the expected format or values.
     */
    public static String checkInputArgumentsAndWhereFindConfigFile(String[] args) throws InvalidPathException {
        // Assuming the first two arguments is the JSON config file path and the second two parameters
        // is the betting amount
        if (args.length < 4) {
            throw new IllegalArgumentException(
                """
                Usage:
                java -jar <your-jar-file> --config <config file> --betting-amount <betting amount>
                
                For example:
                java -jar <your-jar-file> --config config.json --betting-amount 100"""
            );
        }

        // Initialize variables to store the path to the configuration file and the betting amount.
        String configFilePath = null;
        double bettingAmount = 0D;

        // Loop through the command-line arguments to parse and validate the configuration file path and
        // betting amount.
        for (int i = 0; i < args.length; i++) {
            // Determine the type of argument (config file path or betting amount) and process accordingly.
            switch (args[i]) {
                // If the "--config" argument is found, attempt to parse the following argument as the config
                // file path.
                case "--config" -> {
                    // Ensure there is an argument following "--config" to use as the config file path.
                    if (i + 1 < args.length) {
                        // Increment 'i' to skip to the next argument, treating it as the config file path.
                        configFilePath = args[++i];
                    // If "--config" is the last argument without a following path, throw an exception.
                    } else {
                        throw new IllegalArgumentException("Please specify a config file path after --config");
                    }
                }
                // If the "--betting-amount" argument is found, attempt to parse the following argument as the
                // betting amount.
                case "--betting-amount" -> {
                    // Ensure there is an argument following "--betting-amount" to use as the betting amount.
                    if (i + 1 < args.length) {
                        try {
                            // Increment 'i' to skip to the next argument and try to parse it as a double for the
                            // betting amount.
                            bettingAmount = Double.parseDouble(args[++i]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Please specify a valid betting amount after --betting-amount");
                        }
                    // If "--betting-amount" is the last argument without a following amount, throw an exception.
                    } else {
                        throw new IllegalArgumentException("Please specify a betting amount after --betting-amount");
                    }
                }
                // If an argument is encountered that does not match expected options, throw an exception.
                default -> throw new IllegalArgumentException("Unexpected argument: " + args[i]);
            }
        }

        // Validate that the config file path has been set and is not blank.
        if (configFilePath == null || configFilePath.isBlank()) {
            throw new IllegalArgumentException("Please specify a config file path after --config");
        }

        // Validate that the betting amount has been set to a positive value.
        if (bettingAmount <= 0D) {
            throw new IllegalArgumentException("Please specify a correct betting amount after --betting-amount");
        }

        // Attempt to read the config file from the specified path or, if that fails, check the classpath.
        try {
            Files.readAllBytes(Paths.get(Objects.requireNonNull(configFilePath)));
            return "ArbitraryFilePath";

        } catch (InvalidPathException | IOException ex) {
            // If reading from the filesystem fails, attempt to find the config file in the classpath.
            try (InputStream inpStreamForFileInClassloaderPath = ScratchGame.class.getClassLoader().getResourceAsStream(
                configFilePath
            );

            ) {
                if (inpStreamForFileInClassloaderPath != null) {
                    return "ClassloaderPath";
                }
            } catch (Exception e) {
                // If both attempts fail, throw an exception indicating the problem with reading the config file.
                throw new IllegalArgumentException(String.format(
                    "Problems with reading config file at directory: %s. An exception caught: %s%n",
                    configFilePath, e.getMessage()
                ));
            }

        }
        return "";
    }

    /**
     * Validates the probabilities associated with each symbol to ensure they are within acceptable bounds.
     * Specifically, it checks that the map is not null or empty and that all probabilities are non-negative.
     *
     * @param symbolsProbability The map of symbols to their associated probabilities.
     * @throws ProbabilityException if the probability map is null, empty, or contains invalid values.
     */
    public static void validateSymbolsProbability(Map<Symbol, Double> symbolsProbability) {
        // Check if the symbolsProbability map is either null or empty. If it is, throw a ProbabilityException
        // indicating that a non-null and non-empty map is required.
        if (symbolsProbability == null || symbolsProbability.isEmpty()) {
            throw new ProbabilityException("Symbol probability map cannot be null or empty.");
        }

        // Stream through the values of the symbolsProbability map, filter for any probability values that are
        // less than 0 (indicating an invalid probability), and find any occurrence. If any invalid probability
        // value is found, throw a ProbabilityException with the invalid value indicated in the message.
        symbolsProbability.values().stream()
            .filter(probability -> probability < 0)
            .findAny()
            .ifPresent(probability -> {
                throw new ProbabilityException("Invalid probability value: " + probability);
            });
    }

    /**
     * Validates that symbol coordinates are not negative.
     *
     * @param row The row index of the symbol.
     * @param column The column index of the symbol.
     * @throws ProbabilityException If either the row or column index is negative.
     */
    public static void checkSymbolCoordinatesIsNegative(int row, int column) {
        if (row < 0 || column < 0) {
            throw new ProbabilityException("The standard symbol probability coordinates (row or column) are negative.");
        }
    }

    /**
     * Validates that the size of the game matrix is positive.
     *
     * @param matrixRows The number of rows in the matrix.
     * @param matrixColumns The number of columns in the matrix.
     * @throws GameConfigException If either the number of rows or columns is non-positive.
     */
    public static void checkMatrixSizeIsNegative(int matrixRows, int matrixColumns) {
        if (matrixRows <= 0) {
            throw new GameConfigException("The number of matrix rows must be positive, but was " + matrixRows);
        }
        if (matrixColumns <= 0) {
            throw new GameConfigException("The number of matrix columns must be positive, but was " + matrixColumns);
        }
    }

    /**
     * Ensures that a given collection is neither null nor empty.
     *
     * @param collection The collection to check.
     * @param e The exception to throw if the check fails.
     * @throws RuntimeException The specified exception if the collection is null or empty.
     */
    public static void requireNonEmptyCollection(Collection<?> collection, RuntimeException e) {
        if (collection == null || collection.isEmpty()) {
            throw e;
        }
    }

    /**
     * Ensures that an object is not null.
     *
     * @param obj The object to check.
     * @param e The exception to throw if the check fails.
     * @throws RuntimeException The specified exception if the object is null.
     */
    public static void requireNonNull(Object obj, RuntimeException e) {
        if (obj == null) {
            throw e;
        }
    }

    /**
     * Checks if the generated game board has positive dimensions.
     *
     * @param board The game board to check.
     * @throws BoardGenerationException If the board dimensions are not positive.
     */
    public static void checkBoardAfterGeneration(Symbol[][] board) {
        if (board.length == 0 || board[0].length == 0) {
            throw new BoardGenerationException("Board matrix dimensions must be positive");
        }
    }

    /**
     * Checks if a given value is negative.
     *
     * @param value The value to check.
     * @param e The exception to throw if the value is negative.
     * @throws RuntimeException The specified exception if the value is negative.
     */
    public static void checkIsNegative(double value, RuntimeException e) {
        if (value < 0) {
            throw e;
        }
    }

    /**
     * Checks if a given value is negative or zero.
     *
     * @param value The value to check.
     * @param e The exception to throw if the value is negative or zero.
     * @throws RuntimeException The specified exception if the value is negative or zero.
     */
    public static void checkIsNegativeOrZero(double value, RuntimeException e) {
        if (value <= 0D) {
            throw e;
        }
    }

    /**
     * Checks if a string is null or blank.
     *
     * @param string The string to check.
     * @param e The exception to throw if the string is null or blank.
     * @throws RuntimeException The specified exception if the string is null or blank.
     */
    public static void checkIsBlankString(String string, RuntimeException e) {
        if (string == null || string.isBlank()) {
            throw e;
        }
    }

    /**
     * Checks if two values are not both positive.
     *
     * @param v1 The first value to check.
     * @param v2 The second value to check.
     * @param e The exception to throw if both values are positive.
     * @throws RuntimeException The specified exception if both values are positive.
     */
    public static void checkTwoValuesPositive(double v1, double v2, RuntimeException e) {
        if (v1 > 0D && v2 > 0D) {
            throw e;
        }
    }

    /**
     * Validates that all specified probability coordinates are within the bounds of the game matrix.
     * It also identifies any missing probability coordinates within the matrix.
     *
     * @param probabilities The list of probabilities to check.
     * @param matrixRows The number of rows in the matrix.
     * @param matrixColumns The number of columns in the matrix.
     * @throws ProbabilityException If any probability coordinate is out of bounds or missing.
     */
    public static void checkProbabilityCoordinatesWithMatrix(List<Probability> probabilities,
                                                             int matrixRows,
                                                             int matrixColumns) {
        // First, ensure all provided probability coordinates for standard symbols (non-bonus symbols)
        // are valid within the matrix dimensions. Specifically, this checks that the row and column
        // indices for each probability are within the bounds defined by matrixRows and matrixColumns.
        probabilities.stream()
            // Exclude bonus symbols from this validation.
            .filter(p -> !p.isBonus())
            // Cast to StandardSymbolProbability for access to row and column properties.
            .map(p -> (StandardSymbolProbability) p)
            // Identify out-of-bounds probabilities.
            .filter(p -> p.row() < 0 || p.row() >= matrixRows || p.column() < 0 || p.column() >= matrixColumns)
            .findAny()
            .ifPresent(p -> {
                // If an out-of-bounds probability is found, throw an exception detailing the invalid coordinate.
                throw new ProbabilityException(String.format("Probability coordinate out of bounds: %s", p));
            });

        // Next, identify any coordinates in the matrix that do not have an associated probability defined.
        // This helps ensure that all parts of the matrix are covered by the probability configuration.
        String coordinateGaps = IntStream.range(0, matrixRows).boxed()
            // Create pairs for all matrix cells.
            .flatMap(row -> IntStream.range(0, matrixColumns).mapToObj(column -> new ImmutablePair<>(row, column)))
            .filter(pair -> probabilities.stream()
                .filter(p -> !p.isBonus())
                // Again, focus on standard symbols.
                .map(p -> (StandardSymbolProbability) p)
                // Check if the pair lacks a matching probability.
                .noneMatch(p -> p.row() == pair.getLeft() && p.column() == pair.getRight()))
            // Convert missing coordinates to string representation.
            .map(pair -> pair.left + ":" + pair.right)
            // Join the missing coordinates in a comma-separated string.
            .collect(Collectors.joining(", "));

        // If there are any coordinates without defined probabilities, throw an exception listing them.
        // This alerts to gaps in the probability configuration where some matrix cells are not covered.
        if (!coordinateGaps.isBlank()) {
            throw new ProbabilityException(
                String.format("Probability coordinates not provided for matrix cells: %s", coordinateGaps)
            );
        }
    }
}
