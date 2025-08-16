package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.spreadsheet.functions.CellReferenceArgument;
import com.bervan.spreadsheet.functions.FunctionArgument;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@JsModule("./spreadsheet-context-menu.js")
@CssImport("./spreadsheet.css")
public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private final SpreadsheetService spreadsheetService;
    private Spreadsheet spreadsheet;
    private List<SpreadsheetRow> rows;
    private TextArea infoTextArea = new TextArea("");

    public AbstractSpreadsheetView(SpreadsheetService service) {
        this.spreadsheetService = service;
        infoTextArea.getStyle().setColor("white");
        infoTextArea.setSizeFull();
        infoTextArea.setVisible(false);
    }

    private static List<SpreadsheetRow> getSpreadsheetRows(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.readValue(body, new TypeReference<List<SpreadsheetRow>>() {
        });
    }

    @ClientCallable
    public void onCellEdit(String cellId, String value) {
        SpreadsheetCell cell = SpreadsheetService.findCellById(rows, cellId);
        if (cell != null) {
            cell.setNewValueAndCellRelatedFields(value);
            refreshView(rows);
        }
    }

    @ClientCallable
    public void addColumnLeft(Integer columnNumber) {
        spreadsheetService.addColumnLeft(rows, null, columnNumber);
        refreshView(rows);
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " added!");
    }

    @ClientCallable
    public void addColumnRight(Integer columnNumber) {
        spreadsheetService.addColumnRight(rows, null, columnNumber);
        refreshView(rows);
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber + 1) + " added!");
    }

    @ClientCallable
    public void duplicateColumn(Integer columnNumber) {
        spreadsheetService.duplicateColumn(rows, columnNumber);
        refreshView(rows);
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " duplicated!");
    }

    @ClientCallable
    public void deleteColumn(Integer columnNumber) {
        StringBuilder confirmationMessageDetails = new StringBuilder();
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                //collect all formulas except formulas in column with columnNumber
                if (cell.hasFormula() && cell.getColumnNumber() != columnNumber) {
                    List<FunctionArgument> functionArguments = spreadsheetService.getFunctionArguments(rows, cell.getFormula());
                    Set<FunctionArgument> formulasThatUseColumnToBeDeleted = functionArguments.stream().filter(e -> e instanceof CellReferenceArgument)
                            .filter(e -> ((CellReferenceArgument) e).getCell().getColumnNumber() == columnNumber)
                            .collect(Collectors.toSet());
                    if (!formulasThatUseColumnToBeDeleted.isEmpty()) {
                        confirmationMessageDetails.append(" ").append(cell.getCellId());
                    }
                }
            }
        }

        if (!confirmationMessageDetails.isEmpty()) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Column " + SpreadsheetUtils.getColumnHeader(columnNumber));
            confirmDialog.setText("Are you sure you want to delete this column? It contains cells used in other formulas: "
                    + confirmationMessageDetails);

            confirmDialog.setConfirmText("Delete Column: " + SpreadsheetUtils.getColumnHeader(columnNumber));
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.addConfirmListener(event -> {
                deleteColumnConfirmed(columnNumber);
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        } else {
            deleteColumnConfirmed(columnNumber);
        }
    }

    private void deleteColumnConfirmed(Integer columnNumber) {
        spreadsheetService.deleteColumn(rows, columnNumber);
        refreshView(rows);
        showPrimaryNotification("Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " deleted!");
    }

    @ClientCallable
    public void addRowAbove(Integer rowNumber) {
        spreadsheetService.addRowAbove(rows, null, rowNumber);
        refreshView(rows);
        showPrimaryNotification(" Row " + rowNumber + " added!");
    }

    @ClientCallable
    public void addRowBelow(Integer rowNumber) {
        spreadsheetService.addRowBelow(rows, null, rowNumber);
        refreshView(rows);
        showPrimaryNotification(" Row " + (rowNumber + 1) + " added!");
    }

    @ClientCallable
    public void duplicateRow(Integer rowNumber) {
        spreadsheetService.duplicateRow(rows, rowNumber);
        refreshView(rows);
        showPrimaryNotification(" Row " + (rowNumber + 1) + " duplicated!");
    }

    @ClientCallable
    public void deleteRow(Integer rowNumber) {
        StringBuilder confirmationMessageDetails = new StringBuilder();
        for (SpreadsheetRow row : rows) {
            for (SpreadsheetCell cell : row.getCells()) {
                //collect all formulas except formulas in row with rowNumber
                if (cell.hasFormula() && cell.getColumnNumber() != rowNumber) {
                    List<FunctionArgument> functionArguments = spreadsheetService.getFunctionArguments(rows, cell.getFormula());
                    Set<FunctionArgument> formulasThatUseRowToBeDeleted = functionArguments.stream().filter(e -> e instanceof CellReferenceArgument)
                            .filter(e -> ((CellReferenceArgument) e).getCell().getRowNumber() == rowNumber)
                            .collect(Collectors.toSet());
                    if (!formulasThatUseRowToBeDeleted.isEmpty()) {
                        confirmationMessageDetails.append(" ").append(cell.getCellId());
                    }
                }
            }
        }

        if (!confirmationMessageDetails.isEmpty()) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Row " + rowNumber);
            confirmDialog.setText("Are you sure you want to delete this row? It contains cells used in other formulas: "
                    + confirmationMessageDetails);

            confirmDialog.setConfirmText("Delete Row: " + rowNumber);
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.addConfirmListener(event -> {
                deleteRowConfirmed(rowNumber);
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        } else {
            deleteRowConfirmed(rowNumber);
        }
    }

    private void deleteRowConfirmed(Integer rowNumber) {
        spreadsheetService.deleteRow(rows, rowNumber);
        refreshView(rows);
        showPrimaryNotification("Row " + rowNumber + " deleted!");
    }


    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private void init(String spreadsheetName) {
        // Load spreadsheet rows from service
        spreadsheet = getOrCreate(spreadsheetName);
        rows = getRows(spreadsheetName);
        spreadsheetService.evaluateAllFormulas(rows);
        refreshView(rows);
        add(infoTextArea);
    }

    private HtmlSpreadsheet createHTMLTable(List<SpreadsheetRow> rows) {
        return new HtmlSpreadsheet(rows, getElement());
    }

    private VerticalLayout createSpreadsheetLayout(List<SpreadsheetRow> rows) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout actionPanel = getToolbarPanel();
        Button addRowBtn = getAddRowOptBtn(rows);
        Button addColumnBtn = getAddColumnOptBtn(rows);
        Button save = getSaveOptButton();
        Button importData = getImportDataOptButton();
        Button exportData = getExportOptButton();

        Div gap = new Div();
        gap.setWidth("10px");
        actionPanel.add(addRowBtn, addColumnBtn, save, gap, importData, exportData);

        Div tableDiv = createHTMLTable(rows);
        tableDiv.getElement().executeJs("initContextMenu($0)", getElement());

        layout.add(actionPanel, tableDiv);
        layout.setFlexGrow(1, tableDiv);
        layout.setSizeFull();
        return layout;
    }

    private HorizontalLayout getToolbarPanel() {
        HorizontalLayout actionPanel = new HorizontalLayout();
        actionPanel.addClassName("spreadsheet-action-panel");
        actionPanel.setWidthFull();
        actionPanel.getStyle().set("position", "sticky")
                .set("top", "0")
                .set("left", "0")
                .set("background", "white")
                .set("z-index", "10");
        return actionPanel;
    }

    private Button getAddRowOptBtn(List<SpreadsheetRow> rows) {
        Button addRowBtn = new Button("Add Row", event -> {
            addRow(rows);
            showPrimaryNotification("Row " + (rows.size()) + " added");
        });
        addRowBtn.addClassName("spreadsheet-action-button");
        return addRowBtn;
    }

    private Button getAddColumnOptBtn(List<SpreadsheetRow> rows) {
        Button addColumnBtn = new Button("Add Column", event -> {
            addColumn(rows);
            showPrimaryNotification("Column " + SpreadsheetUtils.getColumnHeader(rows.get(0).getCells().size() + 1) + " added");
        });
        addColumnBtn.addClassName("spreadsheet-action-button");
        return addColumnBtn;
    }

    private Button getSaveOptButton() {
        Button save = new Button("Save", event -> {
            save();
            showSuccessNotification("Spreadsheet saved!");
        });
        save.addClassName("spreadsheet-action-button");

        return save;
    }

    private Button getImportDataOptButton() {
        Button importData = new Button("Import", event -> {
            TextArea textArea = new TextArea();
            textArea.setLabel("Data:");

            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Data Import");
            confirmDialog.setText("Paste JSON Data you want to import");
            confirmDialog.add(textArea);

            confirmDialog.setConfirmText("Import");
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.addConfirmListener(e -> {
                try {
                    List<SpreadsheetRow> spreadsheetRows = getSpreadsheetRows(textArea.getValue());
                    spreadsheet.setBody(textArea.getValue());
                    this.rows = spreadsheetRows;
                } catch (JsonProcessingException ex) {
                    showErrorNotification("Import failed!");
                }
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(e -> {
            });
        });
        importData.addClassName("spreadsheet-action-button");
        return importData;
    }

    private Button getExportOptButton() {
        Button export = new Button("Export", event -> {
            String body = getOrCreate(spreadsheet.getName()).getBody();
            Dialog dialog = new Dialog();
            TextArea textArea = new TextArea(body);
            textArea.setLabel("Saved Data:");
            textArea.setSizeFull();
            textArea.setValue(body);
            dialog.add(textArea);
            dialog.setWidth("80vw");
            dialog.open();
        });
        export.addClassName("spreadsheet-action-button");

        return export;
    }

    private void addRow(List<SpreadsheetRow> rows) {
        SpreadsheetRow newRow = new SpreadsheetRow(rows.size() + 1);
        if (!rows.isEmpty()) {
            ArrayList<SpreadsheetCell> newEmptyCells = new ArrayList<>();
            for (SpreadsheetCell cell : rows.get(0).getCells()) {
                newEmptyCells.add(new SpreadsheetCell(newRow.rowNumber, cell.getColumnNumber(), ""));
            }
            newRow.setCells(newEmptyCells);
        }
        rows.add(newRow);
        refreshView(rows);
    }

    private void addColumn(List<SpreadsheetRow> rows) {
        for (SpreadsheetRow row : rows) {
            if (!row.getCells().isEmpty()) {
                row.addCell(new SpreadsheetCell(row.rowNumber, row.getCells().get(row.getCells().size() - 1).getColumnNumber() + 1, ""));
            } else {
                row.addCell(new SpreadsheetCell(row.rowNumber, 1, ""));
            }
        }
        refreshView(rows);
    }

    private void refreshView(List<SpreadsheetRow> rows) {
        removeAll();
        spreadsheetService.evaluateAllFormulas(rows);
        VerticalLayout spreadsheetLayout = createSpreadsheetLayout(rows);
        add(spreadsheetLayout);
    }

    private void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
            spreadsheet.setBody(jsonBody);
            spreadsheetService.save(spreadsheet);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SpreadsheetRow> getRows(String spreadsheetName) {
        Spreadsheet spreadsheet = getOrCreate(spreadsheetName);
        String body = spreadsheet.getBody();
        if (body != null) {
            try {
                return getSpreadsheetRows(body);
            } catch (JsonProcessingException e) {
                try {
                    /* deprecated */
                    return tryBackwardCompatibility(body);
                } catch (JsonProcessingException innerE) {
                    log.error("Failed to parse spreadsheet body!", e);
                    showErrorNotification("Failed to retrieve spreadsheet body! Check text area below.");
                    infoTextArea.setValue(body);
                    infoTextArea.setLabel("Body:");
                    infoTextArea.setVisible(true);
                }

            }
        }

        return new ArrayList<>();
    }

    private List<SpreadsheetRow> tryBackwardCompatibility(String body) throws JsonProcessingException {
        List<SpreadsheetRow> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(body);

        int i = 0;
        for (JsonNode rowArray : root) {
            i++;
            SpreadsheetRow row = new SpreadsheetRow(i);
            for (JsonNode cell : rowArray) {
                int colNum = cell.get("columnNumber").asInt() + 1;
                int rowNum = cell.get("rowNumber").asInt() + 1;
                if (rowNum != i) {
                    showErrorNotification("Data malformed. Backward Compatibility is invalid. Make sure data displays correctly!");
                }

                // If functionValue != null, it's function - use it instead of value
                String value = cell.hasNonNull("functionValue")
                        ? cell.get("functionValue").asText()
                        : cell.get("value").asText();

                row.getCells().add(new SpreadsheetCell(rowNum, colNum, value));
            }
            result.add(row);
        }

        spreadsheetService.moveRowsInFormulasToEnsureBackwardCompatibility(result);
        return result;
    }

    private Spreadsheet getOrCreate(String spreadsheetName) {
        Optional<Spreadsheet> spreadsheetOptional = this.spreadsheetService.loadByName(spreadsheetName);
        return spreadsheetOptional.orElseGet(() -> new Spreadsheet(spreadsheetName));
    }
}