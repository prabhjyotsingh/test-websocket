package coinome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocket {

  static Boolean isInit = false;
  static WebSocketClient mWs = null;
  static HashMap<String, String> headers = new HashMap<String, String>();
  private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  static {
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "en-IN,en;q=0.9,en-GB;q=0.8,en-US;q=0.7,hi;q=0.6");
    headers.put("Cache-Control", "no-cache");
    headers.put("Connection", "Upgrade");
    headers.put("Cookie",
        "_ga=GA1.2.1005256831.1513318939; ext_name=jaehkpjddfdgiiefcnhahapilbejohhj; " +
            "intercom-id-xisrgs36=a77e522d-6ca2-4e8b-9823-92527cade0ba; intercom-lou-xisrgs36=1; " +
            "_gid=GA1.2.813183416.1513590431; user_id=34842; _session_id=b1aa788eb0b290b64e9ea568c411160f; "
            +
            "market=LTC%2FINR; " +
            "intercom-session-xisrgs36" +
            "=N2xibEg1UDdDdDB5MWtjd1Facm9lRjVQNE1ldzZRTWFiZC9SUTRTeGhXYlFEaUNoeTFNTXpLZ0l3eHFXQnJvRS0tQ2h5SmJvZElUVUc0TTNYNFN1Y3hTQT09--b41a6bcbfaef2bc1a047843f71d22fa62adf04b6");
    headers.put("DNT", "1");
    headers.put("Host", "www.coinome.com");
    headers.put("Origin", "https://www.coinome.com");
    headers.put("Pragma", "no-cache");
    headers.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
    headers.put("Sec-WebSocket-Key", "a6F8pvMnyi6pwZ9oCaloMQ==");
    headers.put("Sec-WebSocket-Protocol", "actioncable-v1-json, actioncable-unsupported");
    headers.put("Sec-WebSocket-Version", "13");
    headers.put("Upgrade", "websocket");
  }

  public static void main(String[] args) throws URISyntaxException {
    System.out.println();

    mWs = new WebSocketClient(new URI("wss://www.coinome.com/cable"), new MyDraft(), headers,
        2000) {
      @Override
      public void onMessage(String message) {
//        JSONObject obj = new JSONObject(message);
        Map mapMessage = gson.fromJson(message, Map.class);

        if (mapMessage.get("type") != null) {
          if (!mapMessage.get("type").equals("ping")) {
            System.out.println(mapMessage);
          }

        } else {
          ArrayList<ArrayList> marketRates = (ArrayList<ArrayList>) ((ArrayList) gson
              .fromJson(message, PublicChannelObject.class).getMessage().get(0)).get(1);
          System.out.println(mapMessage);

          Double btcBase = 0.00726415;
          Double ltcBase = 0.45525918;
          Double bchBase = 0.07341229;
          Double totalCash = 0.0;

          for (ArrayList marketRate : marketRates) {
            Double thisPrice = new Double((String) marketRate.get(1));
            switch ((String) marketRate.get(0)) {
              case "BTC/INR":
                System.out.println("BTC == " + btcBase * thisPrice);
                totalCash += btcBase * thisPrice;
                break;
              case "BCH/INR":
                System.out.println("BCH == " + bchBase * thisPrice);
                totalCash += bchBase * thisPrice;
                break;
              case "LTC/INR":
                System.out.println("LTC == " + ltcBase * thisPrice);
                totalCash += ltcBase * thisPrice;
                break;
            }

          }
          System.out.println("Total cash = " + totalCash);
          System.exit(0);
        }

        if (!isInit) {
          isInit = true;
          Map obj;
          obj = new HashMap();
          obj.put("command", "subscribe");
          obj.put("identifier", "{\"channel\":\"PublicChannel\"}");
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
      }

      @Override
      public void onError(Exception ex) {
        ex.printStackTrace();
      }

    };
    //open websocket
    mWs.connect();
  }
}
