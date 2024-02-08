package ae.cyberspeed.util;

import ae.cyberspeed.config.GameConfig;
import ae.cyberspeed.probability.BonusSymbolProbability;
import ae.cyberspeed.probability.Probability;
import ae.cyberspeed.probability.StandardSymbolProbability;
import ae.cyberspeed.symbol.BonusSymbol;
import ae.cyberspeed.symbol.StandardSymbol;
import ae.cyberspeed.symbol.Symbol;
import ae.cyberspeed.wincombination.LinearWinCombination;
import ae.cyberspeed.wincombination.MultiTimesWinCombination;
import ae.cyberspeed.wincombination.WinCombination;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom deserializer for {@link GameConfig} objects.
 * Parses the JSON configuration for a game and constructs a {@link GameConfig} object with the parsed data.
 *
 * @author Anatolii Stepanchuk
 * @version 1.0
 */
public class GameConfigDeserializer extends JsonDeserializer<GameConfig> {
    /**
     * Deserializes a JSON object into a GameConfig object, effectively translating game configuration data into a
     * structured Java object.
     * This method iterates through the JSON object provided by the JsonParser, processing each key-value pair
     * according to its relevance
     * to the game's configuration. It handles various sections of the game configuration, including the game matrix
     * dimensions (rows and columns),
     * symbol definitions, probability settings for symbol appearances, and win combination configurations.
     * <p>
     * For symbols, it delegates to a helper method that processes each symbol's attributes, such as type, impact,
     * and reward multipliers. Similarly,
     * for probabilities and win combinations, it employs specific helper methods that parse and construct the
     * corresponding objects.
     * These processed elements are then used to instantiate a GameConfig object, which encapsulates the entire
     * game setup.
     * <p>
     * It employs rigorous error handling to manage unexpected or malformed data within the JSON, ensuring the
     * integrity of the resulting GameConfig object.
     * This method is a critical component of the game's setup process, allowing for dynamic and flexible game
     * configuration through external JSON files.
     *
     * @param jp The JsonParser reading the JSON data.
     * @param ctxt The DeserializationContext providing configuration for the deserialization process.
     * @return A fully constructed GameConfig object based on the JSON input.
     * @throws IOException If an error occurs while reading from the JsonParser.
     */
    @Override
    public GameConfig deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        AtomicInteger matrixColumns = new AtomicInteger(0);
        AtomicInteger matrixRows = new AtomicInteger(0);

        // Process each key/value pair in the JSON object.
        node.fields().forEachRemaining(entry -> {
            switch (entry.getKey()) {
                case "rows" -> matrixRows.set(entry.getValue().intValue());
                case "columns" -> matrixColumns.set(entry.getValue().intValue());
                case "symbols" -> entry.getValue().fields().forEachRemaining(this::processSymbol);
                case "probabilities" -> entry.getValue().fields().forEachRemaining(probabilityListEntry -> {
                    if (probabilityListEntry.getValue().isArray()) {
                        probabilityListEntry.getValue().elements().forEachRemaining(probabilityArray -> {
                            processProbability(probabilityListEntry, probabilityArray.fields());
                        });
                    } else {
                        processProbability(probabilityListEntry, probabilityListEntry.getValue().fields());
                    }
                });
                case "win_combinations" -> entry.getValue().fields().forEachRemaining(winCombinationListEntry ->
                    processWinCombination(winCombinationListEntry, matrixRows.get(), matrixColumns.get())
                );
                default -> {
                    throw new IllegalStateException(
                        String.format(
                            "Unexpected key/value inside JSON config file: '%s': '%s'",
                            entry.getKey(),
                            entry.getValue().asText()
                        )
                    );
                }
            }
        });

