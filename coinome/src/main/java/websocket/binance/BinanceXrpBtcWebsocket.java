package websocket.binance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import websocket.Common;

public class BinanceXrpBtcWebsocket {

  WebSocketClient mWs = null;
  HashMap<String, String> headers = new HashMap<String, String>();
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public BinanceXrpBtcWebsocket() {

    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "en-IN,en;q=0.9,en-GB;q=0.8,en-US;q=0.7,hi;q=0.6");
    headers.put("Cache-Control", "no-cache");
    headers.put("Connection", "Upgrade");
    headers.put("Cookie",
        "sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2216597876%22%2C%22%24device_id%22%3A%22160985f088c169-016a5b3bf1b6d2-16386656-1296000-160985f088d9ad%22%2C%22props%22%3A%7B%22%24latest_referrer%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%7D%2C%22first_id%22%3A%22160985f088c169-016a5b3bf1b6d2-16386656-1296000-160985f088d9ad%22%7D; _ga=GA1.2.850452319.1515732699");
    headers.put("DNT", "1");
    headers.put("Host", "stream2.binance.com:9443");
    headers.put("Origin", "https://www.binance.com");
    headers.put("Pragma", "no-cache");
    headers.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
    headers.put("Sec-WebSocket-Key", "Ow1r1nn/L/yzHuSnUM2mqw==");
    headers.put("Sec-WebSocket-Version", "13");
    headers.put("Upgrade", "websocket");
    headers.put("User-Agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");

  }

  public void runSocket() throws URISyntaxException {
    mWs = createWebSocket();
    mWs.connect();
  }

  private WebSocketClient createWebSocket() throws URISyntaxException {
    return new WebSocketClient(new URI("wss://stream2.binance.com:9443/ws/xrpbtc@kline_1w.b10"),
        new BinanceDraft(),
        headers,
        2000) {
      @Override
      public void onMessage(String message) {
        Map mapMessage = gson.fromJson(message, Map.class);

        Double XRP_BTC = new Double((String) ((Map) mapMessage.get("k")).get("c"));

        Common.binanceMap.put("XRP_BTC", XRP_BTC);
      }

      @Override
      public void onOpen(ServerHandshake handshake) {
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed connection");
        try {
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

