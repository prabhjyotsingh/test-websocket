package websocket.coinome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import websocket.Common;

public class CoinomeWebsocket {

  Boolean isInit = false;
  WebSocketClient mWs = null;
  HashMap<String, String> headers = new HashMap<String, String>();
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public CoinomeWebsocket() {
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "en-IN,en;q=0.9,en-GB;q=0.8,en-US;q=0.7,hi;q=0.6");
    headers.put("Cache-Control", "no-cache");
    headers.put("Connection", "Upgrade");
    headers.put("DNT", "1");
    headers.put("Host", "ws-ap2.pusher.com");
    headers.put("Origin", "https://www.coinome.com");
    headers.put("Pragma", "no-cache");
    headers.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
    headers.put("Sec-WebSocket-Key", "nU6wBFWYvinQTLSxGv+d1A==");
    headers.put("Sec-WebSocket-Version", "13");
    headers.put("Upgrade", "websocket");
    headers.put("User-Agent",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/64.0.3282.167 Safari/537.36");
    headers.put("Sec-WebSocket-Protocol", "actioncable-v1-json, actioncable-unsupported");
    headers.put("Wec-WebSocket-Accept", "QOBzphvTMhcBuD8PdfwnD78MurE=");
  }

  public void runSocket() {
    runViaHTML();
  }

  private void runViaWebSocket() {
    try {
      mWs = createWebSocket();
      mWs.connect();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }


  public void runViaHTML() {
    (new Thread() {
      public void run() {
        while (true) {
          Common.isCoinomeDone = false;
          Common.coinomeMap = new HashMap<>();
          Common.coinomeMap.clear();
          while (Common.coinomeMap.size() != 3) {
            try {
              fillDetails("ltc");
              fillDetails("BTC");
              fillDetails("BCH");
            } catch (Exception e) {
              e.printStackTrace();
              Common.threadSleep(60 * 1000);
            }
          }

          Common.isCoinomeDone = true;
          Common.initiateExit();
          Common.threadSleep(2 * 60 * 1000);
        }
      }
    }).start();
  }


  private void fillDetails(String type) throws Exception {
    String urlToRead = "https://www.coinome.com/exchange/" + type + "-inr";
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

    for (Element element : doc.getElementsByClass("module-group").get(1)
        .getElementsByClass("module")) {
      if (element.text().contains("INR")) {
        Double thisPrice = new Double(
            element.text().split("INR")[2].split(" ")[1].replaceAll(",", ""));
        if (element.text().contains("BTC")) {
          Common.coinomeMap.put("BTC", thisPrice);
        }
        if (element.text().contains("BCH")) {
          Common.coinomeMap.put("BCH", thisPrice);
        }
        if (element.text().contains("LTC")) {
          Common.coinomeMap.put("LTC", thisPrice);
        }
      }
    }
  }

  private WebSocketClient createWebSocket() throws URISyntaxException {
    return new WebSocketClient(new URI("wss://ws-ap2.pusher" +
        ".com/app/589dd441f02189a2372f?protocol=7&client=js&version=4.2.2&flash=false"),
        new CoinomeDraft(), headers,
        2000) {
      @Override
      public void onMessage(String message) {
        Map mapMessage = gson.fromJson(message, Map.class);

        if (mapMessage.get("type") != null) {
          if (!mapMessage.get("type").equals("ping")) {
            System.out.println(mapMessage);
          }

        } else if (mapMessage.get("event").equals("ticker")) {

          ArrayList marketRates = (ArrayList) gson.fromJson((String) mapMessage.get("data"),
              PublicChannelObject.class).getMessage().get(0);
          if (marketRates.get(0).equals("market_rates")) {

            ArrayList<ArrayList> marketRatesArray = (ArrayList<ArrayList>) marketRates.get(1);

            for (ArrayList rate : marketRatesArray) {
              Double thisPrice = new Double((String) rate.get(1));
              switch ((String) rate.get(0)) {
                case "BTC/INR":
                  Common.coinomeMap.put("BTC", thisPrice);
                  break;
                case "BCH/INR":
                  Common.coinomeMap.put("BCH", thisPrice);
                  break;
                case "LTC/INR":
                  Common.coinomeMap.put("LTC", thisPrice);
                  break;
              }

            }
            Common.isCoinomeDone = true;
            Common.initiateExit();
          }
        }

        if (!isInit) {
          isInit = true;
          Map obj;
          Map objInner;
          obj = new HashMap();
          objInner = new HashMap();
          obj.put("event", "pusher:subscribe");
          objInner.put("channel", "public");
          obj.put("data", objInner);
          message = gson.toJson(obj);
          mWs.send(message);

        }
      }

      @Override
      public void onOpen(ServerHandshake handshake) {
        System.out.println("opened connection");
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed connection");
        try {
          isInit = false;
          mWs = createWebSocket();
        } catch (URISyntaxException e) {
        }
        mWs.connect();
      }

      @Override
      public void onError(Exception ex) {
        ex.printStackTrace();
      }

    };
  }
}