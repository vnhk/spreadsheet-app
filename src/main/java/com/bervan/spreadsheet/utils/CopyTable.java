package com.bervan.spreadsheet.utils;

import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.jsoup.Jsoup;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CopyTable {

    // Method to show the copy table dialog
    public static void showCopyTableDialog(Set<Cell> selectedCells, int columns, List<SpreadsheetRow> spreadsheetRows,
                                           Function<String, Void> errorNotification, Function<String, Void> successNotification) {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");

        VerticalLayout layout = new VerticalLayout();

        // Generate the table content as plain text
        String tableText = "";

        if (!selectedCells.isEmpty()) {
            Map<String, List<Cell>> columnsMap = new HashMap<>();

            for (Cell cell : selectedCells) {
                String column = cell.getColumnSymbol();
                columnsMap.putIfAbsent(column, new ArrayList<>());
                columnsMap.get(column).add(cell);
            }

            boolean allSameSize = columnsMap.values().stream().map(List::size).distinct().count() == 1;

            if (allSameSize) {
                List<Integer> referenceRowNumbers = columnsMap.values().iterator().next().stream().map(cell -> cell.getRowNumber()).sorted().toList();

                boolean allRowsMatch = columnsMap.values().stream().allMatch(cellsI -> {
                    List<Integer> rowNumbers = cellsI.stream().map(cell -> cell.getRowNumber()).sorted().toList();
                    return rowNumbers.equals(referenceRowNumbers);
                });

                if (allRowsMatch) {
                    boolean allContinuous = true;
                    int minRow = 0;
                    int maxRow = 0;
                    for (Map.Entry<String, List<Cell>> entry : columnsMap.entrySet()) {
                        String column = entry.getKey();
                        List<Cell> entryCells = entry.getValue();

                        minRow = entryCells.stream().mapToInt(c -> c.getRowNumber()).min().orElseThrow();
                        maxRow = entryCells.stream().mapToInt(c -> c.getRowNumber()).max().orElseThrow();

                        Set<Integer> rowNumbers = entryCells.stream().map(cell -> cell.getRowNumber()).collect(Collectors.toSet());

                        for (int i = minRow; i <= maxRow; i++) {
                            if (!rowNumbers.contains(i)) {
                                allContinuous = false;
                                break;
                            }
                        }
                    }

                    if (allContinuous) {
                        successNotification.apply("Copy table properties applied based on selection!");
                        tableText = generateTableText(selectedCells.stream().toList());
                    } else {
                        errorNotification.apply("Copy table properties cannot be applied!");
                    }
                } else {
                    errorNotification.apply("Copy table properties cannot be applied!");
                }
            }
        }

        if (tableText.isEmpty()) {
            tableText = generateTableText(columns, spreadsheetRows);
        }

        TextArea textArea = new TextArea();
        textArea.setWidthFull();
        textArea.setHeight("400px");
        textArea.setValue(tableText);
        textArea.setReadOnly(true);

        // Instructions for the user
        Span instructions = new Span("Select all the text below and copy it to your clipboard:");

        // Add components to layout
        layout.add(instructions, textArea);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.addClassName("option-button");

        layout.add(closeButton);

        dialog.add(layout);
        dialog.open();
    }

    // Method to generate the table content as plain text
    private static String generateTableText(int columns, List<SpreadsheetRow> spreadsheetRows) {
        StringBuilder sb = new StringBuilder();

        // Build header row
        sb.append("#\t");
        for (int col = 0; col < columns; col++) {
            sb.append(SpreadsheetUtils.getColumnHeader(col)).append("\t");
        }
        sb.append("\n");

        // Build data rows
        for (int rowIndex = 0; rowIndex < spreadsheetRows.size(); rowIndex++) {
            sb.append(rowIndex).append("\t");
            SpreadsheetRow spreadsheetRow = spreadsheetRows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < spreadsheetRow.getCells().size(); columnIndex++) {
                Cell cell = spreadsheetRow.getCell(columnIndex);
                String val = (cell != null && cell.getHtmlContent() != null) ? Jsoup.parse(cell.getHtmlContent()).text().replaceAll("\n", " ").replaceAll("\t", " ") : "";
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private static String generateTableText(List<Cell> cellList) {
        if (cellList == null || cellList.isEmpty()) {
            return "No data available.";
        }

        StringBuilder sb = new StringBuilder();

        int minRow = cellList.stream().mapToInt(e -> e.getRowNumber()).min().orElse(0);
        int maxRow = cellList.stream().mapToInt(e -> e.getRowNumber()).max().orElse(0);
        int minCol = cellList.stream().mapToInt(e -> e.getColumnNumber()).min().orElse(0);
        int maxCol = cellList.stream().mapToInt(e -> e.getColumnNumber()).max().orElse(0);

        sb.append("#\t");
        for (int col = minCol; col <= maxCol; col++) {
            sb.append(SpreadsheetUtils.getColumnHeader(col)).append("\t");
        }
        sb.append("\n");

        Map<String, Cell> cellMap = cellList.stream().collect(Collectors.toMap(cell -> cell.getColumnNumber() + "_" + cell.getRowNumber(), cell -> cell));

        for (int row = minRow; row <= maxRow; row++) {
            sb.append(row).append("\t");
            for (int col = minCol; col <= maxCol; col++) {
                Cell cell = cellMap.get(col + "_" + row);
                String val = (cell != null && cell.getHtmlContent() != null) ? Jsoup.parse(cell.getHtmlContent()).text().replaceAll("\n", " ").replaceAll("\t", " ") : "";
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}
