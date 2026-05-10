package com.bervan.spreadsheet.api;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpreadsheetDataDto {
    private UUID id;
    private String name;
    private String description;
    private List<SpreadsheetRow> rows;
    private Map<Integer, Integer> columnWidths;
}