        return new GameConfig(
            matrixRows.get(),
            matrixColumns.get(),
            ProbabilityParseHelper.getProbabilities(),
            WinCombinationParseHelper.getWinCombinations()
        );
    }

    /**
     * Processes a symbol configuration and updates the symbol parsing helper accordingly.
     *
     * @param symbolListEntry The entry in the JSON object representing a symbol configuration.
     * @throws IllegalStateException If an unexpected key/value is encountered in the symbol configuration.
     */
    private void processSymbol(Map.Entry<String, JsonNode> symbolListEntry) {
        SymbolParseHelper.setName(symbolListEntry.getKey());

        symbolListEntry.getValue().fields().forEachRemaining(symbolEntry -> {
            switch (symbolEntry.getKey()) {
                case "reward_multiplier" -> SymbolParseHelper.setRewardMultiplier(
                    symbolEntry.getValue().doubleValue()
                );
                case "type" -> SymbolParseHelper.setType(symbolEntry.getValue().asText());
                case "impact" -> SymbolParseHelper.setImpact(symbolEntry.getValue().asText());
                case "extra" -> SymbolParseHelper.setExtra(symbolEntry.getValue().doubleValue());
                default -> {
                    throw new IllegalStateException(
                        String.format(
                            "Unexpected key/value inside JSON config file; symbols section: '%s': '%s'",
                            symbolEntry.getKey(),
                            symbolEntry.getValue().asText()
                        )
                    );
                }
            }
        });

        SymbolParseHelper.create();
        SymbolParseHelper.reset();
    }

    /**
     * Processes a probability configuration and updates the probability parsing helper accordingly.
     *
     * @param probabilityListEntry The entry in the JSON object representing a probability configuration.
     */
    private void processProbability(Map.Entry<String, JsonNode> probabilityListEntry,
                                    Iterator<Map.Entry<String, JsonNode>> fields) {
        ProbabilityParseHelper.setType(probabilityListEntry.getKey());

        fields.forEachRemaining(probabilityEntry -> {
            switch (probabilityEntry.getKey()) {
                case "row" -> ProbabilityParseHelper.setCellRow(probabilityEntry.getValue().intValue());
                case "column" -> ProbabilityParseHelper.setCellColumn(
                    probabilityEntry.getValue().intValue()
                );
                case "symbols" -> probabilityEntry.getValue().fields().forEachRemaining(
                    symbolListEntry -> ProbabilityParseHelper.addSymbolProbability(
                        SymbolParseHelper.findByName(symbolListEntry.getKey()),
                        symbolListEntry.getValue().doubleValue()
                    )
                );
                default -> throw new IllegalStateException(String.format(
                    "Unexpected key/value inside JSON config file; probabilities section: '%s': '%s'",
                    probabilityEntry.getKey(),
                    probabilityEntry.getValue().asText()
                ));
            }
        });

        ProbabilityParseHelper.create();
        ProbabilityParseHelper.reset();
    }

    /**
     * Processes a win combination configuration and updates the win combination parsing helper accordingly.
     *
     * @param winCombinationListEntry The entry in the JSON object representing a win combination configuration.
     * @param rows The number of rows in the game matrix.
     * @param columns The number of columns in the game matrix.
     * @throws IllegalStateException If an unexpected key/value is encountered in the win combination configuration.
     */
    private void processWinCombination(Map.Entry<String, JsonNode> winCombinationListEntry, int rows, int columns) {
        WinCombinationParseHelper.setName(winCombinationListEntry.getKey());
        AtomicInteger coveredAreaRow = new AtomicInteger(0);
        AtomicInteger coveredAreaColumn = new AtomicInteger(0);

        winCombinationListEntry.getValue().fields().forEachRemaining(winCombinationEntry -> {
            switch (winCombinationEntry.getKey()) {
                case "reward_multiplier" -> WinCombinationParseHelper.setRewardMultiplier(
                    winCombinationEntry.getValue().doubleValue()
                );
                case "when" -> WinCombinationParseHelper.setCondition(
                    winCombinationEntry.getValue().textValue()
                );
                case "count" -> WinCombinationParseHelper.setCount(
                    winCombinationEntry.getValue().intValue()
                );
                case "group" -> WinCombinationParseHelper.setGroup(
                    winCombinationEntry.getValue().textValue()
                );
                case "covered_areas" -> winCombinationEntry.getValue().elements().forEachRemaining(
                    coveredAreasRowList -> {
                        int currentRow = coveredAreaRow.getAndIncrement();

                        if (currentRow == 0) {
                            WinCombinationParseHelper.setCoveredAreas(new String[rows][columns]);
                        }

                        for (JsonNode coordinatesNode : coveredAreasRowList) {
                            int currentCol = coveredAreaColumn.getAndIncrement();

                            WinCombinationParseHelper.addCoveredAreas(
                                currentRow, currentCol, coordinatesNode.textValue()
                            );
                        }

                        coveredAreaColumn.set(0);
                    }
                );
                default -> {
                    throw new IllegalStateException(
                        String.format(
                            "Unexpected key/value inside JSON config file; " +
                                "'win_combinations' section: '%s': '%s'",
                            winCombinationEntry.getKey(),
                            winCombinationEntry.getValue().asText()
                        )
                    );
                }
            }
        });

        WinCombinationParseHelper.create();
        WinCombinationParseHelper.reset();
    }


    /**
     * Assists in parsing and creating {@link Symbol} objects from JSON configuration.
     * Handles the identification and instantiation of both standard and bonus symbols.
     */
    private static class SymbolParseHelper {
        private static final List<Symbol> symbols = new ArrayList<>();
        private static String name;
        private static String type;
        private static String impact;
        private static double rewardMultiplier;
        private static double extra;

        /**
         * Resets the helper's state for the next symbol processing.
         */
        private static void reset() {
            setName(null);
            setType(null);
            setImpact(null);
            setRewardMultiplier(0D);
            setExtra(0D);
        }

        private static Symbol findByName(String name) {
            return symbols.stream()
                .filter(s -> s.name().equals(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("Symbol with name '%s' not found!", name)));
        }

        /**
         * Creates a symbol object based on the current state and adds it to the list of symbols.
         */
        private static void create() {
            switch (type) {
                case "standard" -> symbols.add(new StandardSymbol(name, rewardMultiplier));
                case "bonus" -> symbols.add(new BonusSymbol(name, rewardMultiplier, impact, extra));
                default -> throw new IllegalStateException(String.format("Unexpected symbol type: '%s'", type));
            }
        }

        private static List<Symbol> getSymbols() {
            return symbols;
        }
        
        private static void setName(String name) {
            SymbolParseHelper.name = name;
        }

        private static void setType(String type) {
            SymbolParseHelper.type = type;
        }

        private static void setImpact(String impact) {
            SymbolParseHelper.impact = impact;
        }

        private static void setRewardMultiplier(double rewardMultiplier) {
            SymbolParseHelper.rewardMultiplier = rewardMultiplier;
        }

        private static void setExtra(double extra) {
            SymbolParseHelper.extra = extra;
        }
    }

    /**
     * Assists in parsing and creating {@link Probability} objects from JSON configuration.
     * Handles the instantiation of probabilities for both standard and bonus symbols.
     */
    private static class ProbabilityParseHelper {
        private static final List<Probability> probabilities = new ArrayList<>();
        private static String type;
        private static int cellColumn;
        private static int cellRow;
        private static Map<Symbol, Double> symbolsProbability = new HashMap<>();

        private static void reset() {
            setType(null);
            setCellColumn(0);
            setCellRow(0);
            setSymbolsProbability(new HashMap<>());
        }

        private static void addSymbolProbability(Symbol symbol, double probability) {
            symbolsProbability.put(symbol, probability);
        }

        /**
         * Creates a probability object based on the current state and adds it to the list of probabilities.
         */
        private static void create() {
            switch (type) {
                case "standard_symbols" -> probabilities.add(
                    new StandardSymbolProbability(cellRow, cellColumn, symbolsProbability)
                );
                case "bonus_symbols" -> probabilities.add(new BonusSymbolProbability(symbolsProbability));
                default -> throw new IllegalStateException(
                    String.format("Unexpected symbol probability type: '%s'", type)
                );
            }
        }

        private static List<Probability> getProbabilities() {
            return probabilities;
        }

        public static void setType(String type) {
            ProbabilityParseHelper.type = type;
        }

        public static void setCellColumn(int cellColumn) {
            ProbabilityParseHelper.cellColumn = cellColumn;
        }

        public static void setCellRow(int cellRow) {
            ProbabilityParseHelper.cellRow = cellRow;
        }

        public static void setSymbolsProbability(Map<Symbol, Double> symbolsProbability) {
            ProbabilityParseHelper.symbolsProbability = symbolsProbability;
        }
    }

    /**
     * Assists in parsing and creating {@link WinCombination} objects from JSON configuration.
     * Handles the instantiation of win combinations based on the provided configuration.
     */
    private static class WinCombinationParseHelper {
        private final static List<WinCombination> winCombinations = new ArrayList<>();
        private static String name;
        private static double rewardMultiplier;
        private static int count; // required count of the same symbols to activate the reward
        private static String condition; // E.g., "same_symbols", "linear_symbols"
        private static String group; // E.g., "same_symbols", "horizontally_linear_symbols", etc
        private static String[][] coveredAreas; // Specific for linear combinations

        private static void reset() {
            setName(null);
            setRewardMultiplier(0D);
            setCount(0);
            setCondition(null);
            setGroup(null);
            setCoveredAreas(null);
        }

        private static void addCoveredAreas(int row, int column, String coordinates) {
            coveredAreas[row][column] = coordinates;
        }

        /**
         * Creates a win combination object based on the current state and adds it to the list of win combinations.
         */
        private static void create() {
            switch (condition) {
                case "same_symbols" -> winCombinations.add(
                    new MultiTimesWinCombination(name, rewardMultiplier, condition, group, count)
                );
                case "linear_symbols" -> winCombinations.add(
                    new LinearWinCombination(name, rewardMultiplier, condition, group, coveredAreas)
                );
                default -> throw new IllegalStateException(
                    String.format("Unexpected symbol win combination condition: '%s'", condition)
                );
            }
        }

        public static List<WinCombination> getWinCombinations() {
            return winCombinations;
        }

        public static void setName(String name) {
            WinCombinationParseHelper.name = name;
        }

        public static void setRewardMultiplier(double rewardMultiplier) {
            WinCombinationParseHelper.rewardMultiplier = rewardMultiplier;
        }

        public static void setCount(int count) {
            WinCombinationParseHelper.count = count;
        }

        public static void setCondition(String condition) {
            WinCombinationParseHelper.condition = condition;
        }

        public static void setGroup(String group) {
            WinCombinationParseHelper.group = group;
        }

        public static void setCoveredAreas(String[][] coveredAreas) {
            WinCombinationParseHelper.coveredAreas = coveredAreas;
        }
    }
}
