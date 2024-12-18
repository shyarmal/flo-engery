package flo.engergy.challenge.meter.csv;

import java.io.*;
import java.util.*;

public class NEM12SqlGenerator {

    public static final String PREFIX_200 = "200";
    public static final String PREFIX_300 = "300";
    private static final String OUT_FILE = System.getProperty("user.dir")+"/sql-inserts.sql";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("File path is needed");
        }

        List<String> lines = loadMeterReadingsFromFile(args[0]);

        Map<String, List<List<Map<Long, Double>>>> batches = formBatches(lines);
        Map<String, List<Map<Long, Double>>> simplifiedBatches = NEM12Helper.flatternValues.apply(batches);

        Set<String> sqlInserts = new HashSet<>();
        NEM12Helper.createInsertStatements.accept(simplifiedBatches, sqlInserts);

        writeSqlToOutFile(sqlInserts);
    }

    /**
     * Reads the meter reading file and collects 200 and 300 records to a list of strings preserving the order.
     *
     * @param filePath: Path to the meter record file
     * @return: list of 200 and 300 records from the meter reading file
     * @throws IOException
     */
    private static List<String> loadMeterReadingsFromFile(String filePath) throws IOException {
        List<String> lines;
        try (BufferedReader buffer = new BufferedReader(new FileReader(filePath))) {
            lines = buffer.lines()
                    .filter(line -> line.startsWith(PREFIX_200) || line.startsWith(PREFIX_300))
                    .toList();
        }
        return lines;
    }

    /**
     * Writes the set of strings (sql insert statements) to the output file.
     *
     * @param sqlInserts: set of sql statements built
     * @throws IOException
     */
    private static void writeSqlToOutFile(Set<String> sqlInserts) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_FILE))) {
            sqlInserts.forEach(line -> {
                try {
                    writer.write(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Given the list of 200 and 300 records, extracts nmi, interval, date and consumption fields. Extracted values are
     * reduced to a map with nmi as the key and a list of collection of timestamp and consumption.
     *
     * @param lines: 300 and 200 records
     * @return: map {nmi: [[{timestamp, consumption}, {timestamp, consumption}, ...], [{timestamp, consumption}, {timestamp, consumption}, ...], ..]}
     */
    private static Map<String, List<List<Map<Long, Double>>>> formBatches(List<String> lines) {
        Map<String, List<List<Map<Long, Double>>>> batches = new HashMap<>();
        String currentNmi = null;
        Integer currentInterval =  null;
        for (String line : lines) {
            if (line.startsWith(PREFIX_200)) {
                String[] values200 = line.split(",");
                currentNmi = values200[1];
                currentInterval = Integer.parseInt(values200[8]);
                batches.put(currentNmi, new ArrayList<>());
            } else {
                List<Map<Long, Double>> extracted300Values = NEM12Helper.transform300Line.apply(line, currentInterval);
                batches.get(currentNmi).add(extracted300Values);
            }
        }

        return batches;
    }

}
