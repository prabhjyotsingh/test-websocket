package coinome;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.IncompleteException;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.exceptions.LimitExedeedException;
import org.java_websocket.exceptions.NotSendableException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.BinaryFrame;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.TextFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.util.Base64;
import org.java_websocket.util.Charsetfunctions;

public class MyDraft extends Draft {

  /**
   * Attribute for the used extension in this draft
   */
  private IExtension extension = new DefaultExtension();

  /**
   * Attribute for all available extension in this draft
   */
  private List<IExtension> knownExtensions;

  /**
   * Attribute for the used protocol in this draft
   */
  private IProtocol protocol;

  /**
   * Attribute for all available protocols in this draft
   */
  private List<IProtocol> knownProtocols;

  /**
   * Attribute for the current continuous frame
   */
  private Framedata current_continuous_frame;

  /**
   * Attribute for the payload of the current continuous frame
   */
  private List<ByteBuffer> byteBufferList;

  /**
   * Attribute for the current incomplete frame
   */
  private ByteBuffer incompleteframe;

  /**
   * Attribute for the reusable random instance
   */
  private final Random reuseableRandom = new Random();

  /**
   * Constructor for the websocket protocol specified by RFC 6455 with default extensions
   *
   * @since 1.3.5
   */
  public MyDraft() {
    this(Collections.<IExtension>emptyList());
  }

  /**
   * Constructor for the websocket protocol specified by RFC 6455 with custom extensions
   *
   * @param inputExtension the extension which should be used for this draft
   * @since 1.3.5
   */
  public MyDraft(IExtension inputExtension) {
    this(Collections.singletonList(inputExtension));
  }

  /**
   * Constructor for the websocket protocol specified by RFC 6455 with custom extensions
   *
   * @param inputExtensions the extensions which should be used for this draft
   * @since 1.3.5
   */
  public MyDraft(List<IExtension> inputExtensions) {
    this(inputExtensions,
        Collections.<IProtocol>singletonList(new Protocol("actioncable-v1-json")));
  }

  /**
   * Constructor for the websocket protocol specified by RFC 6455 with custom extensions and
   * protocols
   *
   * @param inputExtensions the extensions which should be used for this draft
   * @param inputProtocols the protocols which should be used for this draft
   * @since 1.3.7
   */
  public MyDraft(List<IExtension> inputExtensions, List<IProtocol> inputProtocols) {
    if (inputExtensions == null || inputProtocols == null) {
      throw new IllegalArgumentException();
    }
    knownExtensions = new ArrayList<IExtension>(inputExtensions.size());
    knownProtocols = new ArrayList<IProtocol>(inputProtocols.size());
    boolean hasDefault = false;
    byteBufferList = new ArrayList<ByteBuffer>();
    for (IExtension inputExtension : inputExtensions) {
      if (inputExtension.getClass().equals(DefaultExtension.class)) {
        hasDefault = true;
      }
    }
    knownExtensions.addAll(inputExtensions);
    //We always add the DefaultExtension to implement the normal RFC 6455 specification
    if (!hasDefault) {
      knownExtensions.add(this.knownExtensions.size(), extension);
    }
    knownProtocols.addAll(inputProtocols);
  }

  int readVersion(Handshakedata handshakedata) {
    String vers = handshakedata.getFieldValue("Sec-WebSocket-Version");
    if (vers.length() > 0) {
      int v;
      try {
        v = new Integer(vers.trim());
        return v;
      } catch (NumberFormatException e) {
        return -1;
      }
    }
    return -1;
  }

