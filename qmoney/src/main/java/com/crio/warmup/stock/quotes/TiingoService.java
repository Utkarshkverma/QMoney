
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
   
  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    var om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    return om;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    var om = getObjectMapper();

    if(from.compareTo(to)>=0)
    { 
      throw new RuntimeException() ;
    }
    String url = buildUri(symbol, from, to);
    String result = restTemplate.getForObject(url, String.class);
    TiingoCandle[]  tCandles =   om.readValue(result, TiingoCandle[].class);

    return Arrays.asList(tCandles);
    
  }


  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    String token = "c7509b9441397f4c95f97931247b2de2ca98bac3";

    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
        .replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());

    return url;
  }

  

}
