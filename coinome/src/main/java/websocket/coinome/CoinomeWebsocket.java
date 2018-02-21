package websocket.coinome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import websocket.Common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
      "Chrome/64.0.3282.167 Safari/537.36");
    headers.put("Sec-WebSocket-Protocol", "actioncable-v1-json, actioncable-unsupported");
    headers.put("Wec-WebSocket-Accept", "QOBzphvTMhcBuD8PdfwnD78MurE=");
  }

  public void runSocket() throws URISyntaxException {
    mWs = createWebSocket();
    mWs.connect();
  }

  private WebSocketClient createWebSocket() throws URISyntaxException {
    return new WebSocketClient(new URI("wss://ws-ap2.pusher" +
      ".com/app/589dd441f02189a2372f?protocol=7&client=js&version=4.2.2&flash=false"), new CoinomeDraft(), headers,
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

            Double btcBase = 0.00726415;
            Double ltcBase = 0.45525918;
            Double bchBase = 0.07341229;
            Double totalCash = 0.0;

            for (ArrayList rate : marketRatesArray) {
              Double thisPrice = new Double((String) rate.get(1));
              switch ((String) rate.get(0)) {
                case "BTC/INR":
                  System.out.println("BTC == " + btcBase * thisPrice);
                  totalCash += btcBase * thisPrice;
                  Common.coinomeMap.put("BTC", thisPrice);
                  break;
                case "BCH/INR":
                  System.out.println("BCH == " + bchBase * thisPrice);
                  totalCash += bchBase * thisPrice;
                  Common.coinomeMap.put("BCH", thisPrice);
                  break;
                case "LTC/INR":
                  System.out.println("LTC == " + ltcBase * thisPrice);
                  totalCash += ltcBase * thisPrice;
                  Common.coinomeMap.put("LTC", thisPrice);
                  break;
              }

            }

            Common.isCoinomeDone = true;
            System.out.println("Total cash == " + totalCash);
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