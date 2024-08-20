
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    String fileName = args[0];  // FileName is stored in args[0];
    String json = new String(Files.readAllBytes(Paths.get(PortfolioManagerApplication.class.getClassLoader().getResource(fileName).toURI())));
     // conver json into string
    ObjectMapper objectMapper = new ObjectMapper();
        Trades[] trades = objectMapper.readValue(json, Trades[].class);

        List<String> symbols = new ArrayList<>();
        for (Trades trade : trades) {
            symbols.add(trade.symbol);
        }

        return symbols;


  }



  





  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  /*
   ---------------------------
    //if date is invalid --> throw RuntimeException
    //reverse the returned list of the trades
    // how to bind objectmapper object with java time   
        om.registerModule(new JavaTimeModule());
   ---------------------------
   */

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
  
    String fileName = args[0];
    String reader = new String(Files.readAllBytes(Paths.get(PortfolioManagerApplication.class.getClassLoader().getResource(fileName).toURI())));
    
    LocalDate localDate = LocalDate.parse(args[1]);
    LocalDate comparisonDate = LocalDate.of(2019, 12, 12);
    if (localDate.isBefore(comparisonDate)) {
      throw new RuntimeException();
  } 
    

    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule()); // todo :  To make the time accessible
    
    RestTemplate restTemplate = new RestTemplate();
    PortfolioTrade[] portfolios = om.readValue(reader, PortfolioTrade[].class);
    TreeMap<Double, String> tickerWithCloseValues = new TreeMap<>();
    List<PortfolioTrade> list = new ArrayList<PortfolioTrade>();
    for (PortfolioTrade portfolio : portfolios) {
      list.add(portfolio);
    }

    for (PortfolioTrade symbol : list) {
      String result = restTemplate.getForObject(
          "https://api.tiingo.com/tiingo/daily/" + symbol.getSymbol() + "/prices?startDate=" + symbol.getPurchaseDate().toString()
              + "&endDate=" + localDate.toString()
              + "&token=" + "c7509b9441397f4c95f97931247b2de2ca98bac3",
          String.class);
       TiingoCandle[] collection = om.readValue(result, TiingoCandle[].class);
      tickerWithCloseValues.put(collection[collection.length-1].getClose(), symbol.getSymbol());
    }
    List<String> op = tickerWithCloseValues.values().stream().collect(Collectors.toList());
    
    return  op; 
  }

  public static List<String> reverseStringList(List<String> originalList) {
    List<String> reversedList = new ArrayList<>();

    for (int i = originalList.size() - 1; i >= 0; i--) {
        reversedList.add(originalList.get(i));
    }

    return reversedList;
}

public static String getToken() {
  return "c7509b9441397f4c95f97931247b2de2ca98bac3";
}

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
// FileName is stored in args[0];
    String json = new String(Files.readAllBytes(Paths.get(PortfolioManagerApplication.class.getClassLoader().getResource(filename).toURI())));
    
    PortfolioTrade[] trades = objectMapper.readValue(json, PortfolioTrade[].class);
    
     return Arrays.asList(trades);
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String s = "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate.toString()+"&token="+token;
     return s;
  }





  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "trades.json";
     String toStringOfObjectMapper = "ObjectMapper";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token)  {
    
    String apiUrl = prepareUrl(trade, endDate, token);

    // Initialize RestTemplate for making API requests
    RestTemplate restTemplate = new RestTemplate();

    // Make API request and deserialize the response into a List<Candle>
    TiingoCandle[] candlesArray = restTemplate.getForObject(apiUrl, TiingoCandle[].class);

    // Convert the array to a List<Candle>
    List<Candle> candlesList = Arrays.asList(candlesArray);

    // Return the list of candles
    return candlesList;
    


  }

  // Annualized Return of different companies
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

        // Company's name = args[0];
        // endDate = args[1];
        // buyPrice  = getOpeningPriceOnStartDate
        // SellPrice = getClosingPriceOnEndDate

    String fileName = args[0];
    String reader = new String(Files.readAllBytes(Paths.get(PortfolioManagerApplication.class.getClassLoader().getResource(fileName).toURI())));
    LocalDate endDate = LocalDate.parse(args[1]);
    var om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trade = om.readValue(reader, PortfolioTrade[].class);
    String token = getToken();

    String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    

    List<AnnualizedReturn> annualReturn = new ArrayList<>();

    for (PortfolioTrade portfolioTrade : trade) {
      
      String url = uri.replace("$APIKEY", token).replace("$SYMBOL", portfolioTrade.getSymbol())
          .replace("$STARTDATE", portfolioTrade.getPurchaseDate().toString())
          .replace("$ENDDATE", endDate.toString());

    
      RestTemplate restTemplate = new RestTemplate();
      String result = restTemplate.getForObject(url, String.class);
      TiingoCandle[] tiingoCandles = om.readValue(result, TiingoCandle[].class);

      List<Candle> list = Arrays.asList(tiingoCandles);

      double sellPrice = getClosingPriceOnEndDate(list);
      double buyPrice = getOpeningPriceOnStartDate(list);

      annualReturn.add(calculateAnnualizedReturns(endDate,portfolioTrade,buyPrice,sellPrice));
    }
     
     // use this method to sort when we have double values
      annualReturn.sort((s,a)->a.getAnnualizedReturn().compareTo(s.getAnnualizedReturn()));

      return  annualReturn;
     
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn
  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

    double totalReturn = (sellPrice - buyPrice) / buyPrice;
    double totalnumdays = ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate);
    double totalnumyears = totalnumdays / 365;
    double power = 1 / totalnumyears;

    double annualizedreturns = Math.pow((1 + totalReturn),power) - 1;
    
    return new AnnualizedReturn(trade.getSymbol(),annualizedreturns, totalReturn);
  }













  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    printJsonObject(mainCalculateSingleReturn(args));

  }










}

