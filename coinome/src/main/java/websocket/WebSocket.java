package websocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import websocket.binance.BinanceBtcUsdtWebsocket;
import websocket.binance.BinanceEtherUsdtWebsocket;
import websocket.binance.BinanceLtcUsdtWebsocket;
import websocket.binance.BinanceXrpBtcWebsocket;
import websocket.coinome.CoinomeWebsocket;
import websocket.koinex.KoinexWebsocket;

public class WebSocket {

  public static void main(String[] args) {
    CoinomeWebsocket cws = new CoinomeWebsocket();
    KoinexWebsocket kws = new KoinexWebsocket();
    BinanceBtcUsdtWebsocket binanceWebsocket = new BinanceBtcUsdtWebsocket();
    BinanceXrpBtcWebsocket binanceXrpBtcWebsocket = new BinanceXrpBtcWebsocket();
    BinanceEtherUsdtWebsocket binanceEtherUsdtWebsocket = new BinanceEtherUsdtWebsocket();
    BinanceLtcUsdtWebsocket binanceLtcUsdtWebsocket = new BinanceLtcUsdtWebsocket();
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
    } catch (Exception e) {
    }
  }
}
