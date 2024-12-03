package com.bervan.spreadsheet.view;

import com.bervan.common.MenuNavigationComponent;

public class SpreadsheetPageLayout extends MenuNavigationComponent {
    public SpreadsheetPageLayout(boolean isEdit, String spreadsheetName) {
        super(AbstractSpreadsheetsView.ROUTE_NAME);

        addButton(menuButtonsRow, AbstractSpreadsheetsView.ROUTE_NAME, "List");
        if (isEdit) {
            addButton(menuButtonsRow, AbstractSpreadsheetView.ROUTE_NAME, spreadsheetName);
        }

        add(menuButtonsRow);
    }
}
