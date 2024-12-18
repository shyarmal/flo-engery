package flo.engergy.challenge.meter.csv;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NEM12HelperTest {

    @Test
    public void testTransform300Line() {
        String record300 = "300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.154,0.460,0.770,1.003,1.059,1.750,1.423,1.200,0.980,1.111,0.800,1.403,1.145,1.173,1.065,1.187,0.900,0.998,0.768,1.432,0.899,1.211,0.873,0.786,1.504,0.719,0.817,0.780,0.709,0.700,0.565,0.655,0.543,0.786,0.430,0.432,A,,,20050310121004,";
        int interval = 30;

        List<Map<Long, Double>> result = NEM12Helper.transform300Line.apply(record300, interval);
        assertEquals(48, result.size());

        long timestampInMillis = new Calendar.Builder()
                .setDate(2005, Calendar.MARCH, 1)
                .build()
                .getTimeInMillis();

        List<Long> mapKeys = result
                .stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Long::longValue))
                .toList();

        long intervalMillis = interval * 60 * 1000;
        for (int i = 0; i < 48; i++) {
            long time = timestampInMillis + ((long) i * intervalMillis);
            assertEquals(time, mapKeys.get(i));
        }

        List<Double> mapValues = result
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .toList();

        String[] expectedValues = record300.substring(13, record300.indexOf(",A")).split(",");
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(Double.parseDouble(expectedValues[i]), mapValues.get(i));
        }
    }

    @Test
    public void testCreateInsertStatements() {
        Map<String, List<Map<Long, Double>>> params = Map.of("NEM1201009",
                List.of(Map.of(23l, 83.4d), Map.of(25l, 34.4d)));
        Set<String> sql = new HashSet<>();

        NEM12Helper.createInsertStatements.accept(params, sql);
        assertEquals(2, sql.size());
        assertEquals(2, sql.stream().filter(st -> st.startsWith("INSERT INTO meter_readings")).count());
        assertEquals(2, sql.stream().filter(st -> st.contains("NEM1201009")).count());
        assertEquals(2, sql.stream().filter(st -> st.contains("1970-01-01")).count());
        assertEquals(1, sql.stream().filter(st -> st.contains("83.4")).count());
        assertEquals(1, sql.stream().filter(st -> st.contains("34.4")).count());

    }

    @Test
    public void testFlatternValues() {
        Map<String, List<List<Map<Long, Double>>>> params = Map.of("NEM1201009",
                List.of(List.of(Map.of(23L, 83.4d), Map.of(25L, 34.4d)), List.of(Map.of(13L, 43.4d))));

        Map<String, List<Map<Long, Double>>> result = NEM12Helper.flatternValues.apply(params);
        List<Map<Long, Double>> resultValues = result.get("NEM1201009");

        assertEquals(1, result.size());
        assertEquals(3, resultValues.size());

        long count = resultValues
                .stream()
                .filter(map -> {
                    boolean keyContains = map.containsKey(23L) || map.containsKey(25L) || map.containsKey(13L);
                    boolean valueContains = map.containsValue(83.4d) || map.containsValue(34.4d) || map.containsValue(43.4d);
                    return keyContains && valueContains;
                })
                .count();


        assertEquals(3, count);
    }
}
