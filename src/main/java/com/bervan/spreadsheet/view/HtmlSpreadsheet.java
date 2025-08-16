package com.bervan.spreadsheet.view;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.vaadin.flow.component.html.Div;
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
        emptyHeader.getStyle()
                .set("border", "1px solid gray")
                .set("padding", "5px")
                .set("min-width", "50px");
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
            addEditableCells(row, tr);
            table.appendChild(tr);
        }

        // Wrap table in scrollable div
        this.getStyle()
                .set("overflow", "auto")   // enables scrolling
                .set("border", "1px solid lightgray");

        this.getElement().appendChild(table);
        attachResizeColumnsAndRowsScript();
    }

    // Column headers A, B, C etc.
    private void addColumnHeaders(List<SpreadsheetRow> rows, Element headerRow) {
        int columnCount = rows.get(0).getCells().size();
        for (int i = 0; i < columnCount; i++) {
            Element th = new Element("th");
            th.getClassList().add("spreadsheet-header");
            th.setAttribute("data-column-number", String.valueOf(i + 1));
            th.setText(Character.toString((char) ('A' + i)));
            th.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("text-align", "center")
                    .set("color", "white")
                    .set("background", "#333")
                    .set("position", "relative"); // needed for resizer
            headerRow.appendChild(th);
        }
    }

    // Adds row numbers (1-based)
    private void addRowNumbersAsFirstColumn(int rowIndex, Element tr) {
        Element rowNumberCell = new Element("th");
        rowNumberCell.getClassList().add("spreadsheet-row-header");
        rowNumberCell.setAttribute("data-row-number", String.valueOf(rowIndex + 1));

        rowNumberCell.setText(String.valueOf(rowIndex + 1));
        rowNumberCell.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "5px")
                .set("text-align", "center")
                .set("color", "white")
                .set("background", "#333")
                .set("position", "relative"); // needed for resizer
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
                            """, getElement());

            td.appendChild(editableDiv.getElement());
            tr.appendChild(td);
        }
    }

    private void attachResizeColumnsAndRowsScript() {
        getElement().executeJs(
                """
                          const table = this.querySelector('table');
                        
                          // Create resizers for headers
                          table.querySelectorAll('th').forEach(th => {
                              const resizer = document.createElement('div');
                              resizer.style.width = '5px';
                              resizer.style.height = '100%';
                              resizer.style.position = 'absolute';
                              resizer.style.top = '0';
                              resizer.style.right = '0';
                              resizer.style.cursor = 'col-resize';
                              resizer.style.userSelect = 'none';
                              th.style.position = 'relative';
                              th.appendChild(resizer);
                        
                              let startX, startWidth;
                              resizer.addEventListener('mousedown', e => {
                                  startX = e.clientX;
                                  startWidth = th.offsetWidth;
                                  const onMouseMove = e => {
                                      th.style.width = (startWidth + e.clientX - startX) + 'px';
                                  };
                                  const onMouseUp = e => {
                                      document.removeEventListener('mousemove', onMouseMove);
                                      document.removeEventListener('mouseup', onMouseUp);
                                  };
                                  document.addEventListener('mousemove', onMouseMove);
                                  document.addEventListener('mouseup', onMouseUp);
                              });
                          });
                        
                          // Optional: row resize (simpler)
                          table.querySelectorAll('tr').forEach(tr => {
                              const resizer = document.createElement('div');
                              resizer.style.height = '5px';
                              resizer.style.width = '100%';
                              resizer.style.position = 'absolute';
                              resizer.style.bottom = '0';
                              resizer.style.left = '0';
                              resizer.style.cursor = 'row-resize';
                              resizer.style.userSelect = 'none';
                              tr.style.position = 'relative';
                              tr.appendChild(resizer);
                        
                              let startY, startHeight;
                              resizer.addEventListener('mousedown', e => {
                                  startY = e.clientY;
                                  startHeight = tr.offsetHeight;
                                  const onMouseMove = e => {
                                      tr.style.height = (startHeight + e.clientY - startY) + 'px';
                                  };
                                  const onMouseUp = e => {
                                      document.removeEventListener('mousemove', onMouseMove);
                                      document.removeEventListener('mouseup', onMouseUp);
                                  };
                                  document.addEventListener('mousemove', onMouseMove);
                                  document.addEventListener('mouseup', onMouseUp);
                              });
                          });
                        """
        );
    }

}