package flo.engergy.challenge.meter.csv;

import java.util.*;

public final class NEM12RecordProcessor implements NEM12Action {

    private final List<String> lines;
    private final Set<String> sqlInserts;

    public NEM12RecordProcessor(List<String> lines, Set<String> sqlInserts) {
        this.lines = lines;
        this.sqlInserts = sqlInserts;
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

    @Override
    public void execute() {
        Map<String, List<List<Map<Long, Double>>>> batches = formBatches(lines);
        Map<String, List<Map<Long, Double>>> simplifiedBatches = NEM12Helper.flatternValues.apply(batches);

        NEM12Helper.createInsertStatements.accept(simplifiedBatches, sqlInserts);
    }
}
