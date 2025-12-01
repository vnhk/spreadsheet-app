package com.bervan.spreadsheet.view;

import com.bervan.common.view.AbstractPageView;
import com.bervan.logging.JsonLogger;
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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.*;
import java.util.stream.Collectors;

@CssImport("./spreadsheet.css")
@JsModule("./spreadsheet-context-menu.js")
public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {

    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private final JsonLogger log = JsonLogger.getLogger(getClass());
    private final SpreadsheetService spreadsheetService;
    private Spreadsheet spreadsheet;
    private TextArea infoTextArea = new TextArea("");

    public AbstractSpreadsheetView(SpreadsheetService service) {
        this.spreadsheetService = service;
        infoTextArea.getStyle().setColor("white");
        infoTextArea.setSizeFull();
        infoTextArea.setVisible(false);
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private List<SpreadsheetRow> getSpreadsheetRows(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        List<SpreadsheetRow> spreadsheetRows = mapper.readValue(body, new TypeReference<List<SpreadsheetRow>>() {
        });

        if (spreadsheetRows == null) {
            return new ArrayList<>();
        }

        return spreadsheetRows;
    }

    private Map<Integer, Integer> getColumnWidths(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Map<Integer, Integer> integerIntegerMap = mapper.readValue(body, new TypeReference<Map<Integer, Integer>>() {
        });

        if (integerIntegerMap == null) {
            return new HashMap<>();
        }

        return integerIntegerMap;
    }

    @ClientCallable
    public void onCellEdit(String cellId, String value) {
        SpreadsheetCell cell = SpreadsheetService.findCellById(spreadsheet.getRows(), cellId);
        if (cell != null) {
            cell.setNewValueAndCellRelatedFields(value);
            refreshView(spreadsheet.getRows());
        }
    }

    @ClientCallable
    public void addColumnLeft(Integer columnNumber) {
        spreadsheetService.addColumnLeft(spreadsheet.getRows(), null, columnNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " added!");
    }

    @ClientCallable
    public void addColumnRight(Integer columnNumber) {
        spreadsheetService.addColumnRight(spreadsheet.getRows(), null, columnNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber + 1) + " added!");
    }

    @ClientCallable
    public void duplicateColumn(Integer columnNumber) {
        spreadsheetService.duplicateColumn(spreadsheet.getRows(), columnNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " duplicated!");
    }

    @ClientCallable
    public void deleteColumn(Integer columnNumber) {
        StringBuilder confirmationMessageDetails = new StringBuilder();
        for (SpreadsheetRow row : spreadsheet.getRows()) {
            for (SpreadsheetCell cell : row.getCells()) {
                //collect all formulas except formulas in column with columnNumber
                if (cell.hasFormula() && cell.getColumnNumber() != columnNumber) {
                    List<FunctionArgument> functionArguments = spreadsheetService.getFunctionArguments(spreadsheet.getRows(), cell.getFormula());
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
        spreadsheetService.deleteColumn(spreadsheet.getRows(), columnNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification("Column " + SpreadsheetUtils.getColumnHeader(columnNumber) + " deleted!");
    }

    @ClientCallable
    public void addRowAbove(Integer rowNumber) {
        spreadsheetService.addRowAbove(spreadsheet.getRows(), null, rowNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Row " + rowNumber + " added!");
    }

    @ClientCallable
    public void addRowBelow(Integer rowNumber) {
        spreadsheetService.addRowBelow(spreadsheet.getRows(), null, rowNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Row " + (rowNumber + 1) + " added!");
    }

    @ClientCallable
    public void duplicateRow(Integer rowNumber) {
        spreadsheetService.duplicateRow(spreadsheet.getRows(), rowNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification(" Row " + (rowNumber + 1) + " duplicated!");
    }

    @ClientCallable
    public void deleteRow(Integer rowNumber) {
        StringBuilder confirmationMessageDetails = new StringBuilder();
        for (SpreadsheetRow row : spreadsheet.getRows()) {
            for (SpreadsheetCell cell : row.getCells()) {
                //collect all formulas except formulas in row with rowNumber
                if (cell.hasFormula() && cell.getColumnNumber() != rowNumber) {
                    List<FunctionArgument> functionArguments = spreadsheetService.getFunctionArguments(spreadsheet.getRows(), cell.getFormula());
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
        spreadsheetService.deleteRow(spreadsheet.getRows(), rowNumber);
        refreshView(spreadsheet.getRows());
        showPrimaryNotification("Row " + rowNumber + " deleted!");
    }

    private void openFindReplaceDialog(List<SpreadsheetRow> rows) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Find & Replace");
        dialog.setWidth("60vw");

        VerticalLayout dialogLayout = new VerticalLayout();

        TextField findField = new TextField("Find");
        findField.setWidthFull();
        findField.setPlaceholder("Enter text to find");

        TextField replaceField = new TextField("Replace with");
        replaceField.setWidthFull();

        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button findNextBtn = new Button("Find Next", VaadinIcon.ARROW_RIGHT.create());
        findNextBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        findNextBtn.addClickListener(e -> {
            String findText = findField.getValue();
            if (!findText.isEmpty()) {
                // Implement find logic
                showPrimaryNotification("Searching for: " + findText);
            }
        });

        Button replaceBtn = new Button("Replace", VaadinIcon.REFRESH.create());
        replaceBtn.addClickListener(e -> {
            String findText = findField.getValue();
            String replaceText = replaceField.getValue();
            if (!findText.isEmpty()) {
                // Implement replace logic
                showPrimaryNotification("Replaced: " + findText + " with: " + replaceText);
            }
        });

        Button replaceAllBtn = new Button("Replace All", VaadinIcon.REFRESH.create());
        replaceAllBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        replaceAllBtn.addClickListener(e -> {
            String findText = findField.getValue();
            String replaceText = replaceField.getValue();
            if (!findText.isEmpty()) {
                for (SpreadsheetRow row : rows) {
                    row.getCells().stream().filter(c -> c.getValue().equals(findText))
                            .forEach(c -> c.setNewValueAndCellRelatedFields(replaceText));
                }
                refreshView(rows);
                showPrimaryNotification("Replaced all occurrences of: " + findText);
            }
        });

        Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> dialog.close());

        buttonLayout.add(findNextBtn, replaceBtn, replaceAllBtn, cancelBtn);

        dialogLayout.add(findField, replaceField, buttonLayout);
        dialog.add(dialogLayout);

        dialog.open();
    }

    @ClientCallable
    public void onColumnResize(String columnNumber, int width) {
        int col = Integer.parseInt(columnNumber);
        spreadsheet.getColumnWidths().put(col, width);
    }

    private void init(String spreadsheetName) {
        // Load spreadsheet rows from service
        spreadsheet = getOrCreate(spreadsheetName);
        spreadsheet.setRows(getRows());
        spreadsheet.setColumnWidths(getColumnWidths());
        spreadsheetService.evaluateAllFormulas(spreadsheet.getRows());
        refreshView(spreadsheet.getRows());
        add(infoTextArea);
    }

    private HtmlSpreadsheet createHTMLTable(List<SpreadsheetRow> rows) {
        return new HtmlSpreadsheet(rows, getElement(), spreadsheet.getColumnWidths());
    }

    private VerticalLayout createSpreadsheetLayout(List<SpreadsheetRow> rows) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        HorizontalLayout actionPanel = getToolbarPanel(rows);
        layout.add(actionPanel);

        Div tableDiv = createHTMLTable(rows);
        tableDiv.getElement().executeJs("initContextMenu($0)", getElement());

        layout.add(actionPanel, tableDiv);
        layout.setFlexGrow(1, tableDiv);
        layout.setSizeFull();
        return layout;
    }

    private HorizontalLayout getToolbarPanel(List<SpreadsheetRow> rows) {
        HorizontalLayout actionPanel = new HorizontalLayout();
        actionPanel.addClassName("spreadsheet-action-panel");
        actionPanel.setWidthFull();
        actionPanel.getStyle()
                .set("position", "sticky")
                .set("top", "0")
                .set("left", "0")
                .set("z-index", "10")
                .set("border-bottom", "1px solid #dee2e6")
                .set("padding", "8px 16px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        HorizontalLayout editSection = createRibbonSection("Edit", getAddRowOptBtn(rows), getAddColumnOptBtn(rows));
        HorizontalLayout dataSection = createRibbonSection("Data", getFindReplaceButton(rows));
        HorizontalLayout fileSection = createRibbonSection("File", getSaveOptButton(), getImportDataOptButton(), getExportOptButton());

        actionPanel.add(editSection, dataSection, fileSection);
        actionPanel.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actionPanel.setAlignItems(FlexComponent.Alignment.CENTER);

        return actionPanel;
    }

    private HorizontalLayout createRibbonSection(String title, Button... buttons) {
        HorizontalLayout section = new HorizontalLayout();
        section.addClassName("spreadsheet-ribbon-section");

        VerticalLayout sectionContent = new VerticalLayout();
        sectionContent.setSpacing(false);
        sectionContent.setPadding(false);

        // Section title
        Span sectionTitle = new Span(title);
        sectionTitle.addClassName("spreadsheet-section-title");

        HorizontalLayout buttonsRow = new HorizontalLayout();
        buttonsRow.setSpacing(true);
        buttonsRow.setPadding(false);
        if (buttons != null && buttons.length > 0) {
            buttonsRow.add(buttons);
        }

        sectionContent.add(buttonsRow, sectionTitle);
        section.add(sectionContent);

        return section;
    }

    private Button getFindReplaceButton(List<SpreadsheetRow> rows) {
        Button findReplaceBtn = new Button("Find & Replace");
        findReplaceBtn.addClickListener(event -> {
            openFindReplaceDialog(rows);
        });

        styleSpreadsheetButton(findReplaceBtn, VaadinIcon.SEARCH.create(), "Find and replace text in spreadsheet");
        return findReplaceBtn;
    }

    private Button getAddRowOptBtn(List<SpreadsheetRow> rows) {
        Button addRowBtn = new Button("Add Row");
        addRowBtn.addClickListener(event -> {
            addRow(rows);
            showPrimaryNotification("Row " + (rows.size()) + " added");
        });

        styleSpreadsheetButton(addRowBtn, VaadinIcon.PLUS.create(), "Add a new row to the spreadsheet");
        return addRowBtn;
    }

    private Button getAddColumnOptBtn(List<SpreadsheetRow> rows) {
        Button addColumnBtn = new Button("Add Column");
        addColumnBtn.addClickListener(event -> {
            addColumn(rows);
            showPrimaryNotification("Column " + SpreadsheetUtils.getColumnHeader(rows.get(0).getCells().size() + 1) + " added");
        });

        styleSpreadsheetButton(addColumnBtn, VaadinIcon.PLUS_CIRCLE.create(), "Add a new column to the spreadsheet");
        return addColumnBtn;
    }

    private Button getSaveOptButton() {
        Button saveBtn = new Button("Save");
        saveBtn.addClickListener(event -> {
            save();
            showPrimaryNotification("Spreadsheet saved successfully");
        });

        saveBtn.getElement().setAttribute("data-action", "save");
        styleSpreadsheetButton(saveBtn, VaadinIcon.CHECK_CIRCLE.create(), "Save the spreadsheet");
        return saveBtn;
    }

    private Button getImportDataOptButton() {
        Button importBtn = new Button("Import");
        importBtn.addClickListener(event -> {
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
                    spreadsheet.setRows(spreadsheetRows);
                } catch (JsonProcessingException ex) {
                    log.error("Failed to parse spreadsheet body!", ex);
                    showErrorNotification("Import failed!");
                }
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(e -> {
            });
            confirmDialog.open();
        });

        importBtn.getElement().setAttribute("data-action", "import");
        styleSpreadsheetButton(importBtn, VaadinIcon.UPLOAD.create(), "Import data from file");
        return importBtn;
    }

    private Button getExportOptButton() {
        Button exportBtn = new Button("Export");
        exportBtn.addClickListener(event -> {
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

        exportBtn.getElement().setAttribute("data-action", "export");
        styleSpreadsheetButton(exportBtn, VaadinIcon.DOWNLOAD.create(), "Export spreadsheet to file");
        return exportBtn;
    }

    private void styleSpreadsheetButton(Button button, Icon icon, String tooltip) {
        button.setIcon(icon);
        button.addClassName("spreadsheet-button");

        button.getStyle()
                .set("border-radius", "4px")
                .set("font-size", "12px")
                .set("font-weight", "500")
                .set("padding", "8px 12px")
                .set("min-width", "80px")
                .set("cursor", "pointer");

        if (tooltip != null) {
            button.getElement().setAttribute("title", tooltip);
        }
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
        add(new SpreadsheetPageLayout(true, spreadsheet.getName(), AbstractSpreadsheetView.ROUTE_NAME));
        spreadsheetService.evaluateAllFormulas(rows);
        VerticalLayout spreadsheetLayout = createSpreadsheetLayout(rows);
        add(spreadsheetLayout);
    }

    private void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spreadsheet.getRows());
            spreadsheet.setBody(jsonBody);

            String columnWidthBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spreadsheet.getColumnWidths());
            spreadsheet.setColumnsWidthsBody(columnWidthBody);

            spreadsheetService.save(spreadsheet);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SpreadsheetRow> getRows() {
        String body = spreadsheet.getBody();
        if (body != null) {
            try {
                return getSpreadsheetRows(body);
            } catch (JsonProcessingException e) {
                try {
                    /* deprecated */
                    return tryRowsBackwardCompatibility(body);
                } catch (Exception innerE) {
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

    private Map<Integer, Integer> getColumnWidths() {
        String columnConfigsBody = spreadsheet.getColumnsWidthsBody();
        if (columnConfigsBody != null) {
            try {
                return getColumnWidths(columnConfigsBody);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse spreadsheet column widths config!", e);
                showErrorNotification("Failed to retrieve spreadsheet column widths config! Check text area below.");
                infoTextArea.setValue(columnConfigsBody);
                infoTextArea.setLabel("Body:");
                infoTextArea.setVisible(true);
            }
        }

        return new HashMap<>();
    }

    private List<SpreadsheetRow> tryRowsBackwardCompatibility(String body) throws JsonProcessingException {
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