# CyberSpeed Game Engine

CyberSpeed is a gaming engine designed to run probability-based board games. It utilizes a flexible configuration system allowing for a variety of game setups and win conditions.

## Components

- `GameConfig`: Holds the configuration of the game board, including matrix dimensions, probabilities, and win combinations.
- `Probability`: Defines the probability associated with each symbol on the board.
- `StandardSymbolProbability` and `BonusSymbolProbability`: Implementations of `Probability` for standard and bonus symbols respectively.
- `WinCombination`: An interface for win conditions that can be applied to a game board.
- `LinearWinCombination` and `MultiTimesWinCombination`: Concrete implementations of win conditions.
- `Board`: Represents the game board and is responsible for generating the board state based on probabilities.
- `RewardCalculator`: Calculates the reward based on the applied win combinations.
- `OutputResult`: Formats and outputs the game results.

## Running the Game

To run the game, ensure you have the correct version of Java installed (Java 8 or above). Compile the source code and execute the `ScratchGame` class with the required arguments:

```
java -jar ScratchGame.jar --config <path_to_config_file> --betting-amount <bet_amount>
```

- `<path_to_config_file>`: The path to the JSON configuration file for the game.
- `<bet_amount>`: The betting amount for the game round.

## Configuration

Game configuration is defined in a JSON file which specifies the dimensions of the game board, the symbols, their probabilities, and the win combinations.

Here's an example of a game configuration:

```json
{
  "columns": 3,
  "rows": 3,
  "symbols": {
    "A": {
      "reward_multiplier": 50,
      "type": "standard"
    },
    "B": {
      "reward_multiplier": 25,
      "type": "standard"
    },
    ...
    "10x": {
      "reward_multiplier": 10,
      "type": "bonus",
      "impact": "multiply_reward"
    },
    ...
    "+1000": {
      "extra": 1000,
      "type": "bonus",
      "impact": "extra_bonus"
    },
    ...
    "MISS": {
      "type": "bonus",
      "impact": "miss"
    }
  },
  "probabilities": {
    "standard_symbols": [
      {
        "column": 0,
        "row": 0,
        "symbols": {
          "A": 1,
          "B": 2,
          "C": 3,
          "D": 4,
          "E": 5,
          "F": 6
        }
      },
      {
        "column": 1,
        "row": 0,
        "symbols": {
          "A": 1,
          "B": 2,
          "C": 3,
          "D": 4,
          "E": 5,
          "F": 6
        }
      },
      ...
    ],
    "bonus_symbols": {
      "symbols": {
        "10x": 1,
        "5x": 2,
        "+1000": 3,
        "+500": 4,
        "MISS": 5
      }
    }
  },
  "win_combinations": {
    "same_symbol_3_times": {
      "reward_multiplier": 1,
      "when": "same_symbols",
      "count": 3,
      "group": "same_symbols"
    },
    ...
    "same_symbols_horizontally": {
      "reward_multiplier": 2,
      "when": "linear_symbols",
      "group": "horizontally_linear_symbols",
      "covered_areas": [
        ["0:0", "0:1", "0:2"],
        ["1:0", "1:1", "1:2"],
        ["2:0", "2:1", "2:2"]
      ]
    },
    ...
  }
}
```