package normalCSV;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSV {

    public static void main(String[] args) {
        // Specify the path to your CSV file
        String csvFile = "data.csv";

        // Use try-with-resources to automatically close the BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Read the header
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(",");
                System.out.println("Headers: " + Arrays.toString(headers));
            }

            // Store all records
            List<List<String>> records = new ArrayList<>();

            // Read each line
            while ((line = br.readLine()) != null) {
                // Split by comma
                String[] values = line.split(",");
                records.add(Arrays.asList(values));

                // Print each line of data
                System.out.println("Record: " + Arrays.toString(values));
            }

            // Print total number of records
            System.out.println("\nTotal records: " + records.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
