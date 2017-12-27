package websocket;

import java.net.URISyntaxException;
import websocket.coinome.CoinomeWebsocket;
import websocket.koinex.KoinexWebsocket;

public class WebSocket {

  public static void main(String[] args) {
    CoinomeWebsocket cws = new CoinomeWebsocket();
    KoinexWebsocket kws = new KoinexWebsocket();
    try {
      cws.runSocket();
      kws.runSocket();
    } catch (URISyntaxException e) {

    }
  }
}
