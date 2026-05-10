package com.bervan.spreadsheet.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColumnOperationRequest {
    private String body;
    private String action;
    private int columnNumber;
}
