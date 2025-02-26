import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static int port = 12345; // Default port
    private static String storageDir = "storage/"; // Default storage directory

    public static void main(String[] args) {
        configureServer(args);
        ensureStorageDirectoryExists();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("File Server is running on port " + port);
            System.out.println("Storage directory: " + storageDir);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                executor.submit(new FileHandler(clientSocket, storageDir));
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void configureServer(String[] args) {
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]); // First argument = port
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }
        if (args.length >= 2) {
            storageDir = args[1]; // Second argument = storage directory
        }
    }

    private static void ensureStorageDirectoryExists() {
        File dir = new File(storageDir);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Failed to create storage directory: " + storageDir);
            System.exit(1);
        }
    }
}

class FileHandler implements Runnable {
    private Socket clientSocket;
    private String storageDir;

    public FileHandler(Socket socket, String storageDir) {
        this.clientSocket = socket;
        this.storageDir = storageDir; // Use the instance variable
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            
            String command = (String) in.readObject();
            switch (command.toUpperCase()) {
                case "UPLOAD":
                    handleUpload(in, out);
                    break;
                case "DOWNLOAD":
                    handleDownload(in, out);
                    break;
                default:
                    out.writeObject("Invalid command.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private void handleUpload(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        String fileName = (String) in.readObject();
        byte[] fileData = (byte[]) in.readObject();
        uploadFile(fileName, fileData);
        out.writeObject("File '" + fileName + "' uploaded successfully.");
    }

    private void handleDownload(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        String fileName = (String) in.readObject();
        byte[] fileData = downloadFile(fileName);
        if (fileData.length > 0) {
            out.writeObject(fileData);
        } else {
            out.writeObject("File not found: " + fileName);
        }
    }

    private void uploadFile(String fileName, byte[] fileData) throws IOException {
        File file = new File(storageDir, fileName); // Use the instance variable
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
        }
        System.out.println("File uploaded: " + file.getAbsolutePath());
    }

    private byte[] downloadFile(String fileName) throws IOException {
        File file = new File(storageDir, fileName); // Use the instance variable
        if (!file.exists()) {
            System.err.println("File not found: " + fileName);
            return new byte[0];
        }
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        System.out.println("File downloaded: " + file.getAbsolutePath());
        return fileData;
    }
}