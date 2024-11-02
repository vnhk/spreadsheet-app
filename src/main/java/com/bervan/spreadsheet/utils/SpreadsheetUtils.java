package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;

import java.util.*;

public class SpreadsheetUtils {
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

    public static int extractRowIndex(String input) {
        String numberPart = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numberPart);
    }

    public static int getColumnIndex(String columnLabel) {
        int columnIndex = 0;
        for (int i = 0; i < columnLabel.length(); i++) {
            columnIndex = columnIndex * 26 + (columnLabel.charAt(i) - 'A' + 1);
        }
        return columnIndex - 1;
    }

    public static int getColumnIndex(GridContextMenu.GridContextMenuItemClickEvent<SpreadsheetRow> event, Grid grid, String selectedColumn) {
        List<Grid.Column<SpreadsheetRow>> columns = grid.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().isPresent() && columns.get(i).getId().get().equals(selectedColumn)) {
                return i;
            }
        }
        return -1;
    }

    public static String getColumnHeader(int columnIndex) {
        StringBuilder label = new StringBuilder();
        while (columnIndex >= 0) {
            label.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return label.toString();
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
