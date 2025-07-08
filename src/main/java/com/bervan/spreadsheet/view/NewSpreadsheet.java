package com.bervan.spreadsheet.view;

import com.bervan.spreadsheet.model.Cell;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

@JsModule("./spreadsheet.js")
public class NewSpreadsheet extends VerticalLayout {

    public NewSpreadsheet(Cell[][] cells) {
        Div spreadsheetContainer = new Div();
        spreadsheetContainer.setId("spreadsheet-container");
        spreadsheetContainer.setHeight("400px");
        spreadsheetContainer.setWidth("100%");
        this.add(spreadsheetContainer);

        List<List<Object>> result = new ArrayList<>();

        for (Cell[] row : cells) {
            List<Object> jsRow = new ArrayList<>();
            for (Cell cell : row) {
                jsRow.add(cell != null ? cell.getValue() : "");
            }
            result.add(jsRow);
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        getElement().executeJs(
                "initSpreadsheet($0, JSON.parse($1));",
                spreadsheetContainer.getElement(),
                json
        );

        UI.getCurrent().getPage().addStyleSheet("https://cdn.jsdelivr.net/npm/handsontable/styles/handsontable.min.css");
        UI.getCurrent().getPage().addStyleSheet("https://cdn.jsdelivr.net/npm/handsontable/styles/ht-theme-main.min.css");
    }
}
