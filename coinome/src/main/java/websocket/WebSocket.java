package websocket;

import websocket.coinome.CoinomeWebsocket;
import websocket.koinex.KoinexWebsocket;

import java.net.URISyntaxException;

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
