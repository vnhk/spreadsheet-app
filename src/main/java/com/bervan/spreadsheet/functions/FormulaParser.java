package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FormulaParser {
    private final CellResolver cellResolver;
    private final FunctionRegistry functionRegistry;

    public FormulaParser(FunctionRegistry functionRegistry, CellResolver cellResolver) {
        this.functionRegistry = functionRegistry;
        this.cellResolver = cellResolver;
    }

    public FunctionArgument evaluate(String formula, List<SpreadsheetRow> rows) {
        if (!formula.startsWith("=")) {
            throw new IllegalArgumentException("Not a formula: " + formula);
        }

        // Remove '=' and extract function name and arguments
        String content = removeEquals(formula);
        int parenIndex = getParenIndex(formula, content);
        String functionName = getFunctionName(content, parenIndex);
        String argsString = getArgsString(content, parenIndex); // between (...)

        List<FunctionArgument> args = parseArguments(argsString, rows);
        SpreadsheetFunction function = functionRegistry.getFunction(functionName)
                .orElseThrow(() -> {
                    log.error("Unknown function: {}", functionName);
                    return new IllegalArgumentException("Unknown function: " + functionName);
                });

        return function.calculate(args);
    }

    public List<FunctionArgument> extractFunctionArguments(String formula, List<SpreadsheetRow> rows) {
        if (!formula.startsWith("=")) {
            throw new IllegalArgumentException("Not a formula: " + formula);
        }

        // Remove '=' and extract function name and arguments
        String content = removeEquals(formula);
        int parenIndex = getParenIndex(formula, content);
        String argsString = getArgsString(content, parenIndex); // between (...)

        return parseArguments(argsString, rows);
    }

    private int getParenIndex(String formula, String content) {
        int parenIndex = content.indexOf('(');
        if (parenIndex == -1 || !content.endsWith(")")) {
            throw new IllegalArgumentException("Invalid formula syntax: " + formula);
        }
        return parenIndex;
    }

    private String removeEquals(String formula) {
        // e.g. SUM(1, 2)
        return formula.substring(1).trim();
    }

    private String getArgsString(String content, int parenIndex) {
        return content.substring(parenIndex + 1, content.length() - 1);
    }

    private String getFunctionName(String content, int parenIndex) {
        return content.substring(0, parenIndex).trim();
    }

    private List<FunctionArgument> parseArguments(String argsString, List<SpreadsheetRow> rows) {
        if (argsString.isBlank()) {
            return Collections.emptyList();
        }

        if (argsString.contains(":")) {
            String[] args = argsString.split(":");
            if (args.length == 2) {
                String startCell = args[0];
                String endCell = args[1];

                int startRowNumberFromColumn = SpreadsheetUtils.getRowNumberFromColumn(startCell);
                int endRowNumberFromColumn = SpreadsheetUtils.getRowNumberFromColumn(endCell);
                String startColumnHeader = SpreadsheetUtils.getColumnHeader(startCell);
                String endColumnHeader = SpreadsheetUtils.getColumnHeader(endCell);

                int startColumnNumber = SpreadsheetUtils.getColumnNumber(startColumnHeader);
                int endColumnNumber = SpreadsheetUtils.getColumnNumber(endColumnHeader);

                if (endColumnNumber < startColumnNumber) {
                    throw new RuntimeException("Parse Arguments failed for: " + argsString + ". Incorrect colon expression");
                }

                if (startRowNumberFromColumn == endRowNumberFromColumn) {
                    //1 row - multiple columns
                    int maxColumn = rows.get(0).getCells().stream().max(Comparator.comparingInt(SpreadsheetCell::getColumnNumber)).get().getColumnNumber();
                    List<FunctionArgument> functionArguments = new ArrayList<>();
                    for (int i = startColumnNumber; i <= Math.min(maxColumn, endColumnNumber); i++) {
                        functionArguments.add(toArgument(SpreadsheetUtils.getColumnHeader(i) + startRowNumberFromColumn, rows));
                    }
                    return functionArguments;
                } else {
                    //1 column - multiple rows
                    if (!startColumnHeader.equals(endColumnHeader)) {
                        throw new RuntimeException("Parse Arguments failed for: " + argsString + ". Incorrect colon expression");
                    }
                    List<FunctionArgument> functionArguments = new ArrayList<>();
                    int maxRow = rows.size();
                    for (int i = startRowNumberFromColumn; i <= Math.min(maxRow, endRowNumberFromColumn); i++) {
                        functionArguments.add(toArgument(startColumnHeader + i, rows));
                    }
                    return functionArguments;
                }
            } else {
                throw new RuntimeException("Parse Arguments failed for: " + argsString + ". Incorrect colon expression");
            }

        } else {
            return Arrays.stream(argsString.split(","))
                    .map(String::trim)
                    .map(e -> toArgument(e, rows))
                    .collect(Collectors.toList());
        }
    }

    private FunctionArgument toArgument(String raw, List<SpreadsheetRow> rows) {
        if (raw.matches("[A-Z]+\\d+")) {
            try {
                return cellResolver.resolve(raw, rows); // ex. "A1"
            } catch (IllegalArgumentException e) {
                //create empty argument
                log.warn("Cell {} is not present. Default empty virtual cell is created.", raw);
                return new CellReferenceArgument(new SpreadsheetCell(SpreadsheetUtils.getRowNumberFromColumn(raw), SpreadsheetUtils.getColumnNumber(SpreadsheetUtils.getColumnHeader(raw)), ""));
            } catch (Exception e) {
                log.error("Failed to resolve cell: " + raw, e);
                throw e;
            }
        }

        try {
            return new ConstantArgument(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            return new ConstantArgument(raw); // string
        }
    }
}
