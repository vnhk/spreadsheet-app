package com.bervan.spreadsheet.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class SpreadsheetPageLayout extends MenuNavigationComponent {
    public SpreadsheetPageLayout(boolean isEdit, String spreadsheetName) {
        super(AbstractSpreadsheetsView.ROUTE_NAME);

        addButtonIfVisible(menuButtonsRow, AbstractSpreadsheetsView.ROUTE_NAME, "List", VaadinIcon.HOME.create());
        if (isEdit) {
            addButtonIfVisible(menuButtonsRow, AbstractSpreadsheetView.ROUTE_NAME, spreadsheetName, VaadinIcon.HOME.create());
        }

        add(menuButtonsRow);
    }
}
