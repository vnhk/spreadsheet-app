package com.bervan.spreadsheet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bervan.spreadsheet.utils.SpreadsheetUtils.getColumnHeader;
import static com.bervan.spreadsheet.utils.SpreadsheetUtils.getColumnIndex;

public class Cell {
    public String columnSymbol;
    public Integer columnNumber;
    public Integer rowNumber;
    public String cellId;
    //displayValue
    public String value = "";
    //internalValueIfFunction to be visible on click
    public String functionName;
    private String functionValue;
    public boolean isFunction;
    private Map<Integer, String> relatedCells = new HashMap<>();

    public Cell() {

    }

    public Cell(String value, int columnNumber, int rowNumber) {
        if (value == null) {
            value = "";
        }
        this.value = value;
        this.cellId = getColumnHeader(columnNumber) + rowNumber;
        this.columnSymbol = getColumnHeader(columnNumber);
        this.columnNumber = columnNumber;
        this.rowNumber = rowNumber;

        if (value.startsWith("=")) {
            buildFunction(value);
        }
    }

    public List<String> getRelatedCellsId() {
        List<String> result = new ArrayList<>();
        //order is important
        int size = relatedCells.size();
        for (int i = 0; i < size; i++) {
            result.add(relatedCells.get(i));
        }

        return result;
    }

    public String getFunctionValue() {
        String returnFunctionValue = functionValue;

        for (int i = 0; i < relatedCells.size(); i++) {
            String param = "<#" + i + ">";
            if (returnFunctionValue.contains(param)) {
                returnFunctionValue = returnFunctionValue.replace(param, relatedCells.get(i));
            }
        }

        return returnFunctionValue;
    }

    public void buildFunction(String value) {
        try {
            relatedCells = new HashMap<>();
            isFunction = true;
            functionValue = value;
            functionName = value.split("=")[1].split("\\(")[0];
            String toParseRelated = value.split("=")[1].split("\\(")[1];

            if (toParseRelated.contains(",")) {
                //comma separated cells
                commaSeparatedCells(toParseRelated);
            } else if (toParseRelated.contains(":")) {
                //range cells
                colonSeparatedCells(toParseRelated);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.value = "ERROR";
            this.functionValue = "ERROR";
        }
    }

    private void commaSeparatedCells(String toParseRelated) {
        String[] split = toParseRelated.split(",");
        for (int i = 0; i < split.length; i++) {
            String param = split[i];
            if (param.endsWith(")")) {
                param = param.substring(0, param.length() - 1);
            }
            putParam(i, param);
            functionValue = saveFunctionValueParamChange(param, i, i);
        }
    }

    private String saveFunctionValueParamChange(String param, int variableOrderInFunction, int resultVariableIndex) {
        try {
            String[] functionInParts = null;
            String delimiter = "";
            if (functionValue.contains(",")) {
                functionInParts = functionValue.split(",");
                delimiter = ",";
            } else if (functionValue.contains(":")) {
                functionInParts = functionValue.split(":");
                delimiter = ":";
            }

            if (functionInParts != null) {
                String newPart = functionInParts[variableOrderInFunction].replace(param, "<#" + resultVariableIndex + ">");

                List<String> newParts = new ArrayList<>();

                for (int i = 0; i < functionInParts.length; i++) {
                    if (i == variableOrderInFunction) {
                        newParts.add(newPart);
                    } else {
                        newParts.add(functionInParts[i]);
                    }
                }

                return String.join(delimiter, newParts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }


        return "";
    }

    private void putParam(int i, String param) {
        if (param.contains("<#")) {
//            throw new RuntimeException("INCORRECT SPREADSHEET FUNCTION PARAM");
        }
        relatedCells.put(i, param);
    }

    private void colonSeparatedCells(String toParseRelated) {
        String[] split = toParseRelated.split(":");

        List<String> ranges = new ArrayList<>();
        for (String s : split) {
            if (s.endsWith(")")) {
                s = s.substring(0, s.length() - 1);
            }

            ranges.add(s);
        }

        if (ranges.size() != 2) {
            throw new RuntimeException("Invalid usage of colon separated values!");
        }

        //only 1 row or only 1 column
        String columnOne = ranges.get(0).replaceAll("^([A-Za-z]+).*", "$1");
        String columnTwo = ranges.get(1).replaceAll("^([A-Za-z]+).*", "$1");

        if (columnTwo.equals(columnOne)) {
            //1 column, multiple rows
            Integer start = Integer.parseInt(ranges.get(0).replaceAll(".*?(\\d+)$", "$1"));
            Integer end = Integer.parseInt(ranges.get(1).replaceAll(".*?(\\d+)$", "$1"));

            if (start > end) {
                throw new RuntimeException("Invalid usage of colon separated values!");
            }

            int i = 0;
            for (; start <= end; start++) {
                String param = columnOne + start;
                putParam(i, param);
                i++;
            }

            int size = relatedCells.size();
            String rangeStart = relatedCells.get(0);
            String rangeEnd = relatedCells.get(size - 1);
            functionValue = saveFunctionValueParamChange(rangeStart, 0, 0);
            functionValue = saveFunctionValueParamChange(rangeEnd, 1, size - 1);


        } else {
            //1 row, multiple columns
            List<String> cells = generateRange(ranges.get(0), ranges.get(1));
            for (int i = 0; i < cells.size(); i++) {
                String param = cells.get(i);
                putParam(i, param);
                functionValue = saveFunctionValueParamChange(param, i, i);
            }
        }
    }

    private List<String> generateRange(String start, String end) {
        List<String> result = new ArrayList<>();

        // Extract the initial letter and the constant number part
        String startLetter = start.replaceAll("\\d", "");
        String endLetter = end.replaceAll("\\d", "");
        int startIndex = getColumnIndex(startLetter);
        int endIndex = getColumnIndex(endLetter);
        String numberPart = start.replaceAll("\\D", "");

        // Generate range from startIndex to endIndex
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(getColumnHeader(i) + numberPart);
        }

        return result;
    }

    public void refreshFunction() {
        if (isFunction) {
            buildFunction(getFunctionValue());
        }
    }

    public void updateFunctionRelatedCell(String relatedCell, String newRelatedCell) {
        for (Map.Entry<Integer, String> integerStringEntry : relatedCells.entrySet()) {
            if (integerStringEntry.getValue().equals(relatedCell)) {
                integerStringEntry.setValue(newRelatedCell);
            }
        }
    }
}
