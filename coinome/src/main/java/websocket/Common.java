package websocket;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Common {
    public static Boolean isCoinomeDone = false;
    public static Boolean isKoinexDone = false;

    public static Map<String, Double> koinexMap = new HashMap<>();
    public static Map<String, Double> coinomeMap = new HashMap<>();


    public static void initiateExit() {
        if (isCoinomeDone && isKoinexDone) {

            StringBuilder sb = new StringBuilder("\n");
//            "date\tk-BTC\tc-BTC\t" +
//                    "k-BCH\tc-BCH\t" +
//                    "k-LTC\tc-LTC\n");

            sb.append(new Date()).append("\t");
            System.out.println("BTC difference:: \nKoinex:" +
                    koinexMap.get("BTC") + " Coinome:" + coinomeMap.get("BTC"));
            sb.append(koinexMap.get("BTC")).append("\t").append(coinomeMap.get("BTC")).append("\t");

            System.out.println("BCH difference:: \nKoinex:" +
                    koinexMap.get("BCH") + " Coinome:" + coinomeMap.get("BCH"));
            sb.append(koinexMap.get("BCH")).append("\t").append(coinomeMap.get("BCH")).append("\t");

            System.out.println("LTC difference:: \nKoinex:" +
                    koinexMap.get("LTC") + " Coinome:" + coinomeMap.get("LTC"));
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

            System.out.println("BTC %inc (k-c) = " + BTCpercent);
            System.out.println("BCH %inc (k-c) = " + BCHpercent);
            System.out.println("LTC %inc (k-c) = " + LTCpercent);
            System.exit(0);
        }
    }
}
