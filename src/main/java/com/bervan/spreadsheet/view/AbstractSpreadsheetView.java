package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanTextField;
import com.bervan.common.model.UtilsMessage;
import com.bervan.core.model.BervanLogger;
import com.bervan.spreadsheet.functions.SpreadsheetFunction;
import com.bervan.spreadsheet.model.*;
import com.bervan.spreadsheet.service.SpreadsheetRowConverter;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.*;
import java.util.stream.Collectors;

import static com.bervan.spreadsheet.utils.SpreadsheetUtils.*;

public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    private Spreadsheet spreadsheet;
    private Grid<SpreadsheetRow> grid;
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private String selectedColumn;
    private final SpreadsheetService service;
    private static final int MAX_RECURSION_DEPTH = 100;
    private final List<? extends SpreadsheetFunction> spreadsheetFunctions;
    private final List<TextFieldCell> spreadsheetVaadinCells = new ArrayList<>();

    public Grid<SpreadsheetRow> getGrid() {
        return grid;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        String spreadsheetName = beforeEvent.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        List<Spreadsheet> entity = service.loadByName(spreadsheetName);
        if (entity.size() > 0) {
            spreadsheet = entity.get(0);
            String body = spreadsheet.getBody();
            String columnsConfig = spreadsheet.getColumnsConfig();
            List<SpreadsheetRow> rows = SpreadsheetRowConverter.deserializeSpreadsheetBody(body);
            spreadsheet.setRows(rows);
            spreadsheet.setConfigs(SpreadsheetRowConverter.deserializeColumnsConfig(columnsConfig));
        } else {
            spreadsheet = new Spreadsheet(spreadsheetName);
        }

        if (spreadsheet.getRows() == null) {
            spreadsheet.setRows(new ArrayList<>());
        }

        if (spreadsheet.getConfigs() == null) {
            spreadsheet.setConfigs(new ArrayList<>());
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

        contextMenu.addItem("Clear row data", event -> {
            event.getItem().ifPresent(this::clearDataInRow);
        });

//        contextMenu.addItem("Delete Row", event -> {
//            event.getItem().ifPresent(this::deleteRow);
//        });

        contextMenu.addItem("Copy column");
        contextMenu.addItem("Duplicate Column", event -> {
            event.getItem().ifPresent(row -> {
                int columnIndex = getColumnIndex(event, grid, selectedColumn);
                if (columnIndex != -1) {
                    duplicateColumn(columnIndex);
                }
            });
        });

        contextMenu.addItem("Clear column data", event -> {
            event.getItem().ifPresent(row -> {
                int columnIndex = getColumnIndex(event, grid, selectedColumn);
                if (columnIndex != -1) {
                    clearColumnData(columnIndex);
                }
            });
        });
//        contextMenu.addItem("Delete Column", event -> {
//            event.getItem().ifPresent(row -> {
//                int columnIndex = getColumnIndex(event, grid, selectedColumn);
//                if (columnIndex != -1) {
//                    deleteColumn(columnIndex);
//                }
//            });
//        });

        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getColumnId().ifPresent(id -> {
                selectedColumn = id;
            });
        });

        add(new SpreadsheetPageLayout(true, spreadsheetName));
        addTopRowButtons();
        add(grid);
        refreshGrid();
        addBottomRowButtons();
    }

    private void addTopRowButtons() {
        Button showFunctionsButton = new Button("Show available functions", e -> showAvailableFunctionsModal());
        showFunctionsButton.setClassName("option-button");

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.add(showFunctionsButton);
        add(topLayout);
    }

    private void updateCellsMetadata() {
        for (SpreadsheetRow row : spreadsheet.getRows()) {
            List<Cell> cells = row.getCells();
            cells.sort(Comparator.comparing(e -> e.columnNumber));
            for (int i = 0; i < cells.size(); i++) {
                cells.get(i).columnNumber = i;
                cells.get(i).columnSymbol = getColumnHeader(i);
                cells.get(i).rowNumber = row.number;
                cells.get(i).cellId = cells.get(i).columnSymbol + cells.get(i).rowNumber;
                cells.get(i).refreshFunction();
            }
        }
    }


    public AbstractSpreadsheetView(SpreadsheetService service, BervanLogger logger, List<? extends SpreadsheetFunction> spreadsheetFunctions) {
        this.service = service;
        this.spreadsheetFunctions = spreadsheetFunctions;
    }

    private void refreshGrid() {
        updateCellsMetadata();
        refreshFunctionCells();

        grid.removeAllColumns();
        grid.addColumn(item -> spreadsheet.getRows().indexOf(item)).setHeader("No.")
                .setWidth("50px")
                .setFrozen(true);

        for (int i = 0; i < spreadsheet.getColumnCount(); i++) {
            final int columnIndex = i; // Capture column index for lambda
            Grid.Column<SpreadsheetRow> spreadsheetRowColumn = grid.addColumn(new ComponentRenderer<>(row -> {
                TextFieldCell cellField = new TextFieldCell(row.number, columnIndex, spreadsheetVaadinCells);
                        cellField.setValue(String.valueOf(row.getCell(columnIndex).value));
                        cellField.addFocusListener(textFieldFocusEvent -> {
                            cellField.isFocused = true;
                            if (row.getCell(columnIndex).isFunction) {
                                cellField.setValue(row.getCell(columnIndex).getFunctionValue());
                            }
                            cellField.isFocused = false;
                        });

                        //unfocus
                        cellField.addBlurListener(textFieldBlurEvent -> {
                            cellField.isFocused = true;
                            if (row.getCell(columnIndex).isFunction) {
                                calculateFunctionValue(row.getCell(columnIndex));
                                grid.getDataProvider().refreshItem(row);
                            }
                            cellField.isFocused = false;
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
                                ex.printStackTrace();
                                cell.functionName = "ERROR";
                                cell.value = "ERROR";
                                cell.isFunction = false;
                                showErrorNotification("Function ERROR!");
                            }
                        });

                spreadsheetVaadinCells.add(cellField);
                return cellField;
            })).setHeader(getColumnHeader(columnIndex));
            spreadsheetRowColumn.setResizable(true).setFlexGrow(0);
            spreadsheetRowColumn.setId("col" + (columnIndex + 1));

            Optional<ColumnConfig> columnOptional = spreadsheet.getConfigs().stream().filter(e -> e.columnIndex == columnIndex).findFirst();

            if (columnOptional.isPresent()) {
                spreadsheetRowColumn.setWidth(columnOptional.get().width);
            } else {
                if (spreadsheetRowColumn.getWidth() == null) {
                    spreadsheetRowColumn.setWidth("100px");
                }
                ColumnConfig newColumnConfig = new ColumnConfig();
                newColumnConfig.columnIndex = columnIndex;
                newColumnConfig.width = spreadsheetRowColumn.getWidth();
                spreadsheet.getConfigs().add(newColumnConfig);
            }
        }

        grid.addColumnResizeListener(event -> {
            String header = event.getResizedColumn().getHeaderText();
            int columnIndex = getColumnIndex(header);
            ColumnConfig columnConfig = spreadsheet.getConfigs().stream().filter(e -> e.columnIndex == columnIndex).findFirst().get();
            String newWidth = event.getResizedColumn().getWidth();
            columnConfig.width = newWidth;
        });

        // Enable single-click editing for all cells
        Binder<SpreadsheetRow> binder = new Binder<>(SpreadsheetRow.class);
        grid.getEditor().setBinder(binder);
        grid.addItemClickListener(event -> grid.getEditor().editItem(event.getItem()));
    }

    private void calculateFunctionValue(Cell cell) {
        String functionName = cell.functionName;
        Optional<? extends SpreadsheetFunction> first = spreadsheetFunctions.stream().filter(e -> e.getName().equals(functionName))
                .findFirst();

        if (first.isPresent()) {
            cell.value = (String.valueOf(first.get().calculate(cell.getRelatedCellsId(), spreadsheet.getRows())));
        } else {
            cell.value = "NO FUNCTION";
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
                if (cell.isFunction) {
                    Optional<String> relatedColumn = cell.getRelatedCellsId().stream()
                            .filter(e -> e.equals(getColumnHeader(columnIndex) + number))
                            .findAny();

                    if (relatedColumn.isPresent()) {
                        calculateFunctionValue(cell);
                        int cellRowIndex = getRowNumberFromColumn(cell.cellId);
                        int cellColumnIndex = getColumnIndex(cell.cellId.replace(String.valueOf(cellRowIndex), ""));
                        grid.getDataProvider().refreshItem(row);

                        reloadRelated(cellRowIndex, cellColumnIndex, recursionDepth + 1);
                    }
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

    private void addBottomRowButtons() {
        // Add row button positioned at the bottom center
        Button addRowButton = new Button("Add New Row", e -> addRow());
        addRowButton.setClassName("option-button");
        Button addColumnButton = new Button("Add New Column", e -> addColumn());
        addColumnButton.setClassName("option-button");

        Button sortButton = new Button("Sort columns", e -> sortColumnsModal());
        sortButton.setClassName("option-button");

        Button refreshGrid = new Button("Refresh grid", e -> {
            refreshGrid();
            showSuccessNotification("Grid refreshed");
        });
        refreshGrid.setClassName("option-button");

        Button saveChanges = new Button("Save changes", e -> saveChanges());
        saveChanges.setClassName("option-button");

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomLayout.add(addRowButton);
        bottomLayout.add(addColumnButton);
        bottomLayout.add(sortButton);
        bottomLayout.add(refreshGrid);
        add(bottomLayout);
        add(saveChanges);
    }

    private void refreshFunctionCells() {
        //refresh all functions
        List<Cell> functionCells = new ArrayList<>();
        List<Cell> allCells = new ArrayList<>();
        Set<String> refreshedCells = new HashSet<>();

        for (SpreadsheetRow row : spreadsheet.getRows()) {
            for (Cell cell : row.getCells()) {
                if (cell.isFunction) {
                    functionCells.add(cell);
                } else {
                    refreshedCells.add(cell.cellId);
                }
                allCells.add(cell);
            }
        }

        while (refreshedCells.size() != allCells.size()) {
            majorCellsLoop:
            for (Cell functionCell : functionCells) {
                for (String relatedCell : functionCell.getRelatedCellsId()) {
                    if (!SpreadsheetUtils.isLiteralValue(relatedCell) && !refreshedCells.contains(relatedCell)) {
                        continue majorCellsLoop;
                    }
                }
                calculateFunctionValue(functionCell);
                refreshedCells.add(functionCell.cellId);
            }
        }
    }

    private void showAvailableFunctionsModal() {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");


        Button okButton = new Button("Ok", e -> {
            dialog.close();
        });
        okButton.setClassName("option-button");
        // Add components to dialog
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getDialogTopBarLayout(dialog));

        int i = 1;
        for (SpreadsheetFunction spreadsheetFunction : spreadsheetFunctions) {
            String name = spreadsheetFunction.getName();
            String info = spreadsheetFunction.getInfo();
            Span infoSpan = new Span();
            infoSpan.getElement().setProperty("innerHTML", info);
            verticalLayout.add(new Span(i + ") " + name), infoSpan);
            i++;
        }
        verticalLayout.add(okButton);
        dialog.add(verticalLayout);

        // Open the dialog
        dialog.open();
    }

    private void sortColumnsModal() {
        Dialog dialog = new Dialog();
        dialog.setWidth("30vw");

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

        Button okButton = new Button("Sort columns", e -> {
            UtilsMessage utilsMessage = sortColumns(spreadsheet, columnDropdown.getValue(),
                    orderDropdown.getValue(), columnsField.getValue(), rowsField.getValue(), grid);
            if (utilsMessage.isSuccess) {
                showSuccessNotification(utilsMessage.message);
                dialog.close();
            } else if (utilsMessage.isError) {
                showErrorNotification(utilsMessage.message);
            }
        });
        okButton.setClassName("option-button");
        // Add components to dialog
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getDialogTopBarLayout(dialog), columnsField, rowsField, columnDropdown, orderDropdown, okButton);
        dialog.add(verticalLayout);

        // Open the dialog
        dialog.open();
    }


    private void saveChanges() {
        try {
            saveInternal();
            showSuccessNotification("Saved!");
        } catch (Exception e) {
            showErrorNotification("Could not save spreadsheet!");
        }
    }

    private void saveInternal() {
        String body = SpreadsheetRowConverter.serializeSpreadsheetBody(spreadsheet.getRows());
        spreadsheet.setBody(body);
        String config = SpreadsheetRowConverter.serializeColumnsConfig(spreadsheet.getConfigs());
        spreadsheet.setColumnsConfig(config);
        service.save(spreadsheet);
    }

    private void addRow() {
        spreadsheet.addRow();
        grid.getDataProvider().refreshAll();
    }

    private void addColumn() {
        spreadsheet.addColumn();
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void duplicateRow(SpreadsheetRow row) {
        spreadsheet.duplicateRow(row);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void addRowBelow(SpreadsheetRow row) {
        spreadsheet.duplicateRow(row);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void deleteRow(SpreadsheetRow row) {
        spreadsheet.removeRow(row);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void clearDataInRow(SpreadsheetRow row) {
        spreadsheet.clearDataInRow(row);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void clearColumnData(int columnNumber) {
        spreadsheet.clearColumnData(columnNumber);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void duplicateColumn(int columnIndex) {
        spreadsheet.duplicateColumn(columnIndex);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }

    private void deleteColumn(int columnIndex) {
        spreadsheet.removeColumn(columnIndex);
        refreshGrid();
        grid.getDataProvider().refreshAll();
    }
}