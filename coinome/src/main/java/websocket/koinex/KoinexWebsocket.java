package websocket.koinex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import websocket.Common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class KoinexWebsocket {


    Boolean isInit = false;
    WebSocketClient mWs = null;
    HashMap<String, String> headers = new HashMap<String, String>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public KoinexWebsocket() {

        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-IN,en;q=0.9,en-GB;q=0.8,en-US;q=0.7,hi;q=0.6");
        headers.put("Cache-Control", "no-cache");
        headers.put("Connection", "Upgrade");
        headers.put("DNT", "1");
        headers.put("Host", "ws-ap2.pusher.com");
        headers.put("Origin", "https://koinex.in");
        headers.put("Pragma", "no-cache");
        headers.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        headers.put("Sec-WebSocket-Key", "sYpzTo+bRppNm5mPLOykgw==");
        headers.put("Sec-WebSocket-Version", "13");
        headers.put("Upgrade", "websocket");
        headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/63.0.3239.84 Safari/537.36");

    }

    public void runSocket() throws URISyntaxException {
        mWs = new WebSocketClient(new URI("wss://ws-ap2.pusher" +
                ".com/app/9197b0bfdf3f71a4064e?protocol=7&client=js&version=4.1.0&flash=false"), new KoinexDraft(),
                headers,
                2000) {
            @Override
            public void onMessage(String message) {
                Map mapMessage = gson.fromJson(message, Map.class);

                if (!isInit) {
                    isInit = true;
                    Map obj = new HashMap();
                    obj.put("event", "pusher:subscribe");
                    Map data = new HashMap();
                    data.put("channel", "my-channel");
                    obj.put("data", data);
                    message = gson.toJson(obj);
                    mWs.send(message);
                } else {
                    if (mapMessage.get("event").equals("ticker")) {
                        Map data =
                                (Map) ((Map) gson.fromJson((String) mapMessage.get("data"), Map.class).get("message")
                                ).get("data");
                        Common.koinexMap.put("BTC", new Double((String) data.get("BTC")));
                        Common.koinexMap.put("BCH", new Double((String) data.get("BCH")));
                        Common.koinexMap.put("LTC", new Double((String) data.get("LTC")));
                        Common.isKoinexDone = true;
                    }
                }
                Common.initiateExit();
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

