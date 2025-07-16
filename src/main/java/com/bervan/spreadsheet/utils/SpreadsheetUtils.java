package com.bervan.spreadsheet.utils;

import com.bervan.common.model.UtilsMessage;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SpreadsheetUtils {

    //does sort columns make sense? What if we sort cells that are used somewhere in a formula? should we update it or not?

    public static UtilsMessage sortColumns(List<SpreadsheetRow> spreadsheetRows, String sortColumn, String order, String columnsToBeSorted, String rows) {
        log.error(" sortColumns not implemented");
        return new UtilsMessage();
    }
//        UtilsMessage utilsMessage = new UtilsMessage();
//        String[] colonSeparatedRowsToBeSorted = rows.split(":");
//
//        if (colonSeparatedRowsToBeSorted.length == 2) {
//            try {
//                Integer startRow = Integer.parseInt(colonSeparatedRowsToBeSorted[0].replaceAll(".*?(\\d+)$", "$1"));
//                Integer endRow = Integer.parseInt(colonSeparatedRowsToBeSorted[1].replaceAll(".*?(\\d+)$", "$1"));
//
//                if (startRow < 0) startRow = 0;
//                if (endRow > spreadsheetRows.size() - 1) endRow = spreadsheetRows.size() - 1;
//                if (startRow > endRow) throw new RuntimeException("Incorrect rows");
//
//                // Column indices to swap
//                List<Integer> columnIndicesToSwap = Arrays.stream(columnsToBeSorted.split(","))
//                        .map(String::trim)
//                        .map(SpreadsheetUtils::getColumnIndex)
//                        .filter(i -> i >= 0)
//                        .toList();
//
//                int sortColumnIndex = getColumnIndex(sortColumn);
//                if (sortColumnIndex < 0 || sortColumnIndex >= spreadsheetRows.get(0).getCells().size()) {
//                    throw new RuntimeException("Invalid sort column: " + sortColumn);
//                }
//
//                // Extract target rows and create sortable pairs: [sortKey, map of columnIndex -> value]
//                List<Map<Integer, String>> extractedValues = new ArrayList<>();
//                List<String> sortKeys = new ArrayList<>();
//
//                for (int i = startRow; i <= endRow; i++) {
//                    SpreadsheetRow row = spreadsheetRows.get(i);
//                    Map<Integer, String> valueMap = new HashMap<>();
//                    for (int colIndex : columnIndicesToSwap) {
//                        valueMap.put(colIndex, row.getCell(colIndex).getValue());
//                    }
//                    extractedValues.add(valueMap);
//
//                    String sortKey = row.getCell(sortColumnIndex).getValue();
//                    sortKeys.add(sortKey);
//                }
//
//                // Sort the indices based on sortKey and order
//                List<Integer> sortedIndices = IntStream.range(0, sortKeys.size())
//                        .boxed()
//                        .sorted((i1, i2) -> {
//                            String val1 = sortKeys.get(i1);
//                            String val2 = sortKeys.get(i2);
//
//                            boolean isInt1 = isInteger(val1);
//                            boolean isInt2 = isInteger(val2);
//
//                            int cmp;
//                            if (isInt1 && isInt2) {
//                                cmp = Integer.compare(Integer.parseInt(val1), Integer.parseInt(val2));
//                            } else {
//                                cmp = val1.compareToIgnoreCase(val2);
//                            }
//
//                            return "Descending".equalsIgnoreCase(order) ? -cmp : cmp;
//                        })
//                        .collect(Collectors.toList());
//
//                // Apply sorted values back only to selected columns
//                for (int i = startRow; i <= endRow; i++) {
//                    SpreadsheetRow row = spreadsheetRows.get(i);
//                    Map<Integer, String> sortedValueMap = extractedValues.get(sortedIndices.get(i - startRow));
//                    for (Map.Entry<Integer, String> entry : sortedValueMap.entrySet()) {
//                        row.getCell(entry.getKey()).setValue(entry.getValue());
//                    }
//                }
//
//                utilsMessage.message = "Sort applied!";
//                utilsMessage.isSuccess = true;
//            } catch (Exception e) {
//                log.error("An error occurred while sorting", e);
//                utilsMessage.message = "An error occurred while sorting: " + e.getMessage();
//                utilsMessage.isError = true;
//            }
//        } else {
//            utilsMessage.message = "Invalid sort configuration!";
//            utilsMessage.isError = true;
//        }
//        return utilsMessage;
//    }

//    public static UtilsMessage sortColumns(Spreadsheet spreadsheet, String sortColumn, String order, String columnsToBeSorted, String rows, Grid grid) {
//        UtilsMessage utilsMessage = new UtilsMessage();
//        String[] colonSeparated = rows.split(":");
//        if (colonSeparated.length == 2) {
//            try {
//                Integer start = Integer.parseInt(colonSeparated[0].replaceAll(".*?(\\d+)$", "$1"));
//                Integer end = Integer.parseInt(colonSeparated[1].replaceAll(".*?(\\d+)$", "$1"));
//
//                if (start < 0) {
//                    start = 0;
//                }
//
//                if (end > spreadsheet.getRows().size() - 1) {
//                    end = spreadsheet.getRows().size() - 1;
//                }
//
//                if (start > end) {
//                    throw new RuntimeException("Incorrect rows");
//                }
//
//                List<SpreadsheetRow> allRows = spreadsheet.getRows();
//                List<SpreadsheetRow> targetRows = new ArrayList<>();
//
//                // Collect only the rows that fall within the specified range
//                for (SpreadsheetRow row : allRows) {
//                    Integer rowNumber = row.number;
//                    if (rowNumber >= start && rowNumber <= end) {
//                        targetRows.add(row);
//                    }
//                }
//
//                Comparator<SpreadsheetRow> comparator = Comparator.comparing(row -> {
//                    Cell sortCell = row.getCell(getColumnIndex(sortColumn));
//                    String cellValue = sortCell != null ? sortCell.getValue() : null;
//
//                    // If cellValue is null or non-integer, treat it as greater than any integer value
//                    if (cellValue == null || !isInteger(cellValue)) {
//                        if ("Descending".equalsIgnoreCase(order)) {
//                            return Integer.MAX_VALUE * -1;
//                        }
//                        return Integer.MAX_VALUE; // Push null or non-integer values to the end
//                    }
//                    return Integer.parseInt(cellValue);
//                });
//
//                // Reverse the comparator if the order is "Descending"
//                if ("Descending".equalsIgnoreCase(order)) {
//                    comparator = comparator.reversed();
//                }
//
//                // Sort rows by the specified column and order
//                targetRows.sort(comparator);
//
//                // Extract the column symbols that need to be sorted (comma-separated list)
//                List<String> columnsToSort = Arrays.asList(columnsToBeSorted.split(","));
//
//                Map<Integer, List<String>> newValues = new HashMap<>();
//                int index = start;
//
//                for (int i = 0; i < targetRows.size(); i++) {
//                    SpreadsheetRow sortedRow = targetRows.get(i);
//                    newValues.put(index, new ArrayList<>());
//
//                    for (String column : columnsToSort) {
//                        newValues.get(index).add(sortedRow.getCell(getColumnIndex(column)).getValue());
//                    }
//
//                    index++;
//                }
//
//                index = start;
//                for (; index <= end; index++) {
//                    List<String> values = newValues.get(index);
//
//                    for (int i = 0; i < values.size(); i++) {
//                        int finalIndex = index;
//                        spreadsheet.getRows().stream().filter(row -> row.number == finalIndex)
//                                .findFirst().get()
//                                .setCell(getColumnIndex(columnsToSort.get(i)), values.get(i));
//                    }
//
//                }
//
//                grid.getDataProvider().refreshAll();
//                utilsMessage.message = "Sort applied!";
//                utilsMessage.isSuccess = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                utilsMessage.message = "An error occurred while sorting: " + e.getMessage();
//                utilsMessage.isError = true;
//            }
//        } else {
//            utilsMessage.message = "Invalid sort configuration!";
//            utilsMessage.isError = true;
//        }
//        return utilsMessage;
//    }

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
}
