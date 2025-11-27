package com.bervan.spreadsheet.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.UUID;

public abstract class AbstractSpreadsheetsView extends AbstractBervanTableView<UUID, Spreadsheet> {
    public static final String ROUTE_NAME = "/spreadsheet-app/spreadsheets";

    public AbstractSpreadsheetsView(BaseService<UUID, Spreadsheet> service, BervanViewConfig bervanViewConfig) {
        super(new SpreadsheetPageLayout(false, null, AbstractSpreadsheetsView.ROUTE_NAME), service, bervanViewConfig, Spreadsheet.class);
        renderCommonComponents();
    }

    @Override
    protected Grid<Spreadsheet> getGrid() {
        Grid<Spreadsheet> grid = new Grid<>(Spreadsheet.class, false);
        buildGridAutomatically(grid);

        return grid;
    }

    @Override
    protected void preColumnAutoCreation(Grid<Spreadsheet> grid) {
        grid.addComponentColumn(entity -> {
                    Icon linkIcon = new Icon(VaadinIcon.LINK);
                    linkIcon.getStyle().set("cursor", "pointer");
                    return new Anchor(ROUTE_NAME + "/" + entity.getName(), new HorizontalLayout(linkIcon));
                }).setKey("link")
                .setWidth("6px")
                .setResizable(false);
    }
}
