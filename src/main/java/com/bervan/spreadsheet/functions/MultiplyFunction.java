//package com.bervan.spreadsheet.functions;
//
//import com.bervan.spreadsheet.model.Cell;
//
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class MultiplyFunction implements SpreadsheetFunction {
//    @Override
//    public String calculate(List<String> allParams, List<List<Cell>> rows) {
//        try {
//            List<Object> params = getParams(allParams, rows);
//
//            double res = 1;
//            for (int i = 0; i < params.size(); i++) {
//                try {
//                    res *= getDouble(params, i);
//                } catch (NumberFormatException e) {
//                    //we ignore it, because we want to make this function working for empty values
//                }
//            }
//            return String.valueOf(res);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "ERROR!";
//        }
//    }
//
//    @Override
//    public String getInfo() {
//        return """
//                Examples: <br>
//                    (a) =*(A1,10) <br>
//                    (b) =*(C0:C10) <br>
//                    (c) =*(C1,B2,G10)
//                """;
//    }
//
//    @Override
//    public String getName() {
//        return "*";
//    }
//}
