package flo.engergy.challenge.meter.csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public final class NEM12SqlWriter implements NEM12Action {

    private final String outputPath;
    private final Set<String> sqlInserts;

    public NEM12SqlWriter(String outputPath, Set<String> sqlInserts) {
        this.outputPath = outputPath;
        this.sqlInserts = sqlInserts;
    }

    /**
     * Writes the set of strings (sql insert statements) to the output file.
     *
     * @param sqlInserts: set of sql statements built
     * @throws IOException
     */
    private void writeSqlToOutFile(Set<String> sqlInserts) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            sqlInserts.forEach(line -> {
                try {
                    writer.write(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void execute() throws IOException {
        writeSqlToOutFile(sqlInserts);
    }
}
