import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.Path;

public class PerspectiveConnection {

  /** */
  public enum Type {
    ORIGINATOR,
    OUTSIDER,
  }

  private final String ip;
  private final int port;
  private final Type type;

  private DatagramSocket socket;

  /** */
  public PerspectiveConnection(String ip, int port, Type userType) {
    this.ip = ip;
    this.port = port;
    this.type = userType;
    this.socket = null;
  }

  /** */
  public void open() throws IOException {
    socket = new DatagramSocket();
    if (type == Type.ORIGINATOR) {

    } else if (type == Type.OUTSIDER) {
      // Request the file contents of the user you are viewing
      socket.receive();
    }
  }

  /** */
  public void close() {

  }

  /** */
  private void sendFile(File file) {

  }

  /** */
  private void recieveFile() {

  }

}
