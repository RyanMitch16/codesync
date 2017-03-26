import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PerspectiveSender {

  private final String ip;
  private final int receivingPort;
  private final int targetPort;
  private final Path projectPath;

  private ServerSocket inputSocket;
  private Socket outputSocket;

  /** */
  public PerspectiveSender(String ip, int receivingPort, int targetPort, Path projectPath) {
    this.ip = ip;
    this.receivingPort = receivingPort;
    this.targetPort = targetPort;
    this.projectPath = projectPath;
  }

  /** */
  public void openReceiver(String username) throws IOException {
    System.out.println("Requesting connection");
    outputSocket = new Socket(ip, targetPort);

    byte[] requestbytes = ("ACCESS:" + username).getBytes();
    outputSocket.getOutputStream().write(requestbytes.length);
    outputSocket.getOutputStream().write(requestbytes);

    //Socket inSocket = outputSocket.getInputStream();
    InputStream is = outputSocket.getInputStream();
    if (is.read() == 1) {
      System.out.println("Connection accepted");
      while (is.read() == 1) {
        receiveFile(is);
      }
      System.out.println("Files transferred");
    } else {
      System.out.println("Connection refused");
    }
    //inSocket.close();

    inputSocket = new ServerSocket(9999);
    while (true) {
      Socket inSocket = inputSocket.accept();
      InputStream inputStream = inSocket.getInputStream();
      int size = inputStream.read();
      byte[] buffer = new byte[size];
      inputStream.read(buffer, 0, size);
      String request = new String(buffer);

      if (request.startsWith("UPDATE_OTHERS:")) {
        outputSocket.getOutputStream().write(size);
        outputSocket.getOutputStream().write("UPDATE_SELF:".getBytes());

        // Retrieve the file name of the file
        int fileNameSize = inputStream.read();
        byte[] fileNameBuffer = new byte[fileNameSize];
        inputStream.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);
        String relativePath = fileName.substring(projectPath.toAbsolutePath().toString().length() + 1);

        outputSocket.getOutputStream().write(relativePath.getBytes().length);
        outputSocket.getOutputStream().write(relativePath.getBytes());

        int contentsSize = inputStream.read();
        byte[] contentsBuffers = new byte[contentsSize];
        inputStream.read(contentsBuffers, 0, contentsSize);

        outputSocket.getOutputStream().write(contentsBuffers);
      } else if (request.startsWith("UPDATE_SELF:")) {
        System.out.println("Updates found");

        // Retrieve the file name of the file
        int fileNameSize = inputStream.read();
        byte[] fileNameBuffer = new byte[fileNameSize];
        inputStream.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);

        File file = new File(projectPath.toAbsolutePath().toString(), fileName);
        int contentsSize = inputStream.read();
        byte[] contentsBuffers = new byte[contentsSize];
        inputStream.read(contentsBuffers, 0, contentsSize);

        FileOutputStream fooStream = new FileOutputStream(file, false);
        fooStream.write(contentsBuffers);
        fooStream.close();
      }
      inSocket.close();

    }
  }

  /** */
  public void openHost() throws IOException {

    inputSocket = new ServerSocket(receivingPort);
    //outputSocket = new Socket(ip, targetPort);

    while (true) {
      Socket inSocket = inputSocket.accept();
      InputStream inputStream = inSocket.getInputStream();
      int size = inputStream.read();
      byte[] buffer = new byte[size];
      inputStream.read(buffer, 0, size);
      String request = new String(buffer);

      if (request.startsWith("ACCESS:")) {
        System.out.println("Would you like to allow access to "
            + request.substring(request.indexOf(":") + 1) +"? (y/n)");
        Scanner scanner = new Scanner(System.in);
        if (scanner.next().toLowerCase().equals("y")) {
          System.out.println("Access granted");
          inSocket.getOutputStream().write(1);
          sendAllFiles(new File(projectPath.toString()), inSocket.getOutputStream());
          inSocket.getOutputStream().write(0);
          System.out.println("Done");
        } else {
          System.out.println("Access denied");
          inSocket.getOutputStream().write(0);
        }
      } else if (request.startsWith("UPDATE_OTHERS:")) {
        inSocket.getOutputStream().write(size);
        inSocket.getOutputStream().write("UPDATE_SELF:".getBytes());

        // Retrieve the file name of the file
        int fileNameSize = inputStream.read();
        byte[] fileNameBuffer = new byte[fileNameSize];
        inputStream.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);
        String relativePath = fileName.substring(projectPath.toAbsolutePath().toString().length() + 1);

        inSocket.getOutputStream().write(relativePath.getBytes().length);
        inSocket.getOutputStream().write(relativePath.getBytes());

        int contentsSize = inputStream.read();
        byte[] contentsBuffers = new byte[contentsSize];
        inputStream.read(contentsBuffers, 0, contentsSize);

        inSocket.getOutputStream().write(contentsBuffers);
      } else if (request.startsWith("UPDATE_SELF:")) {
        System.out.println("Updates found");

        // Retrieve the file name of the file
        int fileNameSize = inputStream.read();
        byte[] fileNameBuffer = new byte[fileNameSize];
        inputStream.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);

        File file = new File(projectPath.toAbsolutePath().toString(), fileName);
        int contentsSize = inputStream.read();
        byte[] contentsBuffers = new byte[contentsSize];
        inputStream.read(contentsBuffers, 0, contentsSize);

        FileOutputStream fooStream = new FileOutputStream(file, false);
        fooStream.write(contentsBuffers);
        fooStream.close();
      }
      inSocket.close();
    }
  }

  /** */
  public void close() throws IOException {
    inputSocket.close();
    outputSocket.close();
  }

  /** */
  private void receiveFile(InputStream inputStream) throws IOException {

    // Retrieve the file name of the file
    int fileNameSize = inputStream.read();
    byte[] fileNameBuffer = new byte[fileNameSize];
    inputStream.read(fileNameBuffer, 0, fileNameSize);
    String fileName = new String(fileNameBuffer);

    // Write the file to the location in the perspective directory
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
        new File(projectPath.toAbsolutePath().toString(), fileName)));
    int bytesTotal = inputStream.read();
    byte[] buffer = new byte[bytesTotal];
    inputStream.read(buffer, 0, bytesTotal);
    bos.write(buffer, 0, bytesTotal);
    bos.close();
  }

  /** Writes the specified file to the {@link OutputStream}. */
  private void sendFile(File file, OutputStream outputStream) throws IOException {
    byte[] buffer = new byte[(int) file.length()];

    // Write the relative path of the file to the output stream
    String relativePath = file.getAbsolutePath().substring(projectPath.toAbsolutePath().toString().length() + 1);
    byte[] fileNameBytes = relativePath.getBytes();
    outputStream.write(fileNameBytes.length);
    outputStream.write(fileNameBytes);

    // Write the file to the output stream
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
    bis.read(buffer, 0, buffer.length);
    outputStream.write(buffer.length);
    outputStream.write(buffer, 0, buffer.length);

    //
    outputStream.flush();
  }

  private void sendAllFiles(File path, OutputStream outputStream) throws IOException {
    for (File file : path.listFiles()) {
      if (file.isDirectory()) {
        //sendAllFiles(file, outputStream);
      } else {
        outputStream.write(1);
        System.out.println("Sent 1");
        sendFile(file, outputStream);
      }
    }
  }

  public static void main(String[] args){

    try {

      if (args[0].equals("r")) {
        PerspectiveSender perspectiveSender = new PerspectiveSender("10.122.1.61", 8765, 8766,
            Paths.get("C:"));
        perspectiveSender.openReceiver("Ryan");

      } else {
        PerspectiveSender perspectiveSender = new PerspectiveSender("10.122.212.221", 8766, 8765,
            Paths.get("/Users/ryanmitchell/Desktop/projects/codesync/syncer/"));
        perspectiveSender.openHost();
      }
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }
}