  @Override
  public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata)
      throws InvalidHandshakeException {
    int v = readVersion(handshakedata);
    if (v != 13) {
      return HandshakeState.NOT_MATCHED;
    }
    HandshakeState extensionState = HandshakeState.NOT_MATCHED;
    String requestedExtension = handshakedata.getFieldValue("Sec-WebSocket-Extensions");
    for (IExtension knownExtension : knownExtensions) {
      if (knownExtension.acceptProvidedExtensionAsServer(requestedExtension)) {
        extension = knownExtension;
        extensionState = HandshakeState.MATCHED;
        break;
      }
    }
    HandshakeState protocolState = HandshakeState.NOT_MATCHED;
    String requestedProtocol = handshakedata.getFieldValue("Sec-WebSocket-Protocol");
    for (IProtocol knownProtocol : knownProtocols) {
      if (knownProtocol.acceptProvidedProtocol(requestedProtocol)) {
        protocol = knownProtocol;
        protocolState = HandshakeState.MATCHED;
        break;
      }
    }
    if (protocolState == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED) {
      return HandshakeState.MATCHED;
    }
    return HandshakeState.NOT_MATCHED;
  }

  @Override
  public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response)
      throws
      InvalidHandshakeException {
    if (!basicAccept(response)) {
      return HandshakeState.NOT_MATCHED;
    }
    if (!request.hasFieldValue("Sec-WebSocket-Key") || !response
        .hasFieldValue("Sec-WebSocket-Accept")) {
      return HandshakeState.NOT_MATCHED;
    }

    String seckey_answere = response.getFieldValue("Sec-WebSocket-Accept");
    String seckey_challenge = request.getFieldValue("Sec-WebSocket-Key");
    seckey_challenge = generateFinalKey(seckey_challenge);

    if (!seckey_challenge.equals(seckey_answere)) {
      return HandshakeState.NOT_MATCHED;
    }

    HandshakeState extensionState = HandshakeState.NOT_MATCHED;
    String requestedExtension = response.getFieldValue("Sec-WebSocket-Extensions");
    for (IExtension knownExtension : knownExtensions) {
      if (knownExtension.acceptProvidedExtensionAsClient(requestedExtension)) {
        extension = knownExtension;
        extensionState = HandshakeState.MATCHED;
        break;
      }
    }
    HandshakeState protocolState = HandshakeState.NOT_MATCHED;
    String requestedProtocol = response.getFieldValue("Sec-WebSocket-Protocol");
    for (IProtocol knownProtocol : knownProtocols) {
      if (knownProtocol.acceptProvidedProtocol(requestedProtocol)) {
        protocol = knownProtocol;
        protocolState = HandshakeState.MATCHED;
        break;
      }
    }
    if (protocolState == HandshakeState.MATCHED && extensionState == HandshakeState.MATCHED) {
      return HandshakeState.MATCHED;
    }
    return HandshakeState.NOT_MATCHED;
  }

  /**
   * Getter for the extension which is used by this draft
   *
   * @return the extension which is used or null, if handshake is not yet done
   */
  public IExtension getExtension() {
    return extension;
  }

  /**
   * Getter for all available extensions for this draft
   *
   * @return the extensions which are enabled for this draft
   */
  public List<IExtension> getKnownExtensions() {
    return knownExtensions;
  }

  /**
   * Getter for the protocol which is used by this draft
   *
   * @return the protocol which is used or null, if handshake is not yet done or no valid protocols
   * @since 1.3.7
   */
  public IProtocol getProtocol() {
    return protocol;
  }

  /**
   * Getter for all available protocols for this draft
   *
   * @return the protocols which are enabled for this draft
   * @since 1.3.7
   */
  public List<IProtocol> getKnownProtocols() {
    return knownProtocols;
  }

  @Override
  public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(
      ClientHandshakeBuilder request) {
    return request;
  }

  @Override
  public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request,
      ServerHandshakeBuilder
          response) throws InvalidHandshakeException {
    return response;
  }

  @Override
  public Draft copyInstance() {
    ArrayList<IExtension> newExtensions = new ArrayList<IExtension>();
    for (IExtension extension : getKnownExtensions()) {
      newExtensions.add(extension.copyInstance());
    }
    ArrayList<IProtocol> newProtocols = new ArrayList<IProtocol>();
    for (IProtocol protocol : getKnownProtocols()) {
      newProtocols.add(protocol.copyInstance());
    }
    return new MyDraft(newExtensions, newProtocols);
  }

  @Override
  public ByteBuffer createBinaryFrame(Framedata framedata) {
    getExtension().encodeFrame(framedata);
    if (WebSocketImpl.DEBUG) {
      System.out.println(
          "afterEnconding(" + framedata.getPayloadData().remaining() + "): {" + (framedata
              .getPayloadData().remaining() > 1000 ? "too big to display"
              : new String(framedata.getPayloadData().array()))
              + '}');
    }
    return createByteBufferFromFramedata(framedata);
  }

  private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
    ByteBuffer mes = framedata.getPayloadData();
    boolean mask = role == WebSocket.Role.CLIENT; // framedata.getTransfereMasked();
    int sizebytes = mes.remaining() <= 125 ? 1 : mes.remaining() <= 65535 ? 2 : 8;
    ByteBuffer buf = ByteBuffer
        .allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes
            .remaining());
    byte optcode = fromOpcode(framedata.getOpcode());
    byte one = (byte) (framedata.isFin() ? -128 : 0);
    one |= optcode;
    buf.put(one);
    byte[] payloadlengthbytes = toByteArray(mes.remaining(), sizebytes);
    assert (payloadlengthbytes.length == sizebytes);

    if (sizebytes == 1) {
      buf.put((byte) (payloadlengthbytes[0] | (mask ? (byte) -128 : 0)));
    } else if (sizebytes == 2) {
      buf.put((byte) ((byte) 126 | (mask ? (byte) -128 : 0)));
      buf.put(payloadlengthbytes);
    } else if (sizebytes == 8) {
      buf.put((byte) ((byte) 127 | (mask ? (byte) -128 : 0)));
      buf.put(payloadlengthbytes);
    } else {
      throw new RuntimeException("Size representation not supported/specified");
    }

    if (mask) {
      ByteBuffer maskkey = ByteBuffer.allocate(4);
      maskkey.putInt(reuseableRandom.nextInt());
      buf.put(maskkey.array());
      for (int i = 0; mes.hasRemaining(); i++) {
        buf.put((byte) (mes.get() ^ maskkey.get(i % 4)));
      }
    } else {
      buf.put(mes);
      //Reset the position of the bytebuffer e.g. for additional use
      mes.flip();
    }
    assert (buf.remaining() == 0) : buf.remaining();
    buf.flip();
    return buf;
  }

  public Framedata translateSingleFrame(ByteBuffer buffer)
      throws IncompleteException, InvalidDataException {
    int maxpacketsize = buffer.remaining();
    int realpacketsize = 2;
    if (maxpacketsize < realpacketsize) {
      throw new IncompleteException(realpacketsize);
    }
    byte b1 = buffer.get( /*0*/);
    boolean FIN = b1 >> 8 != 0;
    boolean rsv1 = false;
    boolean rsv2 = false;
    boolean rsv3 = false;
    if ((b1 & 0x40) != 0) {
      rsv1 = true;
    }
    if ((b1 & 0x20) != 0) {
      rsv2 = true;
    }
    if ((b1 & 0x10) != 0) {
      rsv3 = true;
    }
    byte b2 = buffer.get( /*1*/);
    boolean MASK = (b2 & -128) != 0;
    int payloadlength = (byte) (b2 & ~(byte) 128);
    Framedata.Opcode optcode = toOpcode((byte) (b1 & 15));

    if (!(payloadlength >= 0 && payloadlength <= 125)) {
      if (optcode == Framedata.Opcode.PING || optcode == Framedata.Opcode.PONG
          || optcode == Framedata.Opcode
          .CLOSING) {
        throw new InvalidFrameException("more than 125 octets");
      }
      if (payloadlength == 126) {
        realpacketsize += 2; // additional length bytes
        if (maxpacketsize < realpacketsize) {
          throw new IncompleteException(realpacketsize);
        }
        byte[] sizebytes = new byte[3];
        sizebytes[1] = buffer.get( /*1 + 1*/);
        sizebytes[2] = buffer.get( /*1 + 2*/);
        payloadlength = new BigInteger(sizebytes).intValue();
      } else {
        realpacketsize += 8; // additional length bytes
        if (maxpacketsize < realpacketsize) {
          throw new IncompleteException(realpacketsize);
        }
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
          bytes[i] = buffer.get( /*1 + i*/);
        }
        long length = new BigInteger(bytes).longValue();
        if (length > Integer.MAX_VALUE) {
          throw new LimitExedeedException("Payloadsize is to big...");
        } else {
          payloadlength = (int) length;
        }
      }
    }

    // int maskskeystart = foff + realpacketsize;
    realpacketsize += (MASK ? 4 : 0);
    // int payloadstart = foff + realpacketsize;
    realpacketsize += payloadlength;

    if (maxpacketsize < realpacketsize) {
      throw new IncompleteException(realpacketsize);
    }

    ByteBuffer payload = ByteBuffer.allocate(checkAlloc(payloadlength));
    if (MASK) {
      byte[] maskskey = new byte[4];
      buffer.get(maskskey);
      for (int i = 0; i < payloadlength; i++) {
        payload.put((byte) (buffer.get( /*payloadstart + i*/) ^ maskskey[i % 4]));
      }
    } else {
      payload.put(buffer.array(), buffer.position(), payload.limit());
      buffer.position(buffer.position() + payload.limit());
    }

    FramedataImpl1 frame = FramedataImpl1.get(optcode);
    frame.setFin(FIN);
    frame.setRSV1(rsv1);
    frame.setRSV2(rsv2);
    frame.setRSV3(rsv3);
    payload.flip();
    frame.setPayload(payload);
    getExtension().isFrameValid(frame);
    getExtension().decodeFrame(frame);
    if (WebSocketImpl.DEBUG) {
      System.out.println(
          "afterDecoding(" + frame.getPayloadData().remaining() + "): {" + (frame.getPayloadData()
              .remaining() > 1000 ? "too big to display"
              : new String(frame.getPayloadData().array())) + '}');
    }
    frame.isValid();
    return frame;
  }


  @Override
  public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
    while (true) {
      List<Framedata> frames = new LinkedList<Framedata>();
      Framedata cur;
      if (incompleteframe != null) {
        // complete an incomplete frame
        try {
          buffer.mark();
          int available_next_byte_count = buffer.remaining();// The number of bytes received
          int expected_next_byte_count = incompleteframe
              .remaining();// The number of bytes to complete the
          // incomplete frame

          if (expected_next_byte_count > available_next_byte_count) {
            // did not receive enough bytes to complete the frame
            incompleteframe.put(buffer.array(), buffer.position(), available_next_byte_count);
            buffer.position(buffer.position() + available_next_byte_count);
            return Collections.emptyList();
          }
          incompleteframe.put(buffer.array(), buffer.position(), expected_next_byte_count);
          buffer.position(buffer.position() + expected_next_byte_count);
          cur = translateSingleFrame((ByteBuffer) incompleteframe.duplicate().position(0));
          frames.add(cur);
          incompleteframe = null;
        } catch (IncompleteException e) {
          // extending as much as suggested
          ByteBuffer extendedframe = ByteBuffer.allocate(checkAlloc(e.getPreferredSize()));
          assert (extendedframe.limit() > incompleteframe.limit());
          incompleteframe.rewind();
          extendedframe.put(incompleteframe);
          incompleteframe = extendedframe;
          continue;
        }
      }

      while (buffer.hasRemaining()) {// Read as much as possible full frames
        buffer.mark();
        try {
          cur = translateSingleFrame(buffer);
          frames.add(cur);
        } catch (IncompleteException e) {
          // remember the incomplete data
          buffer.reset();
          int pref = e.getPreferredSize();
          incompleteframe = ByteBuffer.allocate(checkAlloc(pref));
          incompleteframe.put(buffer);
          break;
        }
      }
      return frames;
    }
  }

  @Override
  public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
    BinaryFrame curframe = new BinaryFrame();
    curframe.setPayload(binary);
    curframe.setTransferemasked(mask);
    try {
      curframe.isValid();
    } catch (InvalidDataException e) {
      throw new NotSendableException(e);
    }
    return Collections.singletonList((Framedata) curframe);
  }

  @Override
  public List<Framedata> createFrames(String text, boolean mask) {
    TextFrame curframe = new TextFrame();
    curframe.setPayload(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(text)));
    curframe.setTransferemasked(mask);
    try {
      curframe.isValid();
    } catch (InvalidDataException e) {
      throw new NotSendableException(e);
    }
    return Collections.singletonList((Framedata) curframe);
  }

  @Override
  public void reset() {
    incompleteframe = null;
    if (extension != null) {
      extension.reset();
    }
    extension = new DefaultExtension();
    protocol = null;
  }

  /**
   * Generate a date for for the date-header
   *
   * @return the server time
   */
  private String getServerTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(calendar.getTime());
  }

  /**
   * Generate a final key from a input string
   *
   * @param in the input string
   * @return a final key
   */
  private String generateFinalKey(String in) {
    String seckey = in.trim();
    String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    MessageDigest sh1;
    try {
      sh1 = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    return Base64.encodeBytes(sh1.digest(acc.getBytes()));
  }

  private byte[] toByteArray(long val, int bytecount) {
    byte[] buffer = new byte[bytecount];
    int highest = 8 * bytecount - 8;
    for (int i = 0; i < bytecount; i++) {
      buffer[i] = (byte) (val >>> (highest - 8 * i));
    }
    return buffer;
  }


  private byte fromOpcode(Framedata.Opcode opcode) {
    if (opcode == Framedata.Opcode.CONTINUOUS) {
      return 0;
    } else if (opcode == Framedata.Opcode.TEXT) {
      return 1;
    } else if (opcode == Framedata.Opcode.BINARY) {
      return 2;
    } else if (opcode == Framedata.Opcode.CLOSING) {
      return 8;
    } else if (opcode == Framedata.Opcode.PING) {
      return 9;
    } else if (opcode == Framedata.Opcode.PONG) {
      return 10;
    }
    throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
  }


  private Framedata.Opcode toOpcode(byte opcode) throws InvalidFrameException {
    switch (opcode) {
      case 0:
        return Framedata.Opcode.CONTINUOUS;
      case 1:
        return Framedata.Opcode.TEXT;
      case 2:
        return Framedata.Opcode.BINARY;
      // 3-7 are not yet defined
      case 8:
        return Framedata.Opcode.CLOSING;
      case 9:
        return Framedata.Opcode.PING;
      case 10:
        return Framedata.Opcode.PONG;
      // 11-15 are not yet defined
      default:
        throw new InvalidFrameException("Unknown opcode " + (short) opcode);
    }
  }

  @Override
  public void processFrame(WebSocketImpl webSocketImpl, Framedata frame)
      throws InvalidDataException {
    Framedata.Opcode curop = frame.getOpcode();
    if (curop == Framedata.Opcode.CLOSING) {
      int code = CloseFrame.NOCODE;
      String reason = "";
      if (frame instanceof CloseFrame) {
        CloseFrame cf = (CloseFrame) frame;
        code = cf.getCloseCode();
        reason = cf.getMessage();
      }
      if (webSocketImpl.getReadyState() == WebSocket.READYSTATE.CLOSING) {
        // complete the close handshake by disconnecting
        webSocketImpl.closeConnection(code, reason, true);
      } else {
        // echo close handshake
        if (getCloseHandshakeType() == CloseHandshakeType.TWOWAY) {
          webSocketImpl.close(code, reason, true);
        } else {
          webSocketImpl.flushAndClose(code, reason, false);
        }
      }
    } else if (curop == Framedata.Opcode.PING) {
      webSocketImpl.getWebSocketListener().onWebsocketPing(webSocketImpl, frame);
    } else if (curop == Framedata.Opcode.PONG) {
      webSocketImpl.updateLastPong();
      webSocketImpl.getWebSocketListener().onWebsocketPong(webSocketImpl, frame);
    } else if (!frame.isFin() || curop == Framedata.Opcode.CONTINUOUS) {
      if (curop != Framedata.Opcode.CONTINUOUS) {
        if (current_continuous_frame != null) {
          throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
              "Previous continuous frame sequence not " +
                  "completed" +
                  ".");
        }
        current_continuous_frame = frame;
        byteBufferList.add(frame.getPayloadData());
      } else if (frame.isFin()) {
        if (current_continuous_frame == null) {
          throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
              "Continuous frame sequence was not started.");
        }
        byteBufferList.add(frame.getPayloadData());
        if (current_continuous_frame.getOpcode() == Framedata.Opcode.TEXT) {
          ((FramedataImpl1) current_continuous_frame).setPayload(getPayloadFromByteBufferList());
          ((FramedataImpl1) current_continuous_frame).isValid();
          try {
            webSocketImpl.getWebSocketListener()
                .onWebsocketMessage(webSocketImpl, Charsetfunctions.stringUtf8
                    (current_continuous_frame.getPayloadData()));
          } catch (RuntimeException e) {
            webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
          }
        } else if (current_continuous_frame.getOpcode() == Framedata.Opcode.BINARY) {
          ((FramedataImpl1) current_continuous_frame).setPayload(getPayloadFromByteBufferList());
          ((FramedataImpl1) current_continuous_frame).isValid();
          try {
            webSocketImpl.getWebSocketListener()
                .onWebsocketMessage(webSocketImpl, current_continuous_frame
                    .getPayloadData());
          } catch (RuntimeException e) {
            webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
          }
        }
        current_continuous_frame = null;
        byteBufferList.clear();
      } else if (current_continuous_frame == null) {
        throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
            "Continuous frame sequence was not started.");
      }
      //Check if the whole payload is valid utf8, when the opcode indicates a text
      if (curop == Framedata.Opcode.TEXT) {
        if (!Charsetfunctions.isValidUTF8(frame.getPayloadData())) {
          throw new InvalidDataException(CloseFrame.NO_UTF8);
        }
      }
      //Checking if the current continous frame contains a correct payload with the other frames combined
      if (curop == Framedata.Opcode.CONTINUOUS && current_continuous_frame != null) {
        byteBufferList.add(frame.getPayloadData());
      }
    } else if (current_continuous_frame != null) {
      throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
          "Continuous frame sequence not completed.");
    } else if (curop == Framedata.Opcode.TEXT) {
      try {
        webSocketImpl.getWebSocketListener()
            .onWebsocketMessage(webSocketImpl, Charsetfunctions.stringUtf8(frame
                .getPayloadData()));
      } catch (RuntimeException e) {
        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
      }
    } else if (curop == Framedata.Opcode.BINARY) {
      try {
        webSocketImpl.getWebSocketListener()
            .onWebsocketMessage(webSocketImpl, frame.getPayloadData());
      } catch (RuntimeException e) {
        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
      }
    } else {
      throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR,
          "non control or continious frame expected");
    }
  }

  @Override
  public Draft.CloseHandshakeType getCloseHandshakeType() {
    return Draft.CloseHandshakeType.TWOWAY;
  }

  @Override
  public String toString() {
    String result = super.toString();
    if (getExtension() != null) {
      result += " extension: " + getExtension().toString();
    }
    if (getProtocol() != null) {
      result += " protocol: " + getProtocol().toString();
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MyDraft that = (MyDraft) o;

    if (extension != null ? !extension.equals(that.extension) : that.extension != null) {
      return false;
    }
    return protocol != null ? protocol.equals(that.protocol) : that.protocol == null;
  }

  @Override
  public int hashCode() {
    int result = extension != null ? extension.hashCode() : 0;
    result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
    return result;
  }

  /**
   * Method to generate a full bytebuffer out of all the fragmented frame payload
   *
   * @return a bytebuffer containing all the data
   * @throws LimitExedeedException will be thrown when the totalSize is bigger then
   * Integer.MAX_VALUE due to not being able to allocate more
   */
  private ByteBuffer getPayloadFromByteBufferList() throws LimitExedeedException {
    long totalSize = 0;
    for (ByteBuffer buffer : byteBufferList) {
      totalSize += buffer.limit();
    }
    if (totalSize > Integer.MAX_VALUE) {
      throw new LimitExedeedException("Payloadsize is to big...");
    }
    ByteBuffer resultingByteBuffer = ByteBuffer.allocate((int) totalSize);
    for (ByteBuffer buffer : byteBufferList) {
      resultingByteBuffer.put(buffer);
    }
    resultingByteBuffer.flip();
    return resultingByteBuffer;
  }
}
