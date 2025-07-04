package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanButton;
import com.bervan.common.BervanTextField;
import com.bervan.common.model.UtilsMessage;
import com.bervan.common.service.AuthService;
import com.bervan.core.model.BervanLogger;
import com.bervan.spreadsheet.functions.SpreadsheetFunction;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.HistorySpreadsheet;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.service.HistorySpreadsheetRepository;
import com.bervan.spreadsheet.service.SpreadsheetRepository;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import com.bervan.spreadsheet.view.utils.StylingOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bervan.spreadsheet.utils.SpreadsheetUtils.sortColumns;

public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private static final int MAX_RECURSION_DEPTH = 100;
    private final SpreadsheetService spreadsheetService;
    private final BervanLogger logger;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Div historyHtml = new Div();
    private final Set<Cell> selectedCells = new HashSet<>();
    @Autowired
    private SpreadsheetRepository spreadsheetRepository;
    @Autowired
    private HistorySpreadsheetRepository historySpreadsheetRepository;
    private List<SpreadsheetFunction> spreadsheetFunctions;
    private Spreadsheet spreadsheetEntity;
    private Div tableHtml;
    private int rows;
    private int columns;
    private Cell[][] cells;
    private boolean historyShow = false;
    private List<HistorySpreadsheet> sorted = new ArrayList<>();
    private Button clearSelectionButton;

    private Cell focusedCell; // Keep track of the currently focused cell

    // Map for quick access to cells by their ID
    private Map<String, Cell> cellMap = new HashMap<>();

    public AbstractSpreadsheetView(SpreadsheetService service, BervanLogger logger, List<? extends SpreadsheetFunction> spreadsheetFunctions) {
        this.spreadsheetService = service;
        this.logger = logger;
        this.spreadsheetFunctions = (List<SpreadsheetFunction>) spreadsheetFunctions;
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private void init(String name) {

        // Load or create Spreadsheet
        List<Spreadsheet> optionalEntity = spreadsheetRepository.findByNameAndDeletedFalseAndOwnersId(name, AuthService.getLoggedUserId());

        if (optionalEntity.size() > 0) {
            spreadsheetEntity = optionalEntity.get(0);
            String body = spreadsheetEntity.getBody();

            // Deserialize body to cells
            try {
                for (Field declaredField : Cell.class.getDeclaredFields()) {
                    declaredField.setAccessible(true);
                }

                cells = objectMapper.readValue(body, Cell[][].class);
                rows = cells.length;
                columns = cells[0].length;

                for (Field declaredField : Cell.class.getDeclaredFields()) {
                    declaredField.setAccessible(false);
                }

                // Rebuild cell map
                rebuildCellMap();

            } catch (Exception e) {
                logger.error(e);
                // Initialize default values on error
                rows = 2;
                columns = 10;
                initializeCells();
            }
        } else {
            spreadsheetEntity = new Spreadsheet(name);
            rows = 2;
            columns = 10;
            initializeCells();
        }

        // Initial table
        tableHtml = new Div();
        tableHtml.getElement().setProperty("innerHTML", buildTable(columns, rows, cells));

        // Refresh all functions before building the table
        refreshAllFunctions();

        // Create the MenuBar
        MenuBar menuBar = new MenuBar();

        // File menu
        MenuItem fileMenu = menuBar.addItem("File");
        fileMenu.addClassName("option-button");
        fileMenuOptions(fileMenu);

        // Edit menu
        MenuItem editMenu = menuBar.addItem("Edit");
        editMenu.addClassName("option-button");
        editMenuOptions(editMenu);

        // Styling menu
        MenuItem stylingMenu = menuBar.addItem("Styling Column");
        stylingMenu.addClassName("option-button");
        StylingOptions stylingOptions = new StylingOptions();
        stylingOptions.stylingMenuOptions(stylingMenu, selectedCells, focusedCell, unused -> {
            refreshTable();
            return null;
        });

        // Help menu
        MenuItem helpMenu = menuBar.addItem("Help");
        helpMenu.addClassName("option-button");
        helpMenuOptions(helpMenu);

        // Add the MenuBar and table to the layout
        Button saveButton = new BervanButton("Save");
        saveButton.addClickListener(event -> {
            save();
        });

        clearSelectionButton = new Button("Clear Selection", event -> {
            clearSelection();
        });
        clearSelectionButton.addClassName("option-button");
        clearSelectionButton.setVisible(false);

        add(clearSelectionButton, menuBar, tableHtml, saveButton, new Hr(), historyHtml);

        refreshTable();
    }

    private void refreshClearSelectionButtonVisibility() {
        boolean hasSelection = !selectedCells.isEmpty();
        clearSelectionButton.setVisible(hasSelection);
    }

    private void clearSelection() {
        for (Cell cell : selectedCells) {
            getElement().executeJs("document.getElementById($0).style.backgroundColor = ''", cell.getCellId());
        }
        selectedCells.clear();
        refreshClearSelectionButtonVisibility();
    }

    private String buildTable(int columns, int rows, Cell[][] cells) {
        return buildTable(columns, rows, cells, null, true);
    }

    private String buildTable(int columns, int rows, Cell[][] cells, Set<String> changedCellIds) {
        return buildTable(columns, rows, cells, changedCellIds, true);
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
            UtilsMessage utilsMessage = sortColumns(cells, columnDropdown.getValue(),
                    orderDropdown.getValue(), columnsField.getValue(), rowsField.getValue());
            if (utilsMessage.isSuccess) {
                refreshTable();
                showSuccessNotification(utilsMessage.message);
                dialog.close();
            } else if (utilsMessage.isError) {
                showErrorNotification(utilsMessage.message);
            }
        });
        okButton.addClassName("option-button");
        // Add components to dialog
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getDialogTopBarLayout(dialog), columnsField, rowsField, columnDropdown, orderDropdown, okButton);
        dialog.add(verticalLayout);

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

                boolean allRowsMatch = columnsMap.values().stream().allMatch(cells -> {
                    List<Integer> rowNumbers = cells.stream().map(cell -> cell.getRowNumber()).sorted().toList();
                    return rowNumbers.equals(referenceRowNumbers);
                });

                if (allRowsMatch) {
                    boolean allContinuous = true;
                    int minRow = 0;
                    int maxRow = 0;
                    for (Map.Entry<String, List<Cell>> entry : columnsMap.entrySet()) {
                        String column = entry.getKey();
                        List<Cell> cells = entry.getValue();

                        minRow = cells.stream().mapToInt(c -> c.getRowNumber()).min().orElseThrow();
                        maxRow = cells.stream().mapToInt(c -> c.getRowNumber()).max().orElseThrow();

                        Set<Integer> rowNumbers = cells.stream().map(cell -> cell.getRowNumber()).collect(Collectors.toSet());

                        for (int i = minRow; i <= maxRow; i++) {
                            if (!rowNumbers.contains(i)) {
                                allContinuous = false;
                                break;
                            }
                        }
                    }

                    if (allContinuous) {
                        showPrimaryNotification("Sort column properties applied based on selection!");
                        columnsField.setValue(String.join(",", columnsMap.keySet()));
                        rowsField.setValue(minRow + ":" + maxRow);
                    } else {
                        showErrorNotification("Sort column properties cannot be applied based on selection!");
                    }
                } else {
                    showErrorNotification("Sort column properties cannot be applied based on selection!");
                }
            }
        }

        // Open the dialog
        dialog.open();
    }

    private void helpMenuOptions(MenuItem helpMenu) {
        SubMenu helpSubMenu = helpMenu.getSubMenu();
        MenuItem showFunctionsItem = helpSubMenu.addItem("Show Available Functions", event -> {
            showAvailableFunctionsModal();
        });
    }

    private void editMenuOptions(MenuItem editMenu) {
        SubMenu editSubMenu = editMenu.getSubMenu();

        MenuItem addRowItem = editSubMenu.addItem("Add Row", event -> {
            rows++;
            updateCellsArray();
            refreshTable();
        });

        MenuItem addColumnItem = editSubMenu.addItem("Add Column", event -> {
            columns++;
            updateCellsArray();
            refreshTable();
        });

        MenuItem sortColumnsItem = editSubMenu.addItem("Sort Columns", event -> {
            sortColumnsModal();
        });

        MenuItem refreshTableItem = editSubMenu.addItem("Refresh Table", event -> {
            refreshTable();
            showSuccessNotification("Table refreshed");
        });
    }


    private void fileMenuOptions(MenuItem fileMenu) {
        SubMenu fileSubMenu = fileMenu.getSubMenu();

        MenuItem saveItem = fileSubMenu.addItem("Save", event -> {
            save();
        });

        MenuItem pasteItem = fileSubMenu.addItem("Paste", event -> {
            handlePasteAction();
        });

        MenuItem copyTableItem = fileSubMenu.addItem("Copy Table", event -> {
            showCopyTableDialog();
        });

        MenuItem showHistory = fileSubMenu.addItem("History", event -> {
            historyShow = !historyShow;
            if (historyShow) {
                reloadHistory();
                if (sorted.size() > 0) {
                    showHistoryTable(0);
                }
            }
        });
    }

    private void save() {
        try {
            String body = objectMapper.writeValueAsString(cells);
            spreadsheetEntity.setBody(body);
            spreadsheetRepository.save(spreadsheetEntity);
            reloadHistory();
            showSuccessNotification("Table saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorNotification("Failed to save table.");
        }
    }

    private void showHistoryTable(int historyIndex) {
        // Mark as red all different values compared to current
        HistorySpreadsheet historySpreadsheet = sorted.get(historyIndex);
        String tableHTML = "";
        try {
            Cell[][] historyCells = objectMapper.readValue(historySpreadsheet.getBody(), Cell[][].class);
            int historyRows = historyCells.length;
            int historyColumns = historyCells[0].length;

            // Compute the set of cell IDs that have changed
            Set<String> changedCellIds = new HashSet<>();

            for (int row = 0; row < historyRows; row++) {
                for (int col = 0; col < historyColumns; col++) {
                    Cell currentCell = null;
                    if (row < cells.length && col < cells[0].length) {
                        currentCell = cells[row][col];
                    }
                    Cell historyCell = historyCells[row][col];

                    String currentValue = currentCell != null && currentCell.getValue() != null ? currentCell.getValue() : "";
                    String historyValue = historyCell.getValue() != null ? historyCell.getValue() : "";

                    if (!currentValue.equals(historyValue)) {
                        changedCellIds.add(historyCell.getCellId());
                    }
                }
            }

            tableHTML = buildTable(historyColumns, historyRows, historyCells, changedCellIds, false);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorNotification("Could not show history change!");
        }

        String headerHTML = historySpreadsheet.getUpdateDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Div historyContent = new Div();
        historyContent.getElement().setProperty("innerHTML", headerHTML + "<br>" + tableHTML);

        // Create navigation buttons
        Button leftButton = new Button("Previous");
        leftButton.setVisible(historyIndex > 0); // Disable if at the first history
        leftButton.addClickListener(event -> showHistoryTable(historyIndex - 1));
        leftButton.addClassName("option-button");

        Button rightButton = new Button("Next");
        rightButton.addClassName("option-button");
        rightButton.setVisible(historyIndex < sorted.size() - 1); // Disable if at the last history
        rightButton.addClickListener(event -> showHistoryTable(historyIndex + 1));

        // Add buttons to the layout
        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.add(leftButton, rightButton);

        // Remove old content and re-add new components
        historyHtml.getElement().removeAllChildren();
        historyHtml.getElement().appendChild(historyContent.getElement(), new Hr().getElement(), buttonContainer.getElement());
    }

    private void reloadHistory() {
        List<HistorySpreadsheet> history = historySpreadsheetRepository.findAllByHistoryOwnerId(spreadsheetEntity.getId());
        sorted = history.stream().sorted(Comparator.comparing(HistorySpreadsheet::getUpdateDate).reversed()).collect(Collectors.toList());
    }

    private void refreshTable() {
        tableHtml.getElement().setProperty("innerHTML", buildTable(columns, rows, cells));

        getElement().executeJs("""
                    const table = this.querySelector('table');
                    let focusedCellId = null;
                
                    table.addEventListener('click', event => {
                        const cell = event.target.closest('td');
                        if (cell && cell.id) {
                            if (event.shiftKey) {
                                if (cell.classList.contains('selected-cell')) {
                                    cell.classList.remove('selected-cell');
                                    $0.$server.removeSelectedCell(cell.id);
                                } else {
                                    cell.classList.add('selected-cell');
                                    $0.$server.addSelectedCell(cell.id);
                                }
                            } else if (cell.hasAttribute('contenteditable')) {
                                const id = cell.id;
                                focusedCellId = id;
                                $0.$server.cellFocusIn(id);
                                $0.$server.updateStylingMenuVisibility(true);
                            }
                        } else if (event.target.tagName === 'TH') {
                            if (event.shiftKey) {
                                let allTd = document.querySelectorAll("td");
                                let atLeastOneMarkedAlready = false;
                                let atLeastOneNotMarkedAlready = false;
                                let tdToSwitch = [];
                
                                for (let i = 0; i < allTd.length; i++) {
                                    if (allTd[i].id.startsWith(event.target.innerText)) {
                                        tdToSwitch.push(allTd[i]);
                
                                        if (allTd[i].style.backgroundColor === 'green') {
                                            atLeastOneMarkedAlready = true;
                                        } else {
                                            atLeastOneNotMarkedAlready = true;
                                        }
                                    }
                                }
                
                                for (let i = 0; i < tdToSwitch.length; i++) {
                                    if (atLeastOneNotMarkedAlready) {
                                        tdToSwitch[i].style.backgroundColor = 'green';
                                        $0.$server.addSelectedCell(tdToSwitch[i].id);
                                    } else {
                                        tdToSwitch[i].style.backgroundColor = '';
                                        $0.$server.removeSelectedCell(tdToSwitch[i].id);
                                    }
                                }
                            }
                        }
                    });
                
                    table.addEventListener('focusout', event => {
                        const cell = event.target;
                        if (cell.hasAttribute('contenteditable')) {
                            const id = cell.id;
                            const htmlContent = cell.innerHTML;
                            $0.$server.updateCellValue(id, htmlContent);
                        }
                    });
                
                    window.addEventListener('click', event => {
                        const cell = event.target.closest('td');
                        const menuBar = document.querySelector('vaadin-menu-bar');
                        if (!cell && !menuBar.contains(event.target)) {
                            $0.$server.updateStylingMenuVisibility(false);
                        }
                    });
                """, getElement());
    }

    @ClientCallable
    public void updateStylingMenuVisibility(boolean visible) {

    }

    @ClientCallable
    public void addSelectedCell(String cellId) {
        Cell cell = cellMap.get(cellId);
        if (cell != null) {
            selectedCells.add(cell);
            refreshClearSelectionButtonVisibility();
        }
    }

    @ClientCallable
    public void removeSelectedCell(String cellId) {
        Cell cell = cellMap.get(cellId);
        if (cell != null) {
            selectedCells.remove(cell);
            refreshClearSelectionButtonVisibility();
        }
    }

    @ClientCallable
    public void cellFocusIn(String cellId) {
        focusedCell = cellMap.get(cellId); // Find the focused cell by ID

        if (focusedCell != null) {
            if (focusedCell.isFunction()) {
                String formula = focusedCell.getFunctionValue();
                updateCellInClient(focusedCell.getCellId(), formula);
            }
        }
    }

    private void initializeCells() {
        cells = new Cell[rows][columns];
        cellMap.clear();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = new Cell("", j, i);
                cell.setHtmlContent("");
                cells[i][j] = cell;
                cellMap.put(cell.getCellId(), cell);
            }
        }
    }

    private void updateCellsArray() {
        Cell[][] newCells = new Cell[rows][columns];

        // Copy existing values to the new array
        for (int i = 0; i < Math.min(cells.length, rows); i++) {
            for (int j = 0; j < Math.min(cells[0].length, columns); j++) {
                newCells[i][j] = cells[i][j];
            }
        }

        // Initialize new cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (newCells[i][j] == null) {
                    Cell cell = new Cell("", j, i);
                    cell.setHtmlContent("");
                    newCells[i][j] = cell;
                    cellMap.put(cell.getCellId(), cell);
                } else {
                    // Update cell metadata
                    Cell cell = newCells[i][j];
                    cell.setColumnNumber(j);
                    cell.setRowNumber(i);
                    cell.setColumnSymbol(getColumnName(j));
                    cell.setCellId(cell.getColumnSymbol() + cell.getRowNumber());
                    cellMap.put(cell.getCellId(), cell);
                }
            }
        }

        cells = newCells;
    }

    private void rebuildCellMap() {
        cellMap.clear();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                // Update cell IDs to start row numbering from 0
                cell.setColumnNumber(j);
                cell.setRowNumber(i);
                cell.setColumnSymbol(getColumnName(j));
                cell.setCellId(cell.getColumnSymbol() + cell.getRowNumber());
                if (cell.getHtmlContent() == null) {
                    cell.setHtmlContent(cell.getValue() != null ? cell.getValue() : "");
                }
                cellMap.put(cell.getCellId(), cell);
            }
        }
    }

    private String buildTable(int columns, int rows, Cell[][] cells, Set<String> changedCellIds, boolean isEditable) {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<table class='spreadsheet-table'>");

        // Build header row with column labels
        tableBuilder.append("<tr class='spreadsheet-header-row'>");
        tableBuilder.append("<th class='spreadsheet-row-number'>#</th>");
        for (int col = 0; col < columns; col++) {
            String columnLabel = getColumnName(col);
            String cellId = "header_" + col;
            tableBuilder.append("<th ")
                    .append("id='").append(cellId).append("' ")
                    .append("class='spreadsheet-header'>");
            tableBuilder.append(columnLabel);
            tableBuilder.append("</th>");
        }
        tableBuilder.append("</tr>");

        // Build data rows
        for (int row = 0; row < rows; row++) {
            tableBuilder.append("<tr>");

            // Row number cell
            tableBuilder.append("<td class='spreadsheet-row-number'>")
                    .append(row)
                    .append("</td>");

            for (int col = 0; col < columns; col++) {
                Cell cell = cells[row][col];
                String cellId = cell.getCellId();

                tableBuilder.append("<td ")
                        .append("id='").append(cellId).append("' ")
                        .append("contenteditable='").append(isEditable).append("' ");

                if (cell.isFunction()) {
                    if (selectedCells.contains(cell)) {
                        tableBuilder.append("class='spreadsheet-cell selected-cell spreadsheet-cell-function'");
                    } else {
                        tableBuilder.append("class='spreadsheet-cell spreadsheet-cell-function'");
                    }
                } else {
                    if (selectedCells.contains(cell)) {
                        tableBuilder.append("class='spreadsheet-cell selected-cell'");
                    } else {
                        tableBuilder.append("class='spreadsheet-cell'");
                    }
                }


                // If the cell is in changedCellIds, add style
                if (changedCellIds != null && changedCellIds.contains(cellId)) {
                    tableBuilder.append(" style='background-color:red; color:white;' ");
                }

                tableBuilder.append(">");

                if (cell.isFunction()) {
                    cell.buildFunction(cell.getFunctionValue());
                }

                String val = cell.getHtmlContent() != null ? cell.getHtmlContent() : "";
                tableBuilder.append(val);
                tableBuilder.append("</td>");
            }
            tableBuilder.append("</tr>");
        }

        tableBuilder.append("</table>");
        return tableBuilder.toString();
    }

    // Method to generate Excel-like column labels
    private String getColumnName(int columnIndex) {
        return SpreadsheetUtils.getColumnHeader(columnIndex);
    }

    @ClientCallable
    public void updateCellValue(String cellId, String htmlContent) {
        Cell cell = cellMap.get(cellId);
        if (cell != null) {
            // Update the cell's htmlContent
            cell.setHtmlContent(htmlContent);

            // Extract plain text value from htmlContent
            String value = Jsoup.parse(htmlContent).text();
            cell.setValue(value);

            // If value starts with '=', it's a function
            if (value.startsWith("=")) {
                cell.isFunction(true);
                cell.buildFunction(value);
            } else {
                cell.isFunction(false);
                cell.setFunctionName(null);
                cell.setValue(value);
            }

            // Recalculate dependent cells
            try {
                recalculateCell(cell);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorNotification("Error in formula calculation.");
            }

            // Update the cell in the client-side
            updateCellInClient(cell.getCellId(), cell.getHtmlContent());
        }
    }

    // New method to update a cell in the client-side
    private void updateCellInClient(String cellId, String htmlContent) {
        getElement().executeJs(
                "const cell = document.getElementById($0);" +
                        "if (cell) { cell.innerHTML = $1; }",
                cellId, htmlContent
        );
    }

    private void recalculateCell(Cell cell) {
        recalculateCell(cell, new HashSet<>(), 0);
    }

    private void recalculateCell(Cell cell, Set<String> visitedCells, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            System.out.println("Recursion limit exceeded for cell: " + cell.getCellId());
            return;
        }

        if (visitedCells.contains(cell.getCellId())) {
            return;
        }

        visitedCells.add(cell.getCellId());

        if (cell.isFunction()) {
            calculateFunctionValue(cell);
        }

        // Update the cell in the client-side
        updateCellInClient(cell.getCellId(), cell.getHtmlContent());

        // Now, find and update any cells that depend on this cell
        for (Cell dependentCell : getDependentCells(cell.getCellId())) {
            recalculateCell(dependentCell, visitedCells, depth + 1);
        }
    }

    private void showAvailableFunctionsModal() {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");

        Button okButton = new BervanButton("Ok", e -> {
            dialog.close();
        });

        // Add components to dialog
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getDialogTopBarLayout(dialog));

        int i = 1;
        for (SpreadsheetFunction spreadsheetFunction : spreadsheetFunctions) {
            String name = spreadsheetFunction.getName();
            String info = spreadsheetFunction.getInfo();
            Span infoSpan = new Span();
            infoSpan.addClassName("spreadsheet-function-info");
            infoSpan.getElement().setProperty("innerHTML", info);
            verticalLayout.add(new Span(i + ") " + name), infoSpan);
            i++;
        }
        verticalLayout.add(okButton);
        dialog.add(verticalLayout);

        // Open the dialog
        dialog.open();
    }

    private void calculateFunctionValue(Cell cell) {
        String functionName = cell.getFunctionName();
        Optional<? extends SpreadsheetFunction> first = spreadsheetFunctions.stream()
                .filter(e -> e.getName().equals(functionName))
                .findFirst();

        if (first.isPresent()) {
            cell.setValue(String.valueOf(first.get().calculate(cell.getRelatedCellsId(), getRows())));
            cell.setHtmlContent(cell.getValue());
        } else {
            cell.setValue("NO FUNCTION");
            cell.setHtmlContent(cell.getValue());
        }
    }

    private List<List<Cell>> getRows() {
        List<List<Cell>> rowsList = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Cell> rowList = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                rowList.add(cells[i][j]);
            }
            rowsList.add(rowList);
        }
        return rowsList;
    }

    private Set<Cell> getDependentCells(String cellId) {
        Set<Cell> dependents = new HashSet<>();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                if (cell.isFunction() && cell.getRelatedCellsId().contains(cellId)) {
                    dependents.add(cell);
                }
            }
        }
        return dependents;
    }

    private void refreshAllFunctions() {
        // Recalculate all functions and update them individually
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                if (cell.isFunction()) {
                    try {
                        recalculateCell(cell);
                    } catch (Exception e) {
                        e.printStackTrace();
                        cell.setValue("ERROR");
                        cell.setHtmlContent(cell.getValue());
                        updateCellInClient(cell.getCellId(), cell.getHtmlContent());
                    }
                }
            }
        }
        // No need to refresh the table since cells have been updated individually
    }

    @ClientCallable
    public void showSuccessNotification(String message) {
        super.showSuccessNotification(message);
    }

    @ClientCallable
    public void showErrorNotification(String message) {
        super.showErrorNotification(message);
    }

    // Method to show the copy table dialog
    private void showCopyTableDialog() {
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

                boolean allRowsMatch = columnsMap.values().stream().allMatch(cells -> {
                    List<Integer> rowNumbers = cells.stream().map(cell -> cell.getRowNumber()).sorted().toList();
                    return rowNumbers.equals(referenceRowNumbers);
                });

                if (allRowsMatch) {
                    boolean allContinuous = true;
                    int minRow = 0;
                    int maxRow = 0;
                    for (Map.Entry<String, List<Cell>> entry : columnsMap.entrySet()) {
                        String column = entry.getKey();
                        List<Cell> cells = entry.getValue();

                        minRow = cells.stream().mapToInt(c -> c.getRowNumber()).min().orElseThrow();
                        maxRow = cells.stream().mapToInt(c -> c.getRowNumber()).max().orElseThrow();

                        Set<Integer> rowNumbers = cells.stream().map(cell -> cell.getRowNumber())
                                .collect(Collectors.toSet());

                        for (int i = minRow; i <= maxRow; i++) {
                            if (!rowNumbers.contains(i)) {
                                allContinuous = false;
                                break;
                            }
                        }
                    }

                    if (allContinuous) {
                        showPrimaryNotification("Copy table properties applied based on selection!");
                        tableText = generateTableText(selectedCells.stream().toList());
                    } else {
                        showErrorNotification("Copy table properties cannot be applied!");
                    }
                } else {
                    showErrorNotification("Copy table properties cannot be applied!");
                }
            }
        }

        if (tableText.isEmpty()) {
            tableText = generateTableText();
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
    private String generateTableText() {
        StringBuilder sb = new StringBuilder();

        // Build header row
        sb.append("#\t");
        for (int col = 0; col < columns; col++) {
            sb.append(getColumnName(col)).append("\t");
        }
        sb.append("\n");

        // Build data rows
        for (int row = 0; row < rows; row++) {
            sb.append(row).append("\t");
            for (int col = 0; col < columns; col++) {
                Cell cell = cells[row][col];
                String val = (cell != null && cell.getHtmlContent() != null) ? Jsoup.parse(cell.getHtmlContent()).text().replaceAll("\n", " ")
                        .replaceAll("\t", " ") : "";
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String generateTableText(List<Cell> cellList) {
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
            sb.append(getColumnName(col)).append("\t");
        }
        sb.append("\n");

        Map<String, Cell> cellMap = cellList.stream()
                .collect(Collectors.toMap(
                        cell -> cell.getColumnNumber() + "_" + cell.getRowNumber(),
                        cell -> cell
                ));

        for (int row = minRow; row <= maxRow; row++) {
            sb.append(row).append("\t");
            for (int col = minCol; col <= maxCol; col++) {
                Cell cell = cellMap.get(col + "_" + row);
                String val = (cell != null && cell.getHtmlContent() != null) ? Jsoup.parse(cell.getHtmlContent()).text().replaceAll("\n", " ")
                        .replaceAll("\t", " ") : "";
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // New method to handle the Paste action
    private void handlePasteAction() {
        if (focusedCell == null) {
            showErrorNotification("No cell is focused. Please focus on a cell to determine the starting point for pasting.");
            return;
        }

        // Show a dialog to get the clipboard content
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeight("400px");

        TextArea clipboardContentArea = new TextArea("Paste your data here");
        clipboardContentArea.setWidthFull();
        clipboardContentArea.setHeight("250px");

        Button pasteButton = new Button("Paste", event -> {
            String clipboardContent = clipboardContentArea.getValue();
            if (clipboardContent == null || clipboardContent.isEmpty()) {
                showErrorNotification("Clipboard is empty.");
                return;
            }

            // Process the clipboard content
            processClipboardContent(clipboardContent);
            dialog.close();
        });

        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(pasteButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(clipboardContentArea, buttonsLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void processClipboardContent(String clipboardContent) {
        // Split the content into rows
        String[] rowsData = clipboardContent.split("\n");
        int dataRowCount = rowsData.length;
        int dataColumnCount = 0;

        List<List<String>> data = new ArrayList<>();

        for (String rowData : rowsData) {
            // Split each row into columns
            String[] columnsData = rowData.split("\t");
            dataColumnCount = Math.max(dataColumnCount, columnsData.length);
            List<String> rowValues = Arrays.asList(columnsData);
            data.add(rowValues);
        }

        // Determine starting position
        int startColumn = focusedCell.getColumnNumber();
        int startRow = 0; // You can adjust this if you want to start from a specific row

        // Check if the table needs to be expanded
        int requiredRows = startRow + dataRowCount;
        int requiredColumns = startColumn + dataColumnCount;

        boolean needExpansion = requiredRows > rows || requiredColumns > columns;

        if (needExpansion) {
            rows = Math.max(rows, requiredRows);
            columns = Math.max(columns, requiredColumns);
            updateCellsArray();
        }

        // Check if any existing cells will be overwritten
        boolean willOverwrite = false;
        for (int i = 0; i < dataRowCount; i++) {
            for (int j = 0; j < dataColumnCount; j++) {
                int currentRow = startRow + i;
                int currentColumn = startColumn + j;
                if (currentRow < cells.length && currentColumn < cells[0].length) {
                    Cell cell = cells[currentRow][currentColumn];
                    if (cell != null && (cell.getValue() != null && !cell.getValue().isEmpty())) {
                        willOverwrite = true;
                        break;
                    }
                }
            }
            if (willOverwrite) {
                break;
            }
        }

        if (willOverwrite) {
            // Ask for confirmation
            Dialog confirmDialog = new Dialog();
            confirmDialog.setWidth("400px");
            confirmDialog.setHeight("200px");
            Span message = new Span("Some cells already contain data. Are you sure you want to overwrite them?");
            Button yesButton = new Button("Yes", event -> {
                pasteDataIntoCells(data, startRow, startColumn);
                confirmDialog.close();
            });
            Button noButton = new Button("No", event -> {
                confirmDialog.close();
            });
            HorizontalLayout buttonsLayout = new HorizontalLayout(yesButton, noButton);
            VerticalLayout dialogLayout = new VerticalLayout(message, buttonsLayout);
            confirmDialog.add(dialogLayout);
            confirmDialog.open();
        } else {
            pasteDataIntoCells(data, startRow, startColumn);
        }
    }

    private void pasteDataIntoCells(List<List<String>> data, int startRow, int startColumn) {
        for (int i = 0; i < data.size(); i++) {
            List<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                String cellValue = rowData.get(j);
                int currentRow = startRow + i;
                int currentColumn = startColumn + j;
                if (currentRow < cells.length && currentColumn < cells[0].length) {
                    Cell cell = cells[currentRow][currentColumn];
                    if (cell != null) {
                        cell.setValue(cellValue);
                        cell.setHtmlContent(cellValue);
                        // Update the cell in the client-side
                        updateCellInClient(cell.getCellId(), cell.getHtmlContent());
                    }
                }
            }
        }
        refreshTable();
        showSuccessNotification("Data pasted successfully.");
    }
}