package websocket.binance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import websocket.Common;

import java.net.URISyntaxException;
import java.util.Map;

public class BinanceBtcUsdtWebsocket {
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public void runSocket() throws URISyntaxException {
    try {
      final WebSocket ws = BinanceCommon.getWebSocket("wss://stream2.binance.com:9443/ws/btcusdt@kline_1w.b10");

      ws.addListener(new WebSocketAdapter() {
        @Override
        public void onTextMessage(WebSocket websocket, String message) throws Exception {
          Map mapMessage = gson.fromJson(message, Map.class);

          Double BTC_USDT = new Double((String) ((Map) mapMessage.get("k")).get("c"));
          Common.binanceMap.put("BTC_USDT", BTC_USDT);
        }

        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame
          clientCloseFrame, boolean closedByServer) throws Exception {
          ws.connect();
        }

      });
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}

