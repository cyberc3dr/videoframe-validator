package ru.cyberc3dr.project;

import org.junit.jupiter.api.Test;
import ru.cyberc3dr.project.algorithm.ParallelValidationScenarioGenerator;
import ru.cyberc3dr.project.model.Configuration;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ParallelValidationScenarioGeneratorTest {

    @Test
    void testAlgorithm1() {
        // signal → frames
        Map<String, Set<String>> signalToFrames = new HashMap<>();
        signalToFrames.put("s1", new HashSet<>(Arrays.asList("v1","v2","v3","v4")));
        signalToFrames.put("s2", new HashSet<>(Arrays.asList("v1","v2","v3")));
        signalToFrames.put("s3", new HashSet<>(Arrays.asList("v3","v4")));

        // arm → frames
        Map<String, Set<String>> armToFrames = new HashMap<>();
        armToFrames.put("a1", new HashSet<>(Arrays.asList("v1","v2")));
        armToFrames.put("a2", new HashSet<>(Arrays.asList("v3","v4")));

        // arm → displays
        Map<String, Integer> armDisplays = new HashMap<>();
        armDisplays.put("a1", 2);
        armDisplays.put("a2", 1);

        // run
        List<Configuration> scenario =
                ParallelValidationScenarioGenerator.algorithm1(
                        new HashMap<>(signalToFrames),
                        armToFrames,
                        armDisplays
                );

        // check number of configs
        assertEquals(3, scenario.size());

        // check contents vs Table 24
        Configuration c1 = scenario.get(0);
        Configuration c2 = scenario.get(1);
        Configuration c3 = scenario.get(2);

        assertEquals(Set.of("s1","s2"), c1.getSignals());
        assertEquals(Set.of("s1","s3"), c2.getSignals());
        assertEquals(Set.of("s3"), c3.getSignals());
    }
}