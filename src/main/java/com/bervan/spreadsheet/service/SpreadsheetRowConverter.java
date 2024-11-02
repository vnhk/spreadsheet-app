package com.bervan.spreadsheet.service;

import com.bervan.spreadsheet.model.ColumnConfig;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.google.common.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

public class SpreadsheetRowConverter {

    private static final Gson gson = new GsonBuilder().create();

    public static String serializeSpreadsheetBody(List<SpreadsheetRow> spreadsheetRows) {
        return gson.toJson(spreadsheetRows);
    }

    public static String serializeColumnsConfig(List<ColumnConfig> columnsConfig) {
        return gson.toJson(columnsConfig);
    }

    public static List<SpreadsheetRow> deserializeSpreadsheetBody(String json) {
        Type listType = new TypeToken<List<SpreadsheetRow>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public static List<ColumnConfig> deserializeColumnsConfig(String json) {
        Type listType = new TypeToken<List<ColumnConfig>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}