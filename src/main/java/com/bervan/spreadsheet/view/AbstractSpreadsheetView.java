package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        grid.addColumn(item -> spreadsheet.getRows().indexOf(item)).setHeader("No.");

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

        Button saveChanges = new Button("Save changes", e -> saveChanges());
        saveChanges.setClassName("option-button");

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomLayout.add(addRowButton);
        bottomLayout.add(addColumnButton);
        add(bottomLayout);
        add(saveChanges);
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