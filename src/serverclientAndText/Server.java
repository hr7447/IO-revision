package serverclientAndText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java FileServer <file_path>");
            System.exit(1);
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Error: File '" + filePath + "' does not exist or is not a regular file");
            System.exit(1);
        }

        // First read and display the file contents on server side
        System.out.println("=== Server reading file contents ===");
        System.out.println("\nReading line by line:");
        readLineByLine(file);
        System.out.println("\nReading paragraph by paragraph:");
        readParagraphs(file);

        // Then start the server
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("\nServer is listening on port 5000");
            System.out.println("Ready to send file: " + filePath);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                new Thread(() -> handleClient(socket, file)).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket, File file) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(file.length());

            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = socket.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;
            long fileSize = file.length();

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                int progress = (int) ((totalBytesSent * 100) / fileSize);
                System.out.print("\rSending: " + progress + "% complete");
            }

            outputStream.flush();
            System.out.println("\nFile sent successfully");

            fileInputStream.close();
            socket.close();

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void readLineByLine(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    System.out.printf("Line %d: %s%n", lineNumber, line);
                }
                lineNumber++;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static void readParagraphs(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> paragraph = new ArrayList<>();
            int paragraphNumber = 1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (!paragraph.isEmpty()) {
                        System.out.printf("Paragraph %d:%n%s%n%n",
                                paragraphNumber++,
                                String.join(" ", paragraph));
                        paragraph.clear();
                    }
                } else {
                    paragraph.add(line.trim());
                }
            }

            if (!paragraph.isEmpty()) {
                System.out.printf("Paragraph %d:%n%s%n",
                        paragraphNumber,
                        String.join(" ", paragraph));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}