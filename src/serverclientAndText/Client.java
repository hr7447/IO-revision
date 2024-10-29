package serverclientAndText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileClient <server_address> <output_directory>");
            System.exit(1);
        }

        String host = args[0];
        String outputDir = args[1];
        int port = 5000;

        File directory = new File(outputDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.out.println("Error: Could not create output directory");
                System.exit(1);
            }
        }

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to server");

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();

            File outputFile = new File(directory, fileName);
            System.out.println("Receiving file: " + outputFile.getAbsolutePath());

            InputStream inputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize &&
                    (bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                int progress = (int) ((totalBytesRead * 100) / fileSize);
                System.out.print("\rReceiving: " + progress + "% complete");
            }

            fileOutputStream.close();
            System.out.println("\nFile received successfully");

            // After receiving, read and display the file contents
            System.out.println("\n=== Client reading received file contents ===");
            System.out.println("\nReading line by line:");
            readLineByLine(outputFile);
            System.out.println("\nReading paragraph by paragraph:");
            readParagraphs(outputFile);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
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
