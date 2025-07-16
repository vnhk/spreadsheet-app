package com.bervan.spreadsheet.functions;

public interface CellResolver {
    FunctionArgument resolve(String cellId);
}