import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PluginCommunicator {

  private ServerSocket inputSocket;
  private Socket outputSocket;



  PluginCommunicator(PerspectiveSender perspective) {

  }

  public void waitForChanges() throws IOException {
    Socket socket = inputSocket.accept();
  }

}
