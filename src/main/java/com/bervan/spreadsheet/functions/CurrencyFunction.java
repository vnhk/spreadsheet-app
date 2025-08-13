package com.bervan.spreadsheet.functions;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.bervan.spreadsheet.functions.CurrencyFunction.FUNCTION_NAME;

@Component("F#" + FUNCTION_NAME)
@Slf4j
public class CurrencyFunction implements SpreadsheetFunction {
    public final static String FUNCTION_NAME = "CURRENCY";

    @Override
    public FunctionArgument calculate(List<FunctionArgument> functionArguments) {

        if (functionArguments.size() != 3) {
            throw new RuntimeException("Incorrect Params for " + FUNCTION_NAME + " function");
        }

        return new ConstantArgument(String.valueOf(convertCurrency(functionArguments.get(0).asDouble(), functionArguments.get(1).asText(), functionArguments.get(2).asText())));
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
        return FUNCTION_NAME;
    }

    public String convertCurrency(double amount, String from, String to) {
        String url = String.format("https://www.xe.com/currencyconverter/convert/?Amount=%s&From=%s&To=%s", amount, from, to);

        try {
            Document document = Jsoup.connect(url).get();

            Element conversionElement = document.selectFirst("[data-testid=conversion]");

            if (conversionElement != null) {
                Element rateElement = conversionElement.select("p").get(1);
                if (rateElement != null) {
                    return rateElement.text().split(" ")[0].replaceAll(",", "");
                } else {
                    throw new IOException("Conversion rate element not found");
                }
            } else {
                throw new IOException("Conversion container not found");
            }
        } catch (Exception e) {
            log.error("Failed to retrieve conversion rate!", e);
            return "Failed to retrieve conversion rate.";
        }
    }
}
