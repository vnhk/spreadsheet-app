package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@JsModule("./spreadsheet-context-menu.js")
@CssImport("./spreadsheet.css")
public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private final SpreadsheetService spreadsheetService;
    private List<SpreadsheetRow> rows;


    public AbstractSpreadsheetView(SpreadsheetService service) {
        this.spreadsheetService = service;
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private void init(String spreadsheetName) {
        // Load spreadsheet rows from service
        rows = getRows(spreadsheetName);
        spreadsheetService.evaluateAllFormulas(rows);
        // Create editable HTML table
        refreshView(rows);
    }

    private Div createHTMLTable(List<SpreadsheetRow> rows) {
        Element table = new Element("table");
        table.setAttribute("class", "spreadsheet-table");

        // Create the first row with column headers
        Element headerRow = new Element("tr");

        // Top-left corner cell (empty, for alignment with row numbers)
        Element emptyHeader = new Element("th");
        emptyHeader.getStyle().set("border", "1px solid white").set("padding", "5px");
        headerRow.appendChild(emptyHeader);

        if (!rows.isEmpty()) {
            addColumnHeaders(rows, headerRow); // Add column labels: A, B, C...
        }

        table.appendChild(headerRow);

        // Add each data row
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            SpreadsheetRow row = rows.get(rowIndex);
            Element tr = new Element("tr");

            addRowNumbersAsFirstColumn(rowIndex, tr); // Add row number (1, 2, 3...)

            addEditableInputCells(row, tr); // Add editable <input> elements for each cell

            table.appendChild(tr);
        }

        // Wrap the table in a Vaadin Div component
        Div div = new Div();
        div.getElement().appendChild(table);
        return div;
    }

    // Creates column headers A, B, C, etc.
    private void addColumnHeaders(List<SpreadsheetRow> rows, Element headerRow) {
        int columnCount = rows.get(0).cells.size();
        for (int i = 0; i < columnCount; i++) {
            Element th = new Element("th");
            th.setText(Character.toString((char) ('A' + i))); // Convert 0 → 'A', 1 → 'B', etc.
            th.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("color", "white")
                    .set("padding", "5px")
                    .set("text-align", "center");
            headerRow.appendChild(th);
        }
    }

    // Adds editable input cells to a given table row
    private void addEditableInputCells(SpreadsheetRow row, Element tr) {
        for (SpreadsheetCell cell : row.getCells()) {
            Element td = new Element("td");
            td.getStyle().set("border", "1px solid #ccc").set("padding", "5px");

            Input input = new Input();
            input.getElement().getStyle().set("width", "100%");

            if (cell.hasFormula()) {
                // Different text color for formula cells
                input.getElement().getStyle().set("color", "blue");
            }

            input.getElement().executeJs(
                    """
                            const input = this;
                            input.addEventListener('focus', function(e) {
                                if (input.dataset.formula) {
                                    input.value = input.dataset.formula; // Show formula on focus
                                }
                            });
                            
                            input.addEventListener('blur', function(e) {
                                const newValue = input.value;
                                if (newValue !== input.dataset.formula && newValue !== input.dataset.value) {
                                   $0.$server.onCellEdit(input.dataset.cellId, newValue);
                                } else {
                                    input.value = input.dataset.value; // Revert to value if nothing changed
                                }
                            });
                            """, getElement());

            input.getElement().setAttribute("value", String.valueOf(cell.getValue()));
            input.getElement().setAttribute("data-formula", cell.getFormula() != null ? cell.getFormula() : "");
            input.getElement().setAttribute("data-value", String.valueOf(cell.getValue()));
            input.getElement().setAttribute("data-cell-id", cell.getCellId());

            td.appendChild(input.getElement());
            tr.appendChild(td);
        }
    }

    @ClientCallable
    public void onCellEdit(String cellId, String value) {
        SpreadsheetCell cell = SpreadsheetService.findCellById(rows, cellId);
        if (cell != null) {
            cell.setValue(value);
            spreadsheetService.evaluateAllFormulas(rows);
            refreshView(rows);
        }
    }

    private void refreshView(List<SpreadsheetRow> rows) {
        removeAll();
        Div div = createHTMLTable(rows);
        add(div);
    }

    // Adds the row number (1-based index) to the beginning of the row
    private void addRowNumbersAsFirstColumn(int rowIndex, Element tr) {
        Element rowNumberCell = new Element("th");
        rowNumberCell.setText(String.valueOf(rowIndex + 1));
        rowNumberCell.getStyle()
                .set("border", "1px solid #ccc")
                .set("color", "white")
                .set("padding", "5px")
                .set("text-align", "center");
        tr.appendChild(rowNumberCell);
    }

    private List<SpreadsheetRow> getRows(String spreadsheetName) {
        List<SpreadsheetRow> rows = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 5; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 1; colIndex <= 5; colIndex++) {
                Object value;

                // Sample formulas and values
                if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(1, 2)";
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = "=+(A1, 3)";
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = 42;
                } else if (rowIndex == 4 && colIndex == 4) {
                    value = "=+(A1, B2)";
                } else {
                    value = "Test " + rowIndex + colIndex;
                }

                SpreadsheetCell cell = new SpreadsheetCell(rowIndex, colIndex, value);
                row.cells.add(cell);
            }

            rows.add(row);
        }

        return rows;
    }
}