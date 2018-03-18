package websocket.binance;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;

public class BinanceCommon {

  public static WebSocket getWebSocket(String URI) throws IOException, WebSocketException {
    WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

    WebSocket ws = factory.createSocket(URI)
        .addHeader("Accept-Encoding", "gzip, deflate, br")
        .addHeader("Accept-Encoding", "gzip, deflate, br")
        .addHeader("Accept-Language", "en-IN,en;q=0.9,en-GB;q=0.8,en-US;q=0.7,hi;q=0.6")
        .addHeader("Cache-Control", "no-cache")
        .addHeader("Connection", "Upgrade")
        .addHeader("Cookie",
            "sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2216597876%22%2C%22%24device_id%22%3A"
                +
                "%22160985f088c169-016a5b3bf1b6d2-16386656-1296000-160985f088d9ad%22%2C%22props%22%3A%7B%22"
                +
                "%24latest_referrer" +
                "%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%7D%2C%22first_id%22%3A%22160985f088c169"
                +
                "-016a5b3bf1b6d2" +
                "-16386656-1296000-160985f088d9ad%22%7D; _ga=GA1.2.850452319.1515732699")
        .addHeader("DNT", "1")
        .addHeader("Host", "stream2.binance.com:9443")
        .addHeader("Origin", "https://www.binance.com")
        .addHeader("Pragma", "no-cache")
        .addHeader("Sec-WebSocket-Key", "Ow1r1nn/L/yzHuSnUM2mqw==")
        .addHeader("Sec-WebSocket-Version", "13")
        .addHeader("Upgrade", "websocket")
        .addHeader("User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) "
                +
                "Chrome/63.0.3239.132 " +
                "Safari/537.36")
        .connect();

    return ws;
  }
}
