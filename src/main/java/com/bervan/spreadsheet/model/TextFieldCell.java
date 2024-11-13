package com.bervan.spreadsheet.model;

import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;

public class TextFieldCell extends TextArea {
    public boolean isFocused;
    public Integer number;
    public Integer columnIndex;
    private final static Integer minHeight = 50;


    public TextFieldCell(int number, int columnIndex, List<TextFieldCell> spreadsheetVaadinCells) {
        this.columnIndex = columnIndex;
        this.number = number;
        addClassName("spreadsheet-cell");
        addClassName("spreadsheet-cell-row" + number);
        addClassName("spreadsheet-cell-cl" + columnIndex);
        setWidth("100%");
        setMaxHeight("300px");
        setMinHeight(minHeight + "px");

        setHeight(null);

        updateHeight(spreadsheetVaadinCells);

        addValueChangeListener(changeEvent -> {
            updateHeight(spreadsheetVaadinCells);
        });
    }

    private void updateHeight(List<TextFieldCell> spreadsheetVaadinCells) {
        List<TextFieldCell> allCellsInRow = spreadsheetVaadinCells.stream().filter(e -> e.number.equals(this.number)).toList();

        PendingJavaScriptResult pendingJavaScriptResult = getElement().executeJs(
//                "  setTimeout(() => {" +
                        "const cells = Array.from(document.querySelectorAll('.spreadsheet-cell-row" + this.number + "'));" +
                        "const cellWithMaxNewLines = cells.reduce((maxCell, currentCell) => {" +
                        "  const maxNewLines = (maxCell?.querySelector('textarea')?.value.match(/\\n/g) || []).length;" +
                        "  const currentNewLines = (currentCell.querySelector('textarea')?.value.match(/\\n/g) || []).length;" +
                        "  console.log('Checking cell:', currentCell, 'Newlines:', currentNewLines);" +
                        "  return currentNewLines > maxNewLines ? currentCell : maxCell;" +
                        "}, null);" +
                        " if(!cellWithMaxNewLines) return;" +
                        "  const maxHeight = cellWithMaxNewLines.querySelector('textarea').offsetHeight;" +
                        "    cells.forEach(cell => {" +
                        "      const textArea = cell.querySelector('textarea');" +
                        "      if (textArea) {" +
                        "        console.log('Setting height for cell:', cell, 'to:', maxHeight + 'px');" +
                        "        textArea.style.setProperty('height', maxHeight  + 10 + 'px', 'important');" +
                        "        cell.style.setProperty('height', maxHeight + 15 + 'px', 'important');" +
                        "      } else {" +
                        "        console.log('No textarea found for cell:', cell);" +
                        "      }" +
                        "    });"
//                        "  }, 100);"
        );

        pendingJavaScriptResult.then(result -> {

        });

        pendingJavaScriptResult.then(result -> {

        });


    }
}

