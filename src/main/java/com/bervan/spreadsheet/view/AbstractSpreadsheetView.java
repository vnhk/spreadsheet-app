package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanTextField;
import com.bervan.core.model.BervanLogger;
import com.bervan.spreadsheet.functions.DivisionFunction;
import com.bervan.spreadsheet.functions.MultiplyFunction;
import com.bervan.spreadsheet.functions.SubtractFunction;
import com.bervan.spreadsheet.functions.SumFunction;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.model.TextFieldCell;
import com.bervan.spreadsheet.service.SpreadsheetRowConverter;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    private Spreadsheet spreadsheet;
    private Grid<SpreadsheetRow> grid;
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private String selectedColumn;
    private final SpreadsheetService service;
    private static final int MAX_RECURSION_DEPTH = 100;

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        String spreadsheetName = beforeEvent.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        Optional<Spreadsheet> entity = service.loadByName(spreadsheetName);
        if (entity.isPresent()) {
            spreadsheet = entity.get();
            String body = spreadsheet.getBody();
            spreadsheet.setRows(SpreadsheetRowConverter.deserializeSpreadsheet(body));
        } else {
            spreadsheet = new Spreadsheet(spreadsheetName);
        }

        if (spreadsheet.getRows() == null) {
            spreadsheet.setRows(new ArrayList<>());
        }

        grid = new Grid<>(SpreadsheetRow.class);
        grid.setHeight("70vh");
        grid.setItems(spreadsheet.getRows());

        // Set up right-click context menu on the grid
        GridContextMenu<SpreadsheetRow> contextMenu = new GridContextMenu<>(grid);

        contextMenu.addItem("Copy row");
        contextMenu.addItem("Duplicate Row", event -> {
            event.getItem().ifPresent(this::duplicateRow);
        });
        contextMenu.addItem("Delete Row", event -> {
            event.getItem().ifPresent(this::deleteRow);
        });

        contextMenu.addItem("Copy column");
        contextMenu.addItem("Duplicate Column", event -> {
            event.getItem().ifPresent(row -> {
                int columnIndex = getColumnIndex(event);
                if (columnIndex != -1) {
                    duplicateColumn(columnIndex);
                }
            });
        });
        contextMenu.addItem("Delete Column", event -> {
            event.getItem().ifPresent(row -> {
                int columnIndex = getColumnIndex(event);
                if (columnIndex != -1) {
                    deleteColumn(columnIndex);
                }
            });
        });

        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getColumnId().ifPresent(id -> {
                selectedColumn = id;
            });
        });

        add(new SpreadsheetPageLayout(true, spreadsheetName));
        add(grid);
        updateGridColumns();
        addBottomRowButton();
    }


    public AbstractSpreadsheetView(SpreadsheetService service, BervanLogger logger) {
        this.service = service;
    }

    private void updateGridColumns() {
        grid.removeAllColumns();

        grid.addColumn(item -> spreadsheet.getRows().indexOf(item)).setHeader("No.")
                .setFrozen(true);

        for (int i = 0; i < spreadsheet.getColumnCount(); i++) {
            final int columnIndex = i; // Capture column index for lambda
            Grid.Column<SpreadsheetRow> spreadsheetRowColumn = grid.addColumn(new ComponentRenderer<>(row -> {
                        TextFieldCell cellField = new TextFieldCell();
                        cellField.setValue(String.valueOf(row.getCell(columnIndex).value));
                        cellField.addFocusListener(textFieldFocusEvent -> {
                            cellField.isFocused = true;
                            if (row.getCell(columnIndex).isFunction) {
                                cellField.setValue(row.getCell(columnIndex).functionValue);
                            }
                            cellField.isFocused = false;
                        });

                        //unfocus
                        cellField.addBlurListener(textFieldBlurEvent -> {
                            if (row.getCell(columnIndex).isFunction) {
                                calculateFunctionValue(row.getCell(columnIndex));
                                grid.getDataProvider().refreshItem(row);
                            }
                        });

                        cellField.addValueChangeListener(e -> {
                            String originalValue = e.getValue();
                            Cell cell = row.setCell(columnIndex, originalValue);
                            if (cell.isFunction && !cellField.isFocused) {
                                calculateFunctionValue(cell);
                                grid.getDataProvider().refreshItem(row);
                            }

                            try {

                                if (reloadRelated(row.number, columnIndex)) {
                                    grid.getDataProvider().refreshItem(row);
                                }
                            } catch (Exception ex) {
                                cell.function = "ERROR";
                                cell.value = "ERROR";
                                cell.isFunction = false;
                            }

                        });
                        return cellField;
                    }))
                    .setHeader(getColumnHeader(columnIndex));
            spreadsheetRowColumn.setWidth("200px");
            spreadsheetRowColumn.setId("col" + (columnIndex + 1));
        }

        // Enable single-click editing for all cells
        Binder<SpreadsheetRow> binder = new Binder<>(SpreadsheetRow.class);
        grid.getEditor().setBinder(binder);
        grid.addItemClickListener(event -> grid.getEditor().editItem(event.getItem()));
    }

    private void calculateFunctionValue(Cell cell) {
        if ("+".equals(cell.function)) {
            cell.value = (String.valueOf(new SumFunction().calculate(cell.relatedCells, spreadsheet.getRows())));
        } else if ("*".equals(cell.function)) {
            cell.value = (String.valueOf(new MultiplyFunction().calculate(cell.relatedCells, spreadsheet.getRows())));
        } else if ("-".equals(cell.function)) {
            cell.value = (String.valueOf(new SubtractFunction().calculate(cell.relatedCells, spreadsheet.getRows())));
        } else if ("/".equals(cell.function)) {
            cell.value = (String.valueOf(new DivisionFunction().calculate(cell.relatedCells, spreadsheet.getRows())));
        }
    }

    private boolean reloadRelated(int number, int columnIndex) {
        return reloadRelated(number, columnIndex, 0);
    }

    private boolean reloadRelated(int number, int columnIndex, int recursionDepth) {
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            showErrorNotification("Maximum function depth reached. Stopping further recursion. Fix functions.");
            throw new RuntimeException("Maximum function depth reached. Stopping further recursion. Fix functions.");
        }

        for (SpreadsheetRow row : spreadsheet.getRows()) {
            List<Cell> cells = row.getCells();
            for (Cell cell : cells) {
                Optional<String> relatedColumn = cell.relatedCells.stream()
                        .filter(e -> e.equals(getColumnHeader(columnIndex) + number))
                        .findAny();

                if (relatedColumn.isPresent()) {
                    calculateFunctionValue(cell);
                    int cellRowIndex = extractRowIndex(cell.cellId);
                    int cellColumnIndex = getColumnIndex(cell.cellId.replace(String.valueOf(cellRowIndex), ""));
                    grid.getDataProvider().refreshItem(row);

                    reloadRelated(cellRowIndex, cellColumnIndex, recursionDepth + 1);
                }
            }
        }

        return false;
    }

    private String getColumnHeader(int columnIndex) {
        StringBuilder label = new StringBuilder();
        while (columnIndex >= 0) {
            label.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return label.toString();
    }

    public int extractRowIndex(String input) {
        String numberPart = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numberPart);
    }

    private int getColumnIndex(String columnLabel) {
        int columnIndex = 0;
        for (int i = 0; i < columnLabel.length(); i++) {
            columnIndex = columnIndex * 26 + (columnLabel.charAt(i) - 'A' + 1);
        }
        return columnIndex - 1;
    }

    private int getColumnIndex(GridContextMenu.GridContextMenuItemClickEvent<SpreadsheetRow> event) {
        List<Grid.Column<SpreadsheetRow>> columns = grid.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().isPresent() && columns.get(i).getId().get().equals(selectedColumn)) {
                return i;
            }
        }
        return -1;
    }

    private void addBottomRowButton() {
        // Add row button positioned at the bottom center
        Button addRowButton = new Button("Add New Row", e -> addRow());
        addRowButton.setClassName("option-button");
        Button addColumnButton = new Button("Add New Column", e -> addColumn());
        addColumnButton.setClassName("option-button");

        Button sortButton = new Button("Sort columns", e -> sortColumnsModal());
        sortButton.setClassName("option-button");

        Button saveChanges = new Button("Save changes", e -> saveChanges());
        saveChanges.setClassName("option-button");

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomLayout.add(addRowButton);
        bottomLayout.add(addColumnButton);
        bottomLayout.add(sortButton);
        add(bottomLayout);
        add(saveChanges);
    }

    private void sortColumnsModal() {
        Dialog dialog = new Dialog();
        dialog.setWidth("50vw");

        // Fields for input
        BervanTextField columnsField = new BervanTextField("Type columns (comma separated)", "E,F");
        BervanTextField rowsField = new BervanTextField("Type rows (colon separated)", "0:10");

        // Dropdowns
        ComboBox<String> columnDropdown = new ComboBox<>();
        columnDropdown.setLabel("Select Sort Column");
        ComboBox<String> orderDropdown = new ComboBox<>("Select Order", "Ascending", "Descending");

        // Add a listener to update the column dropdown based on the columns field
        columnsField.addValueChangeListener(event -> {
            String columnsText = event.getValue();
            if (columnsText != null && !columnsText.trim().isEmpty()) {
                List<String> columnOptions = Arrays.stream(columnsText.split(","))
                        .map(String::trim)
                        .filter(col -> !col.isEmpty())
                        .collect(Collectors.toList());

                columnDropdown.setItems(columnOptions);
            } else {
                columnDropdown.clear();
            }
        });

        Button okButton = new Button("Sort columns", e -> sortColumns(columnDropdown.getValue(),
                orderDropdown.getValue(), columnsField.getValue(), rowsField.getValue(), dialog));
        okButton.setClassName("option-button");
        // Add components to dialog
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getDialogTopBarLayout(dialog), columnsField, rowsField, columnDropdown, orderDropdown, okButton);
        dialog.add(verticalLayout);

        // Open the dialog
        dialog.open();
    }

    private void sortColumns(String sortColumn, String order, String columnsToBeSorted, String rows, Dialog dialog) {
        dialog.close();
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
                for (; index < end; index++) {
                    List<String> values = newValues.get(index);

                    for (int i = 0; i < values.size(); i++) {
                        spreadsheet.getRows().get(index).setCell(getColumnIndex(columnsToSort.get(i)), values.get(i));
                    }

                }

                grid.getDataProvider().refreshAll();
                showSuccessNotification("Sort applied!");
                dialog.close();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorNotification("An error occurred while sorting: " + e.getMessage());
            }
        } else {
            showErrorNotification("Invalid sort configuration!");
        }

    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveChanges() {
        try {
            String body = SpreadsheetRowConverter.serializeSpreadsheet(spreadsheet.getRows());
            spreadsheet.setBody(body);
            service.save(spreadsheet);
            showSuccessNotification("Saved!");
        } catch (Exception e) {
            showErrorNotification("Could not save spreadsheet!");
        }
    }

    private void addRow() {
        spreadsheet.addRow();
        grid.getDataProvider().refreshAll();
    }

    private void addColumn() {
        spreadsheet.addColumn();
        updateGridColumns();
        grid.getDataProvider().refreshAll();
    }

    private void duplicateRow(SpreadsheetRow row) {
        spreadsheet.duplicateRow(row);
        grid.getDataProvider().refreshAll();
    }

    private void deleteRow(SpreadsheetRow row) {
        spreadsheet.removeRow(row);
        grid.getDataProvider().refreshAll();
    }

    private void duplicateColumn(int columnIndex) {
        spreadsheet.duplicateColumn(columnIndex);
        updateGridColumns();
        grid.getDataProvider().refreshAll();
    }

    private void deleteColumn(int columnIndex) {
        spreadsheet.removeColumn(columnIndex);
        updateGridColumns();
        grid.getDataProvider().refreshAll();
    }
}