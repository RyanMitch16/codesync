import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PerspectiveSender {

  private final String ip;
  private final int inputPort;
  private final int outputPort;
  private final Path projectPath;

  private ServerSocket inputSocket;
  private Socket outputSocket;

  /** */
  public PerspectiveSender(String ip, int inputPort, int outputPort, Path projectPath) {
    this.ip = ip;
    this.inputPort = inputPort;
    this.outputPort = outputPort;
    this.projectPath = projectPath;
  }

  /** */
  public void open() throws IOException {
    inputSocket = new ServerSocket(inputPort);
    outputSocket = new Socket(ip, outputPort);
  }

  /** */
  public void close() {

  }

  private void recieveFile(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[1024];

    // Retrieve the file name of the file
    int fileNameSize = inputStream.read();
    byte[] fileNameBuffer = new byte[fileNameSize];
    inputStream.read(fileNameBuffer, 0, fileNameSize);
    String fileName = new String(fileNameBuffer);

    // Write the file to the location in the perspective directory
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
        new File(projectPath.toAbsolutePath().toString(), fileName)));
    int bytesRead = inputStream.read(fileNameBuffer, fileNameSize, fileNameBuffer.length);
    bos.write(fileNameBuffer, 0, bytesRead);
    bos.close();
  }

  /** Writes the specified file to the {@link OutputStream}. */
  private void sendFile(File file, OutputStream outputStream) throws IOException {
    byte[] buffer = new byte[(int) file.length()];

    // Write the relative path of the file to the output stream
    String relativePath = file.getAbsolutePath().substring(projectPath.toAbsolutePath().toString().length());
    System.out.println(relativePath);
    byte[] fileNameBytes = relativePath.getBytes();
    outputStream.write(fileNameBytes.length);
    outputStream.write(fileNameBytes);

    // Write the file to the output stream
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
    bis.read(buffer, 0, buffer.length);
    outputStream.write(buffer, 0, buffer.length);

    //
    outputStream.flush();
  }

  public static void main(String[] args) throws IOException {
    if (args[0].equals("r")) {
      PerspectiveSender perspectiveSender = new PerspectiveSender("10.122.70.134", 8765, 8766,
          Paths.get("C:/Users/hhajd/Documents/TARGET"));
      perspectiveSender.open();

      Socket sock = perspectiveSender.inputSocket.accept();
      perspectiveSender.recieveFile(sock.getInputStream());
      sock.close();

    } else {
      PerspectiveSender perspectiveSender = new PerspectiveSender("10.122.1.61", 8766, 8765,
          Paths.get("Users/ryanmitchell/Desktop/projects/codesync"));
      perspectiveSender.open();
      perspectiveSender.sendFile(new File("Hello.txt"), perspectiveSender.outputSocket.getOutputStream());
    }
  }
}
