package com.bervan.spreadsheet.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;


public abstract class AbstractSpreadsheetsView extends AbstractTableView<Spreadsheet> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets";

    public AbstractSpreadsheetsView(BaseService<Spreadsheet> service, BervanLogger log) {
        super(new SpreadsheetPageLayout(false, null), service, log, Spreadsheet.class);
        renderCommonComponents();
    }

    @Override
    protected Grid<Spreadsheet> getGrid() {
        Grid<Spreadsheet> grid = new Grid<>(Spreadsheet.class, false);
        grid.addComponentColumn(entity -> {
                    Icon linkIcon = new Icon(VaadinIcon.LINK);
                    linkIcon.getStyle().set("cursor", "pointer");
                    return new Anchor(ROUTE_NAME + "/" + entity.getName(), new HorizontalLayout(linkIcon));
                }).setKey("link")
                .setResizable(true);
        buildGridAutomatically(grid);

        return grid;
    }
}
