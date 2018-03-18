package websocket;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import websocket.binance.*;
import websocket.coinome.CoinomeWebsocket;
import websocket.koinex.KoinexWebsocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSocket {

  public static void main(String[] args) {
    CoinomeWebsocket cws = new CoinomeWebsocket();
    KoinexWebsocket kws = new KoinexWebsocket();
    BinanceBtcUsdtWebsocket binanceWebsocket = new BinanceBtcUsdtWebsocket();
    BinanceXrpBtcWebsocket binanceXrpBtcWebsocket = new BinanceXrpBtcWebsocket();
    BinanceEtherUsdtWebsocket binanceEtherUsdtWebsocket = new BinanceEtherUsdtWebsocket();
    BinanceLtcUsdtWebsocket binanceLtcUsdtWebsocket = new BinanceLtcUsdtWebsocket();
    BinanceBchUsdtWebsocket binanceBchUsdtWebsocket = new BinanceBchUsdtWebsocket();
    try {

      String urlToRead = "http://www.xe.com/currencyconverter/convert/?Amount=1&From=INR&To=USD";
      StringBuilder html = new StringBuilder();
      URL url = new URL(urlToRead);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
        html.append(line);
      }
      rd.close();
      Document doc = Jsoup.parse(html.toString());
      Common.INR_USD = new Double(doc.getElementsByClass("uccResultAmount").first().text());

      cws.runSocket();
      kws.runSocket();
      binanceWebsocket.runSocket();
      binanceXrpBtcWebsocket.runSocket();
      binanceEtherUsdtWebsocket.runSocket();
      binanceLtcUsdtWebsocket.runSocket();
      binanceBchUsdtWebsocket.runSocket();
    } catch (Exception e) {
    }
  }
}
