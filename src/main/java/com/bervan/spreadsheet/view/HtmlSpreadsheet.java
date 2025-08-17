package com.bervan.spreadsheet.view;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

import java.util.List;
import java.util.Map;

public class HtmlSpreadsheet extends Div {
    private final Element parentElement;
    private final Map<Integer, Integer> columnWidths;

    public HtmlSpreadsheet(List<SpreadsheetRow> rows, Element parentElement, Map<Integer, Integer> columnWidths) {
        this.parentElement = parentElement;
        this.columnWidths = columnWidths;

        Element table = new Element("table");
        table.setAttribute("class", "spreadsheet-table");

        table.getStyle()
                .set("border-collapse", "separate")
                .set("border-spacing", "0")
                .set("width", "95vw")
                .set("display", "block")
                .set("overflow", "auto")
                .set("max-height", "78vh");

        Element headerRow = new Element("tr");
        Element emptyHeader = new Element("th");

        // EMPTY TOP-LEFT CELL — must stick both top and left
        emptyHeader.getStyle()
                .set("border", "1px solid gray")
                .set("padding", "5px")
                .set("min-width", "50px")
                .set("position", "sticky") // stick
                .set("top", "0")
                .set("left", "0")
                .set("z-index", "5")       // above row/col headers
                .set("background", "#333")
                .set("color", "white");
        headerRow.appendChild(emptyHeader);

        if (!rows.isEmpty()) {
            addColumnHeaders(rows, headerRow);
        }
        table.appendChild(headerRow);

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            SpreadsheetRow row = rows.get(rowIndex);
            Element tr = new Element("tr");
            addRowNumbersAsFirstColumn(rowIndex, tr);
            addEditableCells(row, tr);
            table.appendChild(tr);
        }

        this.getStyle()
                .set("width", "100%")
                .set("height", "80vh")
                .set("overflow", "auto");

        this.getElement().appendChild(table);
        attachResizeColumnsAndRowsScript();
    }

    private void addColumnHeaders(List<SpreadsheetRow> rows, Element headerRow) {
        int columnCount = rows.get(0).getCells().size();
        for (int i = 0; i < columnCount; i++) {
            Element th = new Element("th");
            th.getClassList().add("spreadsheet-header");
            th.setAttribute("data-column-number", String.valueOf(i + 1));
            th.setText(Character.toString((char) ('A' + i)));

            // COLUMN HEADERS (A, B, C...) — stick to top
            th.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("text-align", "center")
                    .set("color", "white")
                    .set("background", "#333")
                    .set("position", "sticky") // <-- keep sticky
                    .set("top", "0")
                    .set("z-index", "3");      // above cells, below top-left

            //set initial column widths if exists
            if (columnWidths != null && columnWidths.containsKey(i + 1)) {
                int width = columnWidths.get(i + 1);
                th.getStyle().set("width", width + "px");
                columnWidths.put(i + 1, width);
            }

            headerRow.appendChild(th);
        }
    }

    private void addRowNumbersAsFirstColumn(int rowIndex, Element tr) {
        Element rowNumberCell = new Element("th");
        rowNumberCell.getClassList().add("spreadsheet-row-header");
        rowNumberCell.setAttribute("data-row-number", String.valueOf(rowIndex + 1));

        rowNumberCell.setText(String.valueOf(rowIndex + 1));

        // ROW NUMBERS — stick to left
        rowNumberCell.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "5px")
                .set("text-align", "center")
                .set("color", "white")
                .set("background", "#333")
                .set("position", "sticky") // <-- keep sticky
                .set("left", "0")
                .set("z-index", "2");      // below top-left, below column headers
        tr.appendChild(rowNumberCell);
    }

    // Adds contenteditable cells
    private void addEditableCells(SpreadsheetRow row, Element tr) {
        for (SpreadsheetCell cell : row.getCells()) {
            Element td = new Element("td");
            td.getClassList().add("spreadsheet-cell");
            td.setAttribute("data-cell-id", cell.getCellId());
            td.setAttribute("data-column-number", String.valueOf(cell.getColumnNumber()));
            td.setAttribute("data-row-number", String.valueOf(cell.getRowNumber()));
            td.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("min-width", "50px");

            // contenteditable div inside td
            Div editableDiv = new Div();
            editableDiv.setText(String.valueOf(cell.getValue()));
            editableDiv.getElement().setAttribute("contenteditable", "true");
            editableDiv.getElement().setAttribute("data-formula", cell.getFormula() != null ? cell.getFormula() : "");
            editableDiv.getElement().setAttribute("data-value", String.valueOf(cell.getValue()));
            editableDiv.getElement().setAttribute("data-cell-id", cell.getCellId());
            editableDiv.getStyle()
                    .set("min-width", "50px")
                    .set("padding", "4px");

            if (cell.hasFormula()) {
                editableDiv.getStyle()
                        .set("color", "#82CAFF")
                        .set("background", "#1B263B");
            }

            // JS listeners for focus/blur to show formula / commit edit
            editableDiv.getElement().executeJs(
                    """
                            const div = this;
                            div.addEventListener('focus', function() {
                                if (div.dataset.formula) {
                                    div.textContent = div.dataset.formula;
                                }
                            });
                            div.addEventListener('blur', function() {
                                const newValue = div.textContent;
                                if (newValue !== div.dataset.value && newValue !== div.dataset.formula) {
                                    $0.$server.onCellEdit(div.dataset.cellId, newValue);
                                } else {
                                    div.textContent = div.dataset.value;
                                }
                            });
                            """, parentElement);

            td.appendChild(editableDiv.getElement());
            tr.appendChild(td);
        }
    }

    private void attachResizeColumnsAndRowsScript() {
        getElement().executeJs("""
                    const table = this.querySelector('table');
                
                    table.querySelectorAll('th.spreadsheet-header').forEach(th => {
                        const columnNumber = th.dataset.columnNumber;
                        const resizer = document.createElement('div');
                        resizer.style.width = '5px';
                        resizer.style.height = '100%';
                        resizer.style.position = 'absolute';
                        resizer.style.top = '0';
                        resizer.style.right = '0';
                        resizer.style.cursor = 'col-resize';
                        resizer.style.userSelect = 'none';
                        // IMPORTANT: do NOT override th.style.position (must remain 'sticky')
                        th.appendChild(resizer);
                
                        let startX, startWidth;
                        let isResizing = false;
                
                        const onMouseMove = e => {
                            if (!isResizing) return;
                            const newWidth = startWidth + e.clientX - startX;
                            th.style.width = newWidth + 'px';
                        };
                
                        const onMouseUp = e => {
                            if (!isResizing) return;
                            isResizing = false;
                            const finalWidth = th.offsetWidth;
                            $0.$server.onColumnResize(columnNumber, finalWidth);
                            document.removeEventListener('mousemove', onMouseMove);
                            document.removeEventListener('mouseup', onMouseUp);
                        };
                
                        resizer.addEventListener('mousedown', e => {
                            e.preventDefault();
                            startX = e.clientX;
                            startWidth = th.offsetWidth;
                            isResizing = true;
                            document.addEventListener('mousemove', onMouseMove);
                            document.addEventListener('mouseup', onMouseUp);
                        });
                    });
                """, parentElement);
    }
}