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
    DataOutputStream rdos = new DataOutputStream(outputSocket.getOutputStream());
    rdos.writeInt(requestbytes.length);
    rdos.write(requestbytes);

    //Socket inSocket = outputSocket.getInputStream();
    DataInputStream is = new DataInputStream(outputSocket.getInputStream());
    if (is.readByte() == 1) {
      System.out.println("Connection accepted");
      while (is.readByte() == 1) {
        System.out.println("Transfering");
        receiveFile(is);
      }
      System.out.println("Files transferred");
    } else {
      System.out.println("Connection refused");
    }
    //outputSocket.close();
    //outputSocket = new Socket(ip, targetPort);
    //inSocket.close();

    inputSocket = new ServerSocket(9999);
    while (true) {
      Socket inSocket = inputSocket.accept();
      DataInputStream dis = new DataInputStream(inSocket.getInputStream());
      DataOutputStream dos = new DataOutputStream(outputSocket.getOutputStream());
      int size = dis.readInt();

      byte[] buffer = new byte[size];
      dis.read(buffer, 0, size);
      String request = new String(buffer);

      System.out.println(request);

      if (request.startsWith("UPDATE_OTHERS:")) {

        dos.writeInt("UPDATE_SELF:".getBytes().length);
        dos.write("UPDATE_SELF:".getBytes());

        // Retrieve the file name of the file
        int fileNameSize = dis.readInt();
        byte[] fileNameBuffer = new byte[fileNameSize];
        dis.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);

        String relativePath = fileName.substring(projectPath.toAbsolutePath().toString().length() + 1);

        dos.writeInt(relativePath.getBytes().length);
        dos.write(relativePath.getBytes());

        int contentsSize = dis.readInt();
        byte[] contentsBuffers = new byte[contentsSize];
        dis.read(contentsBuffers);

        dos.write(contentsBuffers);
        dos.flush();


      } else if (request.startsWith("UPDATE_SELF:")) {
        System.out.println("Updates found");

        // Retrieve the file name of the file
        int fileNameSize = dis.readInt();

        byte[] fileNameBuffer = new byte[fileNameSize];
        dis.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);

        File file = new File(projectPath.toAbsolutePath().toString(), fileName);
        System.out.println(file.getAbsolutePath());
        int contentsSize = dis.readInt();
        byte[] contentsBuffers = new byte[contentsSize];
        dis.read(contentsBuffers, 0, contentsSize);

        FileOutputStream fooStream = new FileOutputStream(file, false);
        fooStream.write(contentsBuffers);
        fooStream.close();
      }
      //inSocket.close();

    }
  }

  /** */
  public void openHost() throws IOException {

    inputSocket = new ServerSocket(receivingPort);
    //outputSocket = new Socket(ip, targetPort);

    while (true) {
      Socket inSocket = inputSocket.accept();
      DataInputStream dis = new DataInputStream(inSocket.getInputStream());
      DataOutputStream dos = new DataOutputStream(inSocket.getOutputStream());
      System.out.print("MMMM");
      int size = dis.readInt();
      byte[] buffer = new byte[size];
      dis.read(buffer, 0, size);
      String request = new String(buffer);

      System.out.print(request);

      if (request.startsWith("ACCESS:")) {
        System.out.println("Would you like to allow access to "
            + request.substring(request.indexOf(":") + 1) +"? (y/n)");
        Scanner scanner = new Scanner(System.in);
        if (scanner.next().toLowerCase().equals("y")) {
          System.out.println("Access granted");
          dos.writeByte(1);
          sendAllFiles(new File(projectPath.toString()), dos);
          dos.writeByte(0);
          System.out.println("Done");
        } else {
          System.out.println("Access denied");
          dos.writeByte(0);
        }
      } else if (request.startsWith("UPDATE_OTHERS:")) {
        dos.writeInt(size);
        dos.write("UPDATE_SELF:".getBytes());

        // Retrieve the file name of the file
        int fileNameSize = dis.readInt();
        byte[] fileNameBuffer = new byte[fileNameSize];
        dis.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);
        String relativePath = fileName.substring(projectPath.toAbsolutePath().toString().length() + 1);

        dos.writeInt(relativePath.getBytes().length);
        dos.write(relativePath.getBytes());

        int contentsSize = dis.readInt();
        byte[] contentsBuffers = new byte[contentsSize];
        dis.read(contentsBuffers, 0, contentsSize);

        dos.write(contentsBuffers);
      } else if (request.startsWith("UPDATE_SELF:")) {
        System.out.println("Updates found");

        // Retrieve the file name of the file
        int fileNameSize = dis.readInt();
        byte[] fileNameBuffer = new byte[fileNameSize];
        dis.read(fileNameBuffer, 0, fileNameSize);
        String fileName = new String(fileNameBuffer);
        System.out.println(fileName);

        File file = new File(projectPath.toAbsolutePath().toString(), fileName);
        int contentsSize = dis.readInt();
        System.out.println(contentsSize);
        byte[] contentsBuffers = new byte[contentsSize];
        dis.read(contentsBuffers, 0, contentsSize);

        FileOutputStream fooStream = new FileOutputStream(file, false);
        fooStream.write(contentsBuffers);
        fooStream.close();
      }
      //inSocket.close();
    }
  }

  /** */
  public void close() throws IOException {
    inputSocket.close();
    outputSocket.close();
  }

  /** */
  private void receiveFile(DataInputStream inputStream) throws IOException {

    // Retrieve the file name of the file
    int fileNameSize = inputStream.readInt();
    byte[] fileNameBuffer = new byte[fileNameSize];
    inputStream.read(fileNameBuffer, 0, fileNameSize);
    String fileName = new String(fileNameBuffer);

    // Write the file to the location in the perspective directory
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
        new File(projectPath.toAbsolutePath().toString(), fileName)));
    int bytesTotal = inputStream.readInt();
    byte[] buffer = new byte[bytesTotal];
    inputStream.read(buffer, 0, bytesTotal);
    bos.write(buffer, 0, bytesTotal);
    bos.close();
  }

  /** Writes the specified file to the {@link OutputStream}. */
  private void sendFile(File file, DataOutputStream outputStream) throws IOException {
    byte[] buffer = new byte[(int) file.length()];

    // Write the relative path of the file to the output stream
    String relativePath = file.getAbsolutePath().substring(projectPath.toAbsolutePath().toString().length() + 1);
    byte[] fileNameBytes = relativePath.getBytes();
    outputStream.writeInt(fileNameBytes.length);
    outputStream.write(fileNameBytes);

    // Write the file to the output stream
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
    bis.read(buffer, 0, buffer.length);
    outputStream.writeInt(buffer.length);
    outputStream.write(buffer, 0, buffer.length);

    //
    outputStream.flush();
  }

  private void sendAllFiles(File path, DataOutputStream outputStream) throws IOException {
    for (File file : path.listFiles()) {
      if (file.isDirectory()) {
        //sendAllFiles(file, outputStream);
      } else {
        outputStream.writeByte(1);
        System.out.println("Sent 1");
        sendFile(file, outputStream);
      }
    }
  }

  public static void main(String[] args){

    try {

      if (args[0].equals("r")) {
        PerspectiveSender perspectiveSender = new PerspectiveSender("10.122.1.61", 8765, 8766,
            Paths.get("/home/chris/"));
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
