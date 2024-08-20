
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }



  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }


private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }


  // Below function to return ObjectMapper so that we don't need to create everyTime
  private static ObjectMapper getObjectMapper() {
    var om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    return om;
  }

  // Below function is to get the list of open close high low and date
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

    // var om = getObjectMapper();

    // if(from.compareTo(to)>=0)
    // { 
    //   throw new RuntimeException() ;
    // }
    // String url = buildUri(symbol, from, to);
    // String result = restTemplate.getForObject(url, String.class);
    // TiingoCandle[]  tCandles =   om.readValue(result, TiingoCandle[].class);

    // return Arrays.asList(tCandles);
  //   if(from.compareTo(to)>=0)
  //   { 
  //     throw new RuntimeException() ;
  //   }
  //  String url= buildUri(symbol, from, to);

  //  TiingoCandle[] stokesStartToEndDate = restTemplate.getForObject(url ,TiingoCandle[].class);

  //  if(stokesStartToEndDate == null)
  //  {
  //   return   new ArrayList<Candle>();
  //  }
  //  else{

  //  List<Candle> stockList = Arrays.asList(stokesStartToEndDate);
  //   return stockList;
  //  }
  return stockQuotesService.getStockQuote(symbol, from, to);
  }


  // Method to build url on the basis of given params
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    String token = "c7509b9441397f4c95f97931247b2de2ca98bac3";

    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
        .replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());

    return url;
  }



  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate)  {
        List<AnnualizedReturn> annualReturn = new ArrayList<>();
        for (PortfolioTrade portfolioTrade : portfolioTrades)
        {
          List<Candle> list;
          try {
            list = getStockQuote(portfolioTrade.getSymbol() , portfolioTrade.getPurchaseDate(), endDate);
            double sellPrice = getClosingPriceOnEndDate(list);
            double buyPrice = getOpeningPriceOnStartDate(list);
            annualReturn.add(calculateAnnualizedReturns(endDate,portfolioTrade,buyPrice,sellPrice));
          } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
         
        }

        annualReturn.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn));
        Collections.reverse(annualReturn);
        
    
        return annualReturn;
  }


  private static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
 } 

 private static Double getClosingPriceOnEndDate(List<Candle> candles) {
  return candles.get(candles.size()-1).getClose();
}



  // Method to calcuate Annunalized Return
  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    double totalReturn = (sellPrice - buyPrice) / buyPrice;
    double totalnumdays = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
    double totalnumyears = totalnumdays / 365;
    double power = 1 / totalnumyears;

    double annualizedreturns = Math.pow((1 + totalReturn), power) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedreturns, totalReturn);
  }
}





