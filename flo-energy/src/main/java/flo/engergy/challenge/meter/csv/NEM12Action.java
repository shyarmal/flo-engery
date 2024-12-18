package flo.engergy.challenge.meter.csv;

import java.io.IOException;

sealed public interface NEM12Action permits NEM12MeterReader, NEM12RecordProcessor, NEM12SqlWriter {

    String PREFIX_200 = "200";
    String PREFIX_300 = "300";

    void execute() throws IOException;

}
