package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractPageLayout;
import com.vaadin.flow.component.html.Hr;

public class SpreadsheetPageLayout extends AbstractPageLayout {
    public SpreadsheetPageLayout(boolean isEdit, String spreadsheetName) {
        super(AbstractSpreadsheetsView.ROUTE_NAME);

        addButton(menuButtonsRow, AbstractSpreadsheetsView.ROUTE_NAME, "List");
        if (isEdit) {
            addButton(menuButtonsRow, AbstractSpreadsheetView.ROUTE_NAME, spreadsheetName);
        }

        add(menuButtonsRow);
        add(new Hr());
    }
}
