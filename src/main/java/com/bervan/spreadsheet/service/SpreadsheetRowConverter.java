package com.bervan.spreadsheet.service;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.google.common.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

public class SpreadsheetRowConverter {

    private static final Gson gson = new GsonBuilder().create();

    public static String serializeSpreadsheet(List<SpreadsheetRow> spreadsheetRows) {
        return gson.toJson(spreadsheetRows);
    }

    public static List<SpreadsheetRow> deserializeSpreadsheet(String json) {
        Type listType = new TypeToken<List<SpreadsheetRow>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}