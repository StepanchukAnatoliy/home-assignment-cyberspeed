package ae.cyberspeed.integration;

import ae.cyberspeed.config.GameConfig;
import ae.cyberspeed.service.Board;
import ae.cyberspeed.service.OutputResult;
import ae.cyberspeed.service.RewardCalculator;
import ae.cyberspeed.symbol.BonusSymbol;
import ae.cyberspeed.symbol.StandardSymbol;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.wincombination.WinCombination;
import ae.cyberspeed.wincombination.WinCombinationGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for the ScratchGame, testing various scenarios including wins with mixed combinations and bonus symbols,
 * wins with multiplier bonus symbols, and lost game scenarios.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public class AcceptanceScenarioTest {
    /**
     * Tests a game scenario where a mix of win combinations and a bonus symbol results in a win.
     *
     * @throws IOException If there is an error reading the game configuration.
     */
    @Test
    public void testWonMixCombinationsAndBonus() throws IOException {
        // Load game configuration from a JSON file.
        String configFilePath = "testConfig1.json";
        double bettingAmount = 100.0;

        // Initialize ObjectMapper for JSON deserialization.
        ObjectMapper objectMapper = new ObjectMapper();
        GameConfig gameConfig;

        // Deserialize the game configuration from the classpath resource.
        gameConfig = objectMapper.readValue(
            new InputStreamReader(Objects.requireNonNull(
                AcceptanceScenarioTest.class.getClassLoader().getResourceAsStream(configFilePath)
            )),
            GameConfig.class
        );

        // Initialize the game board with the loaded configuration.
        Board board = new Board(gameConfig, new Random());

        // Manually set the board with a predefined arrangement of symbols, including a bonus symbol.
        Symbol symbolA = new StandardSymbol("A", 5);
        Symbol symbolB = new StandardSymbol("B", 3);
        Symbol bonusSymbol = new BonusSymbol("+1000", 0D, "extra_bonus", 1000);

        // Set a specific board state to test against.
        board.setBoard(new Symbol[][] {
            { symbolA, symbolA, symbolB },
            { symbolA, bonusSymbol, symbolB },
            { symbolA, symbolA, symbolB }
        });

        // Apply win conditions based on the game configuration and the current board state.
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
        gameConfig.winCombinations().forEach(wc -> wc.applyWinCondition(board, appliedWinCombinations));

        // Calculate the total reward based on applied win combinations and the bonus symbol.
        double reward = RewardCalculator.calculateReward(bettingAmount, board, appliedWinCombinations);

        // Verify the outcome matches the expected result, including the reward and applied win combinations.
        assertEquals(
            """
            {
              "matrix" : [
                [ "A", "A", "B" ],
                [ "A", "+1000", "B" ],
                [ "A", "A", "B" ]
              ],
              "reward" : 6600.0,
              "applied_winning_combinations" : {
                "A" : [ "same_symbol_5_times", "same_symbols_vertically" ],
                "B" : [ "same_symbol_3_times", "same_symbols_vertically" ]
              },
              "applied_bonus_symbol" : [ "+1000" ]
            }""",
            new OutputResult(board, reward, appliedWinCombinations).print()
        );
    }

    /**
     * Tests the scenario where the game is won with a bonus symbol that multiplies the reward by 10x.
     * This test loads a specific game configuration, sets a predefined board state with a mix of symbols
     * including a 10x bonus symbol, and verifies that the calculated reward is as expected.
     */
    @Test
    public void testWonWith10x() throws IOException {
        // Path to the game configuration file for this test scenario.
        String configFilePath = "testConfig2.json";
        // The amount bet by the player, used in reward calculation.
        double bettingAmount = 100.0;

        // Initialize ObjectMapper to deserialize JSON configuration into GameConfig object.
        ObjectMapper objectMapper = new ObjectMapper();
        GameConfig gameConfig;

        // Load game configuration from a JSON file located in the resources directory.
        gameConfig = objectMapper.readValue(
            new InputStreamReader(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(configFilePath)
            )),
            GameConfig.class
        );

        // Create a new game board using the loaded configuration and a random number generator.
        Board board = new Board(gameConfig, new Random());

        // Define symbols, including a bonus symbol that applies a 10x multiplier to the reward.
        Symbol symbolA = new StandardSymbol("A", 50);
        Symbol symbolB = new StandardSymbol("B", 25);
        Symbol symbolC = new StandardSymbol("C", 10);
        Symbol symbolD = new StandardSymbol("D", 5);
        Symbol symbolE = new StandardSymbol("E", 3);
        Symbol symbolF = new StandardSymbol("F", 1.5);
        Symbol bonusSymbol = new BonusSymbol("10x", 10D, "multiply_reward", 0D);

        // Manually set the board to a specific state for this test scenario.
        board.setBoard(new Symbol[][] {
            { symbolA, symbolB, symbolC },
            { symbolE, symbolB, bonusSymbol },
            { symbolF, symbolD, symbolB }
        });

        // Apply win conditions based on the configured win combinations to the board state.
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
        gameConfig.winCombinations().forEach(wc -> wc.applyWinCondition(board, appliedWinCombinations));

        // Calculate the total reward based on the bet amount, board state, and applied win combinations.
        double reward = RewardCalculator.calculateReward(bettingAmount, board, appliedWinCombinations);

        // Assert that the calculated reward matches the expected outcome.
        assertEquals(
            """
            {
              "matrix" : [
                [ "A", "B", "C" ],
                [ "E", "B", "10x" ],
                [ "F", "D", "B" ]
              ],
              "reward" : 50000.0,
              "applied_winning_combinations" : {
                "B" : [ "same_symbol_3_times" ]
              },
              "applied_bonus_symbol" : [ "10x" ]
            }""",
            new OutputResult(board, reward, appliedWinCombinations).print()
        );
    }

    /**
     * Integration test for scenarios where the game results in a loss, ensuring that game logic correctly identifies
     * non-winning situations even when bonus symbols are present on the board.
     */
    @Test
    public void testLostGame() throws IOException {
        // Path to the game configuration file for this test scenario.
        String configFilePath = "testConfig3.json";
        // The amount bet by the player, used in reward calculation.
        double bettingAmount = 100.0;

        // Initialize ObjectMapper for JSON deserialization.
        ObjectMapper objectMapper = new ObjectMapper();
        GameConfig gameConfig;

        // Load game configuration from a JSON file located in the resources directory.
        gameConfig = objectMapper.readValue(
            new InputStreamReader(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(configFilePath)
            )),
            GameConfig.class
        );

        // Create a new game board using the loaded configuration and a random number generator.
        Board board = new Board(gameConfig, new Random());

        // Define symbols, including a bonus symbol that multiplies the reward by 5x.
        Symbol symbolA = new StandardSymbol("A", 50);
        Symbol symbolB = new StandardSymbol("B", 25);
        Symbol symbolC = new StandardSymbol("C", 10);
        Symbol symbolD = new StandardSymbol("D", 5);
        Symbol symbolE = new StandardSymbol("E", 3);
        Symbol symbolF = new StandardSymbol("F", 1.5);
        Symbol bonusSymbol = new BonusSymbol("5x", 5D, "multiply_reward", 0D);

        // Manually set the board to a specific state for this test scenario.
        board.setBoard(new Symbol[][] {
            { symbolA, symbolB, symbolC },
            { symbolE, symbolB, bonusSymbol },
            { symbolF, symbolD, symbolC }
        });

        // Apply win conditions based on the configured win combinations to the board state.
        Map<Symbol, Map<WinCombinationGroup, Queue<WinCombination>>> appliedWinCombinations = new HashMap<>();
        gameConfig.winCombinations().forEach(wc -> wc.applyWinCondition(board, appliedWinCombinations));

        // Calculate the total reward based on the bet amount, board state, and applied win combinations.
        double reward = RewardCalculator.calculateReward(bettingAmount, board, appliedWinCombinations);

        // Assert that the calculated reward matches the expected outcome of 0.0, indicating a lost game.
        assertEquals(
            """
            {
              "matrix" : [
                [ "A", "B", "C" ],
                [ "E", "B", "5x" ],
                [ "F", "D", "C" ]
              ],
              "reward" : 0.0
            }""",
            new OutputResult(board, reward, appliedWinCombinations).print()
        );
    }
}
