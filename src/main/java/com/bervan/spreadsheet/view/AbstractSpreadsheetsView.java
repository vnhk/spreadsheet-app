package com.bervan.spreadsheet.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.spreadsheet.model.Spreadsheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;

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

        if (grid.getColumnByKey("name") != null) {
            grid.getColumnByKey("name").setRenderer(new ComponentRenderer<>(
                    entity -> new Anchor(ROUTE_NAME + "/" + entity.getName(), entity.getName())
            ));
        }

        return grid;
    }
}
