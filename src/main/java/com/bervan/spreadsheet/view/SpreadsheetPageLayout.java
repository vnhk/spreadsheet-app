package com.bervan.spreadsheet.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class SpreadsheetPageLayout extends MenuNavigationComponent {
    public SpreadsheetPageLayout(boolean isEdit, String spreadsheetName, String routeName) {
        super(routeName);

        addButtonIfVisible(menuButtonsRow, AbstractSpreadsheetsView.ROUTE_NAME, "List", VaadinIcon.HOME.create());
        if (isEdit) {
            addButtonIfVisible(menuButtonsRow, AbstractSpreadsheetView.ROUTE_NAME, spreadsheetName, VaadinIcon.FILE.create());
        }

        add(menuButtonsRow);
    }
}
