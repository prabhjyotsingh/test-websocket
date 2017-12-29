package websocket;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import mail.SendMail;
import org.apache.commons.io.FileUtils;

public class Common {

  public static Boolean isCoinomeDone = false;
  public static Boolean isKoinexDone = false;

  public static Map<String, Double> koinexMap = new HashMap<>();
  public static Map<String, Double> coinomeMap = new HashMap<>();
  private static Boolean isExecuted = false;

  public static void initiateExit() {
    if (isCoinomeDone && isKoinexDone) {

      if (isExecuted) {
        return;
      }
      isExecuted = true;
      StringBuilder sb = new StringBuilder("\n");
      StringBuilder sysout = new StringBuilder("\n");
//            "date\tk-BTC\tc-BTC\t" +
//                    "k-BCH\tc-BCH\t" +
//                    "k-LTC\tc-LTC\n");

      sb.append(getCurrentISTTime()).append("\t");
      sysout.append("BTC difference:: \nKoinex:" +
          koinexMap.get("BTC") + " Coinome:" + coinomeMap.get("BTC")).append("\n");
      sb.append(koinexMap.get("BTC")).append("\t").append(coinomeMap.get("BTC")).append("\t");

      sysout.append("BCH difference:: \nKoinex:" +
          koinexMap.get("BCH") + " Coinome:" + coinomeMap.get("BCH")).append("\n");
      sb.append(koinexMap.get("BCH")).append("\t").append(coinomeMap.get("BCH")).append("\t");

      sysout.append("LTC difference:: \nKoinex:" +
          koinexMap.get("LTC") + " Coinome:" + coinomeMap.get("LTC")).append("\n");
      sb.append(koinexMap.get("LTC")).append("\t").append(coinomeMap.get("LTC")).append("\t");

      File f = new File("abc.csv");
      try {
        FileUtils.write(f, sb.toString(), true);
      } catch (IOException e) {

      }
      Double BTCpercent = (koinexMap.get("BTC") - coinomeMap.get("BTC")) /
          Math.min(koinexMap.get("BTC"), coinomeMap.get("BTC")) * 100;
      Double BCHpercent = (koinexMap.get("BCH") - coinomeMap.get("BCH")) /
          Math.min(koinexMap.get("BCH"), coinomeMap.get("BCH")) * 100;
      Double LTCpercent = (koinexMap.get("LTC") - coinomeMap.get("LTC")) /
          Math.min(koinexMap.get("LTC"), coinomeMap.get("LTC")) * 100;

      DecimalFormat df = new DecimalFormat("##.##");
      sysout.append("BTC %inc (k-c) = " + df.format(BTCpercent)).append("\n");
      sysout.append("BCH %inc (k-c) = " + df.format(BCHpercent)).append("\n");
      sysout.append("LTC %inc (k-c) = " + df.format(LTCpercent));

      //      if (Math.abs(BTCpercent) > 2.9 || Math.abs(BCHpercent) > 2.9 || Math.abs(LTCpercent) > 2.9) {
      if (Math.abs(LTCpercent) > 2.9) {
        SendMail sm = new SendMail();
        sm.sendMail(sysout.toString());
      }
      System.out.println(sysout.toString());

      //      System.exit(0);

      try {
        Thread.sleep(300000);
        isExecuted = false;
      } catch (InterruptedException e) {
      }
    }
  }

  public static Date getCurrentISTTime() {
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    TimeZone.setDefault(TimeZone.getTimeZone("IST"));

    return cal.getTime();
  }
}
