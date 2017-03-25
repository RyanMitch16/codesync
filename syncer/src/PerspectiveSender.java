import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

public class PerspectiveSender {


  private final String ip;
  private final int port;
  private final Path projectPath;

  private Socket socket;

  /** */
  public PerspectiveSender(String ip, int port, Path projectPath) {
    this.ip = ip;
    this.port = port;
    this.projectPath = projectPath;
    this.socket = null;
  }

  /** */
  public void open() throws IOException {
    socket = new Socket(ip, port);
  }

  /** */
  public void close() {

  }

  /** */
  private void sendFile(File file) {

  }
}
