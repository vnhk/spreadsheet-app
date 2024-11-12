package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CurrencyFunction implements SpreadsheetFunction {

    @Override
    public String calculate(List<String> allParams, List<SpreadsheetRow> rows) {

        //=CURRENCY(FROM,TO,AMOUNT)

        try {
            List<Object> params = getParams_careAboutOrder(allParams, rows);

            if (params.size() != 3) {
                throw new RuntimeException("Incorrect Params");
            }

            String fromCurr = getString(params, 0);
            String toCurr = getString(params, 1);
            double amount = getDouble(params, 2);


            return String.valueOf(convertCurrency(amount, fromCurr, toCurr));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR!";
        }
    }

    @Override
    public String getInfo() {
        return """
                =CURRENCY(FROM,TO,AMOUNT) <br>
                Example: =CURRENCY(PLN,EUR,10000)
                """;
    }

    @Override
    public String getName() {
        return "CURRENCY";
    }

    public String convertCurrency(double amount, String from, String to) {
        String url = String.format("https://www.xe.com/currencyconverter/convert/?Amount=%s&From=%s&To=%s", amount, from, to);

        try {
            // Fetch the page using Jsoup
            Document document = Jsoup.connect(url).get();

            // Select the element using the data-testid attribute
            Element conversionElement = document.selectFirst("[data-testid=conversion]");

            if (conversionElement != null) {
                // Assuming the exchange rate is in the second <p> tag within the selected div
                Element rateElement = conversionElement.select("p").get(1);
                if (rateElement != null) {
                    return rateElement.text().split(" ")[0].replaceAll(",", "");
                } else {
                    throw new IOException("Conversion rate element not found");
                }
            } else {
                throw new IOException("Conversion container not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to retrieve conversion rate.";
        }
    }
}
