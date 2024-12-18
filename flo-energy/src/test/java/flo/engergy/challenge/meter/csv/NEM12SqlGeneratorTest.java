package flo.engergy.challenge.meter.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class NEM12SqlGeneratorTest {

    private String OUT_FILE = System.getProperty("user.dir") + "/sql-inserts.sql";

    @BeforeEach
    public void init() throws IOException {
        Files.deleteIfExists(Path.of(OUT_FILE));
    }

    @Test
    public void shouldWriteSqlInsertsOfValidMeterReadingsFile() throws IOException {
        NEM12SqlGenerator.main(new String[]{Objects.requireNonNull(getClass().getResource("/meter-entries.csv")).getFile()});
        assertOutputFile();
    }

    @Test
    public void shouldWriteSqlInsertsOfValidMeterReadingsFileWithTwoArgs() throws IOException {
        NEM12SqlGenerator.main(new String[]{Objects.requireNonNull(getClass().getResource("/meter-entries.csv")).getFile(), OUT_FILE});
        assertOutputFile();
    }

    private void assertOutputFile() throws IOException {
        Set<String> written = Files
                .lines(Path.of(OUT_FILE))
                .collect(Collectors.toSet());

        assertEquals(384, written.size());
        assertEquals(192, written.stream().filter(line -> line.contains("NEM1201009")).count());
        assertEquals(384, written.stream().filter(line -> line.contains("NEM1201010") || line.contains("NEM1201009")).count());
        assertEquals(192, written.stream().filter(line -> line.contains(":30:00.0")).count());
        assertEquals(384, written.stream().filter(line -> line.contains(":30:00.0") || line.contains(":00:00.0")).count());
        assertEquals(384, written.stream().filter(line -> line.startsWith("INSERT INTO meter_readings(id, nmi, timestamp, consumption) VALUES (") || line.endsWith(");")).count());
    }

    @Test
    public void throwExceptionIfFilePathIsNotPassed() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> NEM12SqlGenerator.main(new String[]{}));

        assertEquals("File path is needed", thrown.getMessage());
    }

    @Test
    public void shouldNotFailForEmptyFile() throws IOException {
        NEM12SqlGenerator.main(new String[]{Objects.requireNonNull(getClass().getResource("/meter-entries.csv")).getFile()});
    }
}
