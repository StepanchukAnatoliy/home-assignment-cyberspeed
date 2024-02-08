package ae.cyberspeed;

import ae.cyberspeed.config.GameConfig;
import ae.cyberspeed.service.Board;
import ae.cyberspeed.service.OutputResult;
import ae.cyberspeed.service.RewardCalculator;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.util.Validation;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main class for running the ScratchGame, which includes loading game configuration,
 * generating a game board, applying win conditions, calculating rewards, and displaying results.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public class ScratchGame {
    public static void main(String[] args) {
        // Validate input arguments and determine the location type of the configuration file.
        String whereFindConfigFile = Validation.checkInputArgumentsAndWhereFindConfigFile(args);

        // Initialize variables for storing the configuration file path and the betting amount.
        String configFilePath = null;
        double bettingAmount = 0D;

        // Process and extract the configuration file path and betting amount from command-line arguments.
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                // Assign the next argument as the config file path.
                case "--config" -> configFilePath = args[++i];
                // Parse the next argument as the betting amount.
                case "--betting-amount" -> bettingAmount = Double.parseDouble(args[++i]);
            }
        }

        try {
            // Create an ObjectMapper instance for deserializing the game configuration.
            ObjectMapper objectMapper = new ObjectMapper();
            GameConfig gameConfig;

            // Load the game configuration from the specified file path or classpath.
            if (whereFindConfigFile.equals("ArbitraryFilePath")) {
                // Load configuration from the filesystem.
                gameConfig = objectMapper.readValue(
                    new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(configFilePath)))),
                    GameConfig.class
                );
            } else {
                // Load configuration from the classpath.
                gameConfig = objectMapper.readValue(
                    new InputStreamReader(Objects.requireNonNull(
                        ScratchGame.class.getClassLoader().getResourceAsStream(configFilePath)
                    )),
                    GameConfig.class
                );
            }

            // Initialize the game board with the loaded configuration.
            Board board = new Board(gameConfig, new Random());

            // Apply win conditions to the board and collect any winning combinations.
            Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
            gameConfig.winCombinations().forEach(wc -> wc.applyWinCondition(board, appliedWinCombinations));

            // Calculate the total reward based on the applied win combinations and betting amount.
            double reward = RewardCalculator.calculateReward(bettingAmount, board, appliedWinCombinations);

            // Output the game results, including the board state, total reward, and applied win combinations.
            System.out.println(new OutputResult(board, reward, appliedWinCombinations).print());

        } catch (Exception e) {
            // Print the stack trace for any exceptions encountered during game setup or execution.
            e.printStackTrace();
        }
    }
}
