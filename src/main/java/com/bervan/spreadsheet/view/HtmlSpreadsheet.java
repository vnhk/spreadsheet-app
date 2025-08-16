package com.bervan.spreadsheet.view;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.dom.Element;

import java.util.List;

public class HtmlSpreadsheet extends Div {
    public HtmlSpreadsheet(List<SpreadsheetRow> rows) {
        Element table = new Element("table");
        table.setAttribute("class", "spreadsheet-table");

        table.getStyle()
                .set("border-collapse", "collapse")
                .set("width", "max-content");

        // Create header row
        Element headerRow = new Element("tr");
        Element emptyHeader = new Element("th");
        emptyHeader.getStyle().set("border", "1px solid gray").set("padding", "5px").set("min-width", "50px");
        headerRow.appendChild(emptyHeader);

        if (!rows.isEmpty()) {
            addColumnHeaders(rows, headerRow);
        }
        table.appendChild(headerRow);

        // Add data rows
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            SpreadsheetRow row = rows.get(rowIndex);
            Element tr = new Element("tr");
            addRowNumbersAsFirstColumn(rowIndex, tr);
            addEditableInputCells(row, tr);
            table.appendChild(tr);
        }

        // Wrap table in scrollable div
        this.getStyle()
                .set("overflow", "auto")   // enables both horizontal and vertical scrolling
                .set("border", "1px solid lightgray");

        this.getElement().appendChild(table);
    }

    // Creates column headers A, B, C, etc.
    private void addColumnHeaders(List<SpreadsheetRow> rows, Element headerRow) {
        int columnCount = rows.get(0).cells.size();
        for (int i = 0; i < columnCount; i++) {
            Element th = new Element("th");
            th.getClassList().add("spreadsheet-header");
            th.setAttribute("data-column-number", String.valueOf(i + 1)); //starts with 1
            th.setText(Character.toString((char) ('A' + i))); // Convert 0 → 'A', 1 → 'B', etc.
            th.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("color", "white")
                    .set("padding", "5px")
                    .set("text-align", "center");
            headerRow.appendChild(th);
        }
    }

    // Adds the row number (1-based index) to the beginning of the row
    private void addRowNumbersAsFirstColumn(int rowIndex, Element tr) {
        Element rowNumberCell = new Element("th");
        rowNumberCell.getClassList().add("spreadsheet-row-header");
        rowNumberCell.setAttribute("data-row-number", String.valueOf(rowIndex + 1)); //starts with 1

        rowNumberCell.setText(String.valueOf(rowIndex + 1));
        rowNumberCell.getStyle()
                .set("border", "1px solid #ccc")
                .set("color", "white")
                .set("padding", "5px")
                .set("text-align", "center");
        tr.appendChild(rowNumberCell);
    }

    // Adds editable input cells to a given table row
    private void addEditableInputCells(SpreadsheetRow row, Element tr) {
        for (SpreadsheetCell cell : row.getCells()) {
            Element td = new Element("td");
            td.getClassList().add("spreadsheet-cell");
            td.getStyle().set("border", "1px solid #ccc").set("padding", "5px");

            Input input = new Input();
            input.getElement().getStyle();
            input.getStyle()
                    .set("min-width", "50px")
                    .set("padding", "4px");

            if (cell.hasFormula()) {
                // Different text color for formula cells
                input.getElement().getStyle().set("color", "#82CAFF");
                input.getElement().getStyle().set("background", "#1B263B");
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
            input.getElement().setAttribute("data-column-number", String.valueOf(cell.getColumnNumber()));

            td.appendChild(input.getElement());
            tr.appendChild(td);
        }
    }

}
