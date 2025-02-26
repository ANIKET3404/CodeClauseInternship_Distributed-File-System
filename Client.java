import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Example: Upload a file
            String command = "UPLOAD";
            String fileName = "example.txt";
            byte[] fileData = readFile(fileName);

            out.writeObject(command);
            out.writeObject(fileName);
            out.writeObject(fileData);
            String response = (String) in.readObject();
            System.out.println(response);

            // Example: Download a file
            command = "DOWNLOAD";
            out.writeObject(command);
            out.writeObject(fileName);
            byte[] downloadedData = (byte[]) in.readObject();
            writeFile("downloaded_" + fileName, downloadedData);
            System.out.println("File downloaded successfully.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        return fileData;
    }

    private static void writeFile(String fileName, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        }
    }
}
