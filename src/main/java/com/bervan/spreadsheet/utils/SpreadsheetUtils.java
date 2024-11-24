package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;

import java.util.*;

public class SpreadsheetUtils {
    public static UtilsMessage sortColumns(Cell[][] cells, String sortColumn, String order, String columnsToBeSorted, String rows) {
        UtilsMessage utilsMessage = new UtilsMessage();
        String[] colonSeparated = rows.split(":");

        if (colonSeparated.length == 2) {
            try {
                Integer start = Integer.parseInt(colonSeparated[0].replaceAll(".*?(\\d+)$", "$1"));
                Integer end = Integer.parseInt(colonSeparated[1].replaceAll(".*?(\\d+)$", "$1"));

                if (start < 0) {
                    start = 0;
                }

                if (end > cells.length - 1) {
                    end = cells.length - 1;
                }

                if (start > end) {
                    throw new RuntimeException("Incorrect rows");
                }

                // Get the column index to sort by
                int sortColumnIndex = getColumnIndex(sortColumn);

                if (sortColumnIndex < 0 || sortColumnIndex >= cells[0].length) {
                    throw new RuntimeException("Invalid sort column: " + sortColumn);
                }

                // Collect rows within the range
                List<Cell[]> targetRows = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    targetRows.add(cells[i]);
                }

                // Define the comparator for sorting
                Comparator<Cell[]> comparator = (row1, row2) -> {
                    Cell cell1 = row1[sortColumnIndex];
                    Cell cell2 = row2[sortColumnIndex];
                    String val1 = cell1 != null ? cell1.value : null;
                    String val2 = cell2 != null ? cell2.value : null;

                    boolean isVal1Integer = isInteger(val1);
                    boolean isVal2Integer = isInteger(val2);

                    if (!isVal1Integer && !isVal2Integer) {
                        return 0;
                    } else if (!isVal1Integer) {
                        return 1;
                    } else if (!isVal2Integer) {
                        return -1;
                    } else {
                        int comparison = Integer.compare(Integer.parseInt(val1), Integer.parseInt(val2));
                        return "Descending".equalsIgnoreCase(order) ? -comparison : comparison;
                    }
                };

                // Sort the rows
                targetRows.sort(comparator);

                // Apply the sorted rows back to the original array
                int index = start;
                for (Cell[] sortedRow : targetRows) {
                    cells[index] = sortedRow;
                    index++;
                }

                utilsMessage.message = "Sort applied!";
                utilsMessage.isSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                utilsMessage.message = "An error occurred while sorting: " + e.getMessage();
                utilsMessage.isError = true;
            }
        } else {
            utilsMessage.message = "Invalid sort configuration!";
            utilsMessage.isError = true;
        }
        return utilsMessage;
    }
    public static UtilsMessage sortColumns(Spreadsheet spreadsheet, String sortColumn, String order, String columnsToBeSorted, String rows, Grid grid) {
        UtilsMessage utilsMessage = new UtilsMessage();
        String[] colonSeparated = rows.split(":");
        if (colonSeparated.length == 2) {
            try {
                Integer start = Integer.parseInt(colonSeparated[0].replaceAll(".*?(\\d+)$", "$1"));
                Integer end = Integer.parseInt(colonSeparated[1].replaceAll(".*?(\\d+)$", "$1"));

                if (start < 0) {
                    start = 0;
                }

                if (end > spreadsheet.getRows().size() - 1) {
                    end = spreadsheet.getRows().size() - 1;
                }

                if (start > end) {
                    throw new RuntimeException("Incorrect rows");
                }

                List<SpreadsheetRow> allRows = spreadsheet.getRows();
                List<SpreadsheetRow> targetRows = new ArrayList<>();

                // Collect only the rows that fall within the specified range
                for (SpreadsheetRow row : allRows) {
                    Integer rowNumber = row.number;
                    if (rowNumber >= start && rowNumber <= end) {
                        targetRows.add(row);
                    }
                }

                Comparator<SpreadsheetRow> comparator = Comparator.comparing(row -> {
                    Cell sortCell = row.getCell(getColumnIndex(sortColumn));
                    String cellValue = sortCell != null ? sortCell.value : null;

                    // If cellValue is null or non-integer, treat it as greater than any integer value
                    if (cellValue == null || !isInteger(cellValue)) {
                        if ("Descending".equalsIgnoreCase(order)) {
                            return Integer.MAX_VALUE * -1;
                        }
                        return Integer.MAX_VALUE; // Push null or non-integer values to the end
                    }
                    return Integer.parseInt(cellValue);
                });

                // Reverse the comparator if the order is "Descending"
                if ("Descending".equalsIgnoreCase(order)) {
                    comparator = comparator.reversed();
                }

                // Sort rows by the specified column and order
                targetRows.sort(comparator);

                // Extract the column symbols that need to be sorted (comma-separated list)
                List<String> columnsToSort = Arrays.asList(columnsToBeSorted.split(","));

                Map<Integer, List<String>> newValues = new HashMap<>();
                int index = start;

                for (int i = 0; i < targetRows.size(); i++) {
                    SpreadsheetRow sortedRow = targetRows.get(i);
                    newValues.put(index, new ArrayList<>());

                    for (String column : columnsToSort) {
                        newValues.get(index).add(sortedRow.getCell(getColumnIndex(column)).value);
                    }

                    index++;
                }

                index = start;
                for (; index <= end; index++) {
                    List<String> values = newValues.get(index);

                    for (int i = 0; i < values.size(); i++) {
                        int finalIndex = index;
                        spreadsheet.getRows().stream().filter(row -> row.number == finalIndex)
                                .findFirst().get()
                                .setCell(getColumnIndex(columnsToSort.get(i)), values.get(i));
                    }

                }

                grid.getDataProvider().refreshAll();
                utilsMessage.message = "Sort applied!";
                utilsMessage.isSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                utilsMessage.message = "An error occurred while sorting: " + e.getMessage();
                utilsMessage.isError = true;
            }
        } else {
            utilsMessage.message = "Invalid sort configuration!";
            utilsMessage.isError = true;
        }
        return utilsMessage;
    }

    public static int getRowNumberFromColumn(String input) {
        String numberPart = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numberPart);
    }

    public static String incrementRowIndexInColumnName(String input) {
        int oldRow = getRowNumberFromColumn(input);
        return input.replace(String.valueOf(oldRow), String.valueOf(oldRow + 1));
    }

    public static String incrementColumnIndexInColumnName(String input) {
        int oldColumnIndex = getColumnIndex(getColumnHeader(input));
        String oldColumnHeader = getColumnHeader(oldColumnIndex);
        String newColumnHeader = getColumnHeader(oldColumnIndex + 1);
        return input.replace(oldColumnHeader, newColumnHeader);
    }

    public static String decrementRowIndexInColumnName(String input) {
        int oldRow = getRowNumberFromColumn(input);
        return input.replace(String.valueOf(oldRow), String.valueOf(oldRow - 1));
    }

    public static int getColumnIndex(String columnSymbol) {
        int columnIndex = 0;
        for (int i = 0; i < columnSymbol.length(); i++) {
            columnIndex = columnIndex * 26 + (columnSymbol.charAt(i) - 'A' + 1);
        }
        return columnIndex - 1;
    }

    public static int getColumnIndex(GridContextMenu.GridContextMenuItemClickEvent<SpreadsheetRow> event, Grid grid, String selectedColumn) {
        List<Grid.Column<SpreadsheetRow>> columns = grid.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().isPresent() && columns.get(i).getId().get().equals(selectedColumn)) {
                return i - 1; //i - 1 because first column is rowNumber
            }
        }
        return -1;
    }

    public static String getColumnHeader(String columnLabel) {
        return columnLabel.replaceAll("[^A-Z]", "");
    }

    public static String getColumnHeader(int columnIndex) {
        StringBuilder label = new StringBuilder();
        while (columnIndex >= 0) {
            label.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return label.toString();
    }

    public static void shiftRowsInFunctionsToRowPlus1(int oldRowIndex, List<SpreadsheetRow> rows) {
        if (oldRowIndex + 1 > rows.size()) {
            return;
        }

        List<Integer> spreadsheetRowsToShift = rows.subList(oldRowIndex + 1, rows.size())
                .stream().map(e -> e.number)
                .toList();

        for (SpreadsheetRow spreadsheetRow : rows) {
            for (Cell cell : spreadsheetRow.getCells()) {
                if (cell.isFunction) {
                    for (int i = 0; i < cell.getRelatedCellsId().size(); i++) {
                        String relatedCell = cell.getRelatedCellsId().get(i);
                        Integer rowNumber = SpreadsheetUtils.getRowNumberFromColumn(relatedCell);
                        if (spreadsheetRowsToShift.contains(rowNumber)) {
                            String newRelatedCell = SpreadsheetUtils.incrementRowIndexInColumnName(relatedCell);
                            cell.updateFunctionRelatedCell(relatedCell, newRelatedCell);
                        }
                    }
                }
            }
        }
    }


    public static void shiftColumnsPlus1InFunctions(int columnIndex, List<SpreadsheetRow> rows) {
        SpreadsheetRow spreadsheetRow0 = rows.get(0); //it must exist to use this function
        List<String> affectedColumns = spreadsheetRow0.getCells().stream().filter(e -> e.columnNumber > columnIndex)
                .map(e -> e.columnSymbol)
                .toList();
        // > or >= ?????

        //each affected column in any function in any cell has to be updated + 1
        for (SpreadsheetRow spreadsheetRow : rows) {
            for (Cell cell : spreadsheetRow.getCells()) {
                if (cell.isFunction) {
                    for (int i = 0; i < cell.getRelatedCellsId().size(); i++) {
                        String relatedCell = cell.getRelatedCellsId().get(i);
                        String columnHeader = SpreadsheetUtils.getColumnHeader(relatedCell);
                        if (affectedColumns.contains(columnHeader)) {
                            String newRelatedCell = SpreadsheetUtils.incrementColumnIndexInColumnName(relatedCell);
                            cell.updateFunctionRelatedCell(relatedCell, newRelatedCell);
                        }
                    }
                }
            }
        }
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLiteralValue(String relatedCell) {
        try {
            String columnHeader = getColumnHeader(relatedCell);
            int rowNumberFromColumn = getRowNumberFromColumn(relatedCell);

            if (!Strings.isNullOrEmpty(columnHeader)
                    && !columnHeader.equals(relatedCell)
                    && rowNumberFromColumn != Integer.parseInt(relatedCell)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
