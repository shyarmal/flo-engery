package flo.engergy.challenge.meter.csv;

import java.io.*;
import java.util.*;

public class NEM12SqlGenerator {

    private static final String OUT_FILE = System.getProperty("user.dir")+"/sql-inserts.sql";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("File path is needed");
        }

        List<String> lines = new ArrayList<>();
        Set<String> sqlInserts = new HashSet<>();

        List<NEM12Action> actions = List.of(
                new NEM12MeterReader(args[0], lines),
                new NEM12RecordProcessor(lines, sqlInserts),
                new NEM12SqlWriter(args.length == 2 ? args[1] : OUT_FILE, sqlInserts)
        );

        for (NEM12Action action : actions) {
            action.execute();
        }

    }

}
