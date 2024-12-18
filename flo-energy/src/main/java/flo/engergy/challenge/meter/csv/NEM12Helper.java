package flo.engergy.challenge.meter.csv;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper class for NEM12SqlGenerator
 */
public class NEM12Helper {

    private static final String SQL_INSERT = """
            INSERT INTO meter_readings(id, nmi, timestamp, consumption) VALUES ('%s', '%s', '%s', %s);
            """;

    /**
     * Given a 300 record and interval from the 200 record, converts the date and interval into milliseconds and maps
     * the millisecond timestamp to the corresponding consumption values (single entry map of {timestamp, consumption}).
     * A list of these maps is returned.
     */
    static final BiFunction<String, Integer, List<Map<Long, Double>>> transform300Line = (line, interval) -> {
        if (line.endsWith(","))
            line = line + "-";

        String[] tokens = line.split(",");
        String timestampStr = tokens[1];

        long timestampInMillis = new Calendar.Builder()
                .setDate(
                        Integer.parseInt(timestampStr.substring(0, 4)),
                        Integer.parseInt(timestampStr.substring(4, 6)) - 1,
                        Integer.parseInt(timestampStr.substring(7))
                )
                .build()
                .getTimeInMillis();
        long intervalInMillis = interval * 60 * 1000;

        List<Map<Long, Double>> timeValueMap = new ArrayList<>();
        for (int i = 2; i < tokens.length - 5; i++) {
            long time = ((i - 2) * intervalInMillis) + timestampInMillis;
            double value = Double.parseDouble(tokens[i]);

            timeValueMap.add(Map.of(time, value));
        }

        return timeValueMap;
    };


    /**
     * Converts <Map<String, List<List<Map<Long, Double>>>> to Map<String, List<Map<Long, Double>>>>.
     * Removes the nested list.
     */
    static final Function<Map<String, List<List<Map<Long, Double>>>>, Map<String, List<Map<Long, Double>>>> flatternValues = batches ->
            batches
                    .entrySet()
                    .stream()
                    .collect(
                            Collectors
                                    .toMap(Map.Entry::getKey,
                                            entry -> entry.getValue()
                                                    .stream()
                                                    .flatMap(Collection::stream)
                                                    .toList()
                                    ));


    /**
     * Takes the parameter map and a set, forms sql insert statements by substituting placeholders in SQL_INSERT string. SQL statements
     * built are added to the set.
     */
    static final BiConsumer<Map<String, List<Map<Long, Double>>>, Set<String>> createInsertStatements = (parameterMap, sqlInserts) ->
                                            parameterMap.forEach((key, value) -> {
                                                Set<String> statements = value
                                                        .stream()
                                                        .map(map -> {
                                                            Map.Entry<Long, Double> entry = map.entrySet().stream().findFirst().get();

                                                            return SQL_INSERT.formatted(UUID.randomUUID(), key, new Timestamp(entry.getKey()), entry.getValue());
                                                        }).collect(Collectors.toSet());

                                                sqlInserts.addAll(statements);
                                            });

}
