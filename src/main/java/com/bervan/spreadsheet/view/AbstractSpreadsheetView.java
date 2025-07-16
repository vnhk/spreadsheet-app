package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageView;
import com.bervan.spreadsheet.functions.FormulaParser;
import com.bervan.spreadsheet.functions.SpreadsheetFunction;
import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
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
public abstract class AbstractSpreadsheetView extends AbstractPageView implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets/";
    private static final int MAX_RECURSION_DEPTH = 100;
    private final SpreadsheetService spreadsheetService;
    private final FormulaParser formulaParser;
    private final List<SpreadsheetFunction> spreadsheetFunctions;


    public AbstractSpreadsheetView(SpreadsheetService service, FormulaParser formulaParser, List<? extends SpreadsheetFunction> spreadsheetFunctions) {
        this.spreadsheetService = service;
        this.formulaParser = formulaParser;
        this.spreadsheetFunctions = (List<SpreadsheetFunction>) spreadsheetFunctions;
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String spreadsheetName = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        init(spreadsheetName);
    }

    private void init(String spreadsheetName) {
        // Load spreadsheet rows from service
        List<SpreadsheetRow> rows = getRows(spreadsheetName);


        // Create editable HTML table
        Div div = createHTMLTable(rows);

        add(div);
    }

    private Div createHTMLTable(List<SpreadsheetRow> rows) {
        Element table = new Element("table");
        table.getStyle().set("border-collapse", "collapse").set("width", "100%");

        // Add column headers (A, B, C...)
        Element headerRow = new Element("tr");

        // Top-left empty cell (for row numbers)
        Element emptyHeader = new Element("th");
        emptyHeader.getStyle().set("border", "1px solid #ccc").set("padding", "5px");
        headerRow.appendChild(emptyHeader);

        // Assume all rows have the same number of columns
        if (!rows.isEmpty()) {
            int columnCount = rows.get(0).cells.size();
            for (int i = 0; i < columnCount; i++) {
                Element th = new Element("th");
                th.setText(Character.toString((char) ('A' + i))); // A, B, C, ...
                th.getStyle().set("border", "1px solid #ccc")
                        .set("padding", "5px")
                        .set("text-align", "center");
                headerRow.appendChild(th);
            }
        }

        table.appendChild(headerRow);

        // Add row data with row numbers on the left
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            SpreadsheetRow row = rows.get(rowIndex);
            Element tr = new Element("tr");

            // Add row number (1, 2, 3...) as first column
            Element rowNumberCell = new Element("th");
            rowNumberCell.setText(String.valueOf(rowIndex + 1));
            rowNumberCell.getStyle().set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("text-align", "center");
            tr.appendChild(rowNumberCell);

            // Add editable input cells
            for (SpreadsheetCell cell : row.cells) {
                Element td = new Element("td");
                td.getStyle().set("border", "1px solid #ccc").set("padding", "5px");

                // Create input element for editable cell value
                Input input = new Input();
                input.getElement().setAttribute("value", String.valueOf(cell.getValue()));
                input.getElement().getStyle().set("width", "100%");
                input.getElement().setAttribute("data-cell-id", cell.getCellId());

                td.appendChild(input.getElement());
                tr.appendChild(td);
            }

            table.appendChild(tr);
        }

        Div div = new Div();
        div.getElement().appendChild(table);
        return div;
    }

    private List<SpreadsheetRow> getRows(String spreadsheetName) {
        List<SpreadsheetRow> rows = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
            SpreadsheetRow row = new SpreadsheetRow();
            row.rowNumber = rowIndex;

            for (int colIndex = 0; colIndex < 5; colIndex++) {
                Object value;

                // Sample formulas and values
                if (rowIndex == 0 && colIndex == 0) {
                    value = "=+(1, 2)";
                } else if (rowIndex == 1 && colIndex == 1) {
                    value = "=+(A0, 3)";
                } else if (rowIndex == 2 && colIndex == 2) {
                    value = 42;
                } else if (rowIndex == 3 && colIndex == 3) {
                    value = "=+(A0, B1)";
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