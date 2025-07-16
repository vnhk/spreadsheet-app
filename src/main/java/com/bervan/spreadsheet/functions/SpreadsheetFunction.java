package com.bervan.spreadsheet.functions;

import java.util.List;

public interface SpreadsheetFunction {
    FunctionArgument calculate(List<FunctionArgument> functionArguments);

    String getInfo();

    String getName();
}
