package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

public class SpreadsheetUtils {
    /** Sort selected column values within an inclusive, zero-based row-index range.
     * Cell coordinates and unselected columns stay in place; formula references are not rebased.
     */
    public static UtilsMessage sortColumns(List<SpreadsheetRow> spreadsheetRows, String sortColumn, String order, String columnsToBeSorted, String rows) {
        UtilsMessage result = new UtilsMessage();
        try {
            if (!"Ascending".equals(order) && !"Descending".equals(order)) {
                throw new IllegalArgumentException("Sort order must be Ascending or Descending");
            }
            int key = columnIndex(sortColumn);
            int[] columns = Arrays.stream(columnsToBeSorted.split(",", -1))
                    .mapToInt(SpreadsheetUtils::columnIndex).distinct().toArray();
            if (Arrays.stream(columns).noneMatch(column -> column == key)) {
                throw new IllegalArgumentException("The sort column must be included in the selected columns");
            }
            if (rows == null || !rows.matches("[0-9]+:[0-9]+")) {
                throw new IllegalArgumentException("Row range must have the form start:end");
            }
            String[] range = rows.split(":");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            if (spreadsheetRows == null || start > end || end >= spreadsheetRows.size()) {
                throw new IllegalArgumentException("Row range is outside the spreadsheet");
            }
            // Validate and snapshot every source value before mutating any destination.
            List<List<Object>> values = new ArrayList<>();
            List<Object> keys = new ArrayList<>();
            for (int row = start; row <= end; row++) {
                SpreadsheetRow source = spreadsheetRows.get(row);
                List<Object> selected = new ArrayList<>();
                for (int column : columns) {
                    SpreadsheetCell cell = source.getCell(column);
                    selected.add(cell.hasFormula() ? cell.getFormula() : cell.getValue());
                }
                values.add(selected);
                keys.add(source.getCell(key).getValue());
            }
            List<Integer> sorted = new ArrayList<>(IntStream.range(0, values.size()).boxed().toList());
            Comparator<Integer> comparator = (a, b) -> compareValues(keys.get(a), keys.get(b));
            sorted.sort("Descending".equals(order) ? comparator.reversed() : comparator);
            for (int row = 0; row < sorted.size(); row++) {
                List<Object> source = values.get(sorted.get(row));
                for (int column = 0; column < columns.length; column++) {
                    spreadsheetRows.get(start + row).getCell(columns[column])
                            .setNewValueAndCellRelatedFields(source.get(column));
                }
            }
            result.isSuccess = true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            result.isError = true;
            result.message = "Cannot sort selected cells: " + e.getMessage();
        }
        return result;
    }

    private static int columnIndex(String column) {
        if (column == null || !column.trim().matches("[A-Z]+")) {
            throw new IllegalArgumentException("Column names must contain uppercase letters");
        }
        int index = getColumnNumber(column.trim()) - 1;
        if (index < 0) {
            throw new IllegalArgumentException("Column is outside the spreadsheet");
        }
        return index;
    }

    private static int compareValues(Object left, Object right) {
        if (left == null || right == null) {
            return left == right ? 0 : left == null ? -1 : 1;
        }
        BigDecimal leftNumber = numericValue(left);
        BigDecimal rightNumber = numericValue(right);
        if (leftNumber != null && rightNumber != null) {
            return leftNumber.compareTo(rightNumber);
        }
        if (leftNumber != null || rightNumber != null) {
            return leftNumber != null ? -1 : 1;
        }
        return left.toString().compareTo(right.toString());
    }

    private static BigDecimal numericValue(Object value) {
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int getRowNumberFromColumn(String input) {
        String numberPart = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numberPart);
    }

    public static int getColumnNumber(String columnSymbol) {
        int columnNumber = 0;
        for (int i = 0; i < columnSymbol.length(); i++) {
            columnNumber = columnNumber * 26 + (columnSymbol.charAt(i) - 'A' + 1);
        }
        return columnNumber;
    }

    public static String getColumnHeader(String columnLabel) {
        return columnLabel.replaceAll("[^A-Z]", "");
    }

    public static String getColumnHeader(int columnNumber) {
        StringBuilder label = new StringBuilder();
        while (columnNumber > 0) {
            label.insert(0, (char) ('A' + (columnNumber - 1 % 26)));
            columnNumber = columnNumber / 26 - 1;
        }
        return label.toString();
    }
}
