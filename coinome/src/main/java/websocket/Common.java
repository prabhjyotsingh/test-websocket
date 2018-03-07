package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Common {

  public static Boolean isCoinomeDone = false;
  public static Boolean isKoinexDone = false;

  public static Map<String, Double> koinexMap = new HashMap<>();
  public static Map<String, Double> coinomeMap = new HashMap<>();
  public static Map<String, Double> binanceMap = new HashMap<>();
  public static Double INR_USD;
  private static Boolean isExecuted = false;
  private static Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

  public static void initiateExit() {
    if (isCoinomeDone && isKoinexDone && !isExecuted) {
      isExecuted = true;
      StringBuilder sb = new StringBuilder("\n");
      StringBuilder sysout = new StringBuilder("\n");

      try {
        Double LTCpercent = (koinexMap.get("LTC") - coinomeMap.get("LTC")) /
          Math.min(koinexMap.get("LTC"), coinomeMap.get("LTC")) * 100;
        Double BCHpercent = (koinexMap.get("BCH") - coinomeMap.get("BCH")) /
          Math.min(koinexMap.get("BCH"), coinomeMap.get("BCH")) * 100;
        Double BTCpercent = (koinexMap.get("BTC") - coinomeMap.get("BTC")) /
          Math.min(koinexMap.get("BTC"), coinomeMap.get("BTC")) * 100;

        Double XRPpercent =
          ((koinexMap.get("XRP") * INR_USD) - (binanceMap.get("XRP_BTC") * binanceMap
            .get("BTC_USDT"))) /
            Math.min((koinexMap.get("XRP") * INR_USD),
              (binanceMap.get("XRP_BTC") * binanceMap.get("BTC_USDT"))) * 100;
        Double ETHpercent = ((koinexMap.get("ETH") * INR_USD) - (binanceMap.get("ETHER_USDT"))) /
          Math.min((koinexMap.get("ETH") * INR_USD), (binanceMap.get("ETHER_USDT"))) * 100;
        Double LTC_USD_percent = ((koinexMap.get("LTC") * INR_USD) - (binanceMap.get("LTC_USDT"))) /
          Math.min((koinexMap.get("LTC") * INR_USD), (binanceMap.get("LTC_USDT"))) * 100;
        Double BCH_USD_percent = ((koinexMap.get("BCH") * INR_USD) - (binanceMap.get("BCH_USDT"))) /
          Math.min((koinexMap.get("BCH") * INR_USD), (binanceMap.get("BCH_USDT"))) * 100;


        //Print Coinome
        {
          Double btcBase = 0.00726415;
          Double ltcBase = 0.45525918;
          Double bchBase = 0.07341229;
          Double totalCash = 0.0;
          totalCash += btcBase * Common.coinomeMap.get("BTC");
          totalCash += bchBase * Common.coinomeMap.get("BCH");
          totalCash += ltcBase * Common.coinomeMap.get("LTC");
          System.out.println("BTC == " + btcBase * Common.coinomeMap.get("BTC"));
          System.out.println("BCH == " + bchBase * Common.coinomeMap.get("BCH"));
          System.out.println("LTC == " + ltcBase * Common.coinomeMap.get("LTC"));
          System.out.println("Total cash == " + totalCash);
        }

        DecimalFormat df = new DecimalFormat("##.##");
        sysout.append("LTC %inc (k-c) = " + df.format(LTCpercent)).append("\n");
        sysout.append("BCH %inc (k-c) = " + df.format(BCHpercent)).append("\n");
        sysout.append("BTC %inc (k-c) = " + df.format(BTCpercent)).append("\n");
        sysout.append("XRP %inc (k-b) = " + df.format(XRPpercent)).append("\n");
        sysout.append("ETH %inc (k-b) = " + df.format(ETHpercent)).append("\n");
        sysout.append("LTC %inc (k-b) = " + df.format(LTC_USD_percent)).append("\n");
        sysout.append("BCH %inc (k-b) = " + df.format(BCH_USD_percent)).append("\n");

        sysout.append("LTC difference:: \nKoinex:" +
          koinexMap.get("LTC") + " Coinome:" + coinomeMap.get("LTC")).append("\n");
        sysout.append("BCH difference:: \nKoinex:" +
          koinexMap.get("BCH") + " Coinome:" + coinomeMap.get("BCH")).append("\n");
        sysout.append("BTC difference:: \nKoinex:" +
          koinexMap.get("BTC") + " Coinome:" + coinomeMap.get("BTC")).append("\n");

        sb.append(getCurrentISTTime()).append("\t");
        sb.append(koinexMap.get("BTC")).append("\t").append(coinomeMap.get("BTC")).append("\t");
        sb.append(koinexMap.get("BCH")).append("\t").append(coinomeMap.get("BCH")).append("\t");
        sb.append(koinexMap.get("LTC")).append("\t").append(coinomeMap.get("LTC")).append("\t");

        sb.append(df.format(koinexMap.get("XRP") * INR_USD)).append("\t");
        sb.append(df.format(binanceMap.get("XRP_BTC") * binanceMap.get("BTC_USDT"))).append("\t");

        sb.append(df.format(koinexMap.get("ETH") * INR_USD)).append("\t");
        sb.append(df.format(binanceMap.get("ETHER_USDT"))).append("\t");

        sb.append(df.format(koinexMap.get("LTC") * INR_USD)).append("\t");
        sb.append(df.format(binanceMap.get("LTC_USDT"))).append("\t");
        sb.append(df.format(binanceMap.get("BCH_USDT") / INR_USD)).append("\t");

        File f = new File("abc.csv");
        try {
          FileUtils.write(f, sb.toString(), true);
        } catch (IOException e) {

        }

        if (Math.abs(BTCpercent) > 2.9) {
//        SendMail sm = new SendMail();
//        sm.sendMail(sysout.toString());
        }
        System.out.println(sysout.toString());
      } catch (Exception e) {
        System.out.println(e);
      }

      try {
        Thread.sleep(300000);
        isExecuted = false;
      } catch (InterruptedException e1) {

      }


    }
  }

  public static String getCurrentISTTime() {
    return gson.toJson(new Date(), Date.class).replaceAll("\"", "");
  }
}
