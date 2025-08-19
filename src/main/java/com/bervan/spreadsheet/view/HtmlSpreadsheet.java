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
    private Element cellInfoDisplay;

    public HtmlSpreadsheet(List<SpreadsheetRow> rows, Element parentElement, Map<Integer, Integer> columnWidths) {
        this.parentElement = parentElement;
        this.columnWidths = columnWidths;

        createCellInfoDisplay();

        Element table = new Element("table");
        table.setAttribute("class", "spreadsheet-table");

        table.getStyle()
                .set("border-collapse", "separate")
                .set("border-spacing", "0")
                .set("width", "100%")
                .set("table-layout", "fixed");

        Element headerRow = new Element("tr");
        Element emptyHeader = new Element("th");

        // EMPTY TOP-LEFT CELL 
        emptyHeader.getStyle()
                .set("border", "1px solid gray")
                .set("padding", "5px")
                .set("width", "50px")
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
                .set("overflow", "auto")
                .set("position", "relative"); // Add position relative for absolute positioning of info display

        this.getElement().appendChild(cellInfoDisplay);
        this.getElement().appendChild(table);
        attachResizeColumnsAndRowsScript();
        attachCellClickScript();
    }

    private void createCellInfoDisplay() {
        cellInfoDisplay = new Element("div");
        cellInfoDisplay.setAttribute("id", "cell-info-display");
        cellInfoDisplay.getStyle()
                .set("position", "fixed")
                .set("top", "80px")
                .set("left", "20px")
                .set("background", "rgba(0, 0, 0, 0.9)")
                .set("color", "white")
                .set("padding", "10px 15px")
                .set("border-radius", "6px")
                .set("font-family", "monospace")
                .set("font-size", "14px")
                .set("z-index", "9999")
                .set("display", "none")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.4)")
                .set("border", "1px solid #444")
                .set("min-width", "200px");
        
        Element closeButton = new Element("span");
        closeButton.setText("×");
        closeButton.setAttribute("id", "cell-info-close");
        closeButton.getStyle()
                .set("position", "absolute")
                .set("top", "2px")
                .set("right", "8px")
                .set("cursor", "pointer")
                .set("font-size", "18px")
                .set("color", "#ccc")
                .set("line-height", "1")
                .set("font-weight", "bold");
        
        Element contentDiv = new Element("div");
        contentDiv.setAttribute("id", "cell-info-content");
        contentDiv.setText("No cell selected");
        
        cellInfoDisplay.appendChild(closeButton);
        cellInfoDisplay.appendChild(contentDiv);
    }

    private void attachCellClickScript() {
        getElement().executeJs("""
                const table = this.querySelector('table');
                const cellInfoDisplay = this.querySelector('#cell-info-display');
                const cellInfoContent = this.querySelector('#cell-info-content');
                const closeButton = this.querySelector('#cell-info-close');
                
                // Close button click handler
                closeButton.addEventListener('click', function(e) {
                    cellInfoDisplay.style.display = 'none';
                    // Remove all cell highlighting
                    table.querySelectorAll('.spreadsheet-cell').forEach(c => {
                        c.style.outline = '';
                    });
                    e.stopPropagation();
                });
                
                // Add click listeners to all spreadsheet cells
                table.addEventListener('click', function(e) {
                    const cell = e.target.closest('.spreadsheet-cell');
                    if (cell) {
                        const cellId = cell.dataset.cellId;
                        const columnNumber = cell.dataset.columnNumber;
                        const rowNumber = cell.dataset.rowNumber;
                        
                        // Update cell info display
                        cellInfoContent.innerHTML = '<strong>Cell:</strong> ' + cellId + '<br><strong>Column:</strong> ' + columnNumber + '<br><strong>Row:</strong> ' + rowNumber;
                        cellInfoDisplay.style.display = 'block';
                        
                        // Remove previous selection highlighting
                        table.querySelectorAll('.spreadsheet-cell').forEach(c => {
                            c.style.outline = '';
                        });
                        
                        // Highlight selected cell
                        cell.style.outline = '2px solid #007bff';
                        
                        e.stopPropagation();
                    }
                });
                
                // ESC key to close info display
                document.addEventListener('keydown', function(e) {
                    if (e.key === 'Escape' && cellInfoDisplay.style.display === 'block') {
                        cellInfoDisplay.style.display = 'none';
                        // Remove all cell highlighting
                        table.querySelectorAll('.spreadsheet-cell').forEach(c => {
                            c.style.outline = '';
                        });
                    }
                });
                """);
    }

    private void addColumnHeaders(List<SpreadsheetRow> rows, Element headerRow) {
        int columnCount = rows.get(0).getCells().size();
        for (int i = 0; i < columnCount; i++) {
            Element th = new Element("th");
            th.getClassList().add("spreadsheet-header");
            th.setAttribute("data-column-number", String.valueOf(i + 1));
            th.setText(Character.toString((char) ('A' + i)));

            // COLUMN HEADERS (A, B, C...)
            th.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("text-align", "center")
                    .set("color", "white")
                    .set("background", "#333")
                    .set("position", "relative")
                    .set("min-width", "80px");

            //set initial column widths if exists
            if (columnWidths != null && columnWidths.containsKey(i + 1)) {
                int width = columnWidths.get(i + 1);
                th.getStyle().set("width", width + "px");
            } else {
                th.getStyle().set("width", "120px"); // default width
            }

            headerRow.appendChild(th);
        }
    }

    private void addRowNumbersAsFirstColumn(int rowIndex, Element tr) {
        Element rowNumberCell = new Element("th");
        rowNumberCell.getClassList().add("spreadsheet-row-header");
        rowNumberCell.setAttribute("data-row-number", String.valueOf(rowIndex + 1));

        rowNumberCell.setText(String.valueOf(rowIndex + 1));

        // ROW NUMBERS
        rowNumberCell.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "5px")
                .set("text-align", "center")
                .set("color", "white")
                .set("background", "#333")
                .set("width", "50px");
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
                    .set("min-width", "80px");

            // Set initial column width for cells if exists
            if (columnWidths != null && columnWidths.containsKey(cell.getColumnNumber())) {
                int width = columnWidths.get(cell.getColumnNumber());
                td.getStyle().set("width", width + "px");
            } else {
                td.getStyle().set("width", "120px"); // default width to match headers
            }

            // contenteditable div inside td
            Div editableDiv = new Div();
            editableDiv.setText(String.valueOf(cell.getValue()));
            editableDiv.getElement().setAttribute("contenteditable", "true");
            editableDiv.getElement().setAttribute("data-formula", cell.getFormula() != null ? cell.getFormula() : "");
            editableDiv.getElement().setAttribute("data-value", String.valueOf(cell.getValue()));
            editableDiv.getElement().setAttribute("data-cell-id", cell.getCellId());
            editableDiv.getStyle()
                    .set("width", "100%")
                    .set("padding", "4px")
                    .set("box-sizing", "border-box");

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
                
                    table.querySelectorAll('th.spreadsheet-header').forEach((th, index) => {
                        const columnNumber = th.dataset.columnNumber;
                
                        // Create resizer handle
                        const resizer = document.createElement('div');
                        resizer.style.width = '5px';
                        resizer.style.height = '100%';
                        resizer.style.position = 'absolute';
                        resizer.style.top = '0';
                        resizer.style.right = '-2px';
                        resizer.style.cursor = 'col-resize';
                        resizer.style.userSelect = 'none';
                        resizer.style.backgroundColor = '#666';
                        resizer.style.zIndex = '10';
                
                        th.style.position = 'relative';
                        th.appendChild(resizer);
                
                        let startX, startWidth, tableWidth;
                        let isResizing = false;
                
                        resizer.addEventListener('mousedown', (e) => {
                            console.log('Resize started for column', columnNumber);
                            e.preventDefault();
                            e.stopPropagation();
                
                            isResizing = true;
                            startX = e.clientX;
                            startWidth = th.offsetWidth;
                            tableWidth = table.offsetWidth;
                
                            document.addEventListener('mousemove', onMouseMove);
                            document.addEventListener('mouseup', onMouseUp);
                        });
                
                        const onMouseMove = (e) => {
                            if (!isResizing) return;
                
                            const deltaX = e.clientX - startX;
                            const newWidth = Math.max(50, startWidth + deltaX);
                
                            th.style.width = newWidth + 'px';
                
                            const columnCells = table.querySelectorAll(`td[data-column-number="${columnNumber}"]`);
                            columnCells.forEach(cell => {
                                cell.style.width = newWidth + 'px';
                            });
                        };
                
                        const onMouseUp = (e) => {
                            if (!isResizing) return;
                
                            isResizing = false;
                            const finalWidth = th.offsetWidth;
                            console.log('Resize ended for column', columnNumber, 'final width:', finalWidth);
                
                            $0.$server.onColumnResize(columnNumber, finalWidth);
                
                            document.removeEventListener('mousemove', onMouseMove);
                            document.removeEventListener('mouseup', onMouseUp);
                        };
                    });
                """, parentElement);
    }
}