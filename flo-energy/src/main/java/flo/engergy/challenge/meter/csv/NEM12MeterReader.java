package flo.engergy.challenge.meter.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public final class NEM12MeterReader implements NEM12Action {

    private final String filePath;
    private List<String> lines;

    public NEM12MeterReader(String filePath, List<String> lines) {
        this.filePath = filePath;
        this.lines = lines;
    }

    /**
     * Reads the meter reading file and collects 200 and 300 records to a list of strings preserving the order.
     *
     * @return: list of 200 and 300 records from the meter reading file
     * @throws IOException
     */
    private List<String> loadMeterReadingsFromFile() throws IOException {
        List<String> rows;
        try (BufferedReader buffer = new BufferedReader(new FileReader(filePath))) {
            rows = buffer.lines()
                    .filter(line -> line.startsWith(PREFIX_200) || line.startsWith(PREFIX_300))
                    .toList();
        }
        return rows;
    }

    @Override
    public void execute() throws IOException {
        lines.addAll(loadMeterReadingsFromFile());
    }
}
