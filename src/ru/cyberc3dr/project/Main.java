package ru.cyberc3dr.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cyberc3dr.project.algorithm.ParallelValidationScenarioGenerator;
import ru.cyberc3dr.project.model.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command-line launcher for Algorithm 1.
 * <p>
 * Usage:
 *   java -jar project.jar <signals-path> <frames-file> <arms-file> <signal-to-frames-file> <arm-to-frames-file> <arm-displays-file>
 * <p>
 * - signals-path: either a directory with many "*.txt" signal files (examples in test data folder) or a single file
 * - frames-file: file with one frame id per line
 * - arms-file: file with one ARM id per line
 * - signal-to-frames-file: mapping lines in form "<signal>:<frame>" (one mapping per line). Multiple mappings for a signal allowed.
 * - arm-to-frames-file: mapping lines in form "<arm>:<frame>" (one mapping per line). Multiple mappings for an arm allowed.
 * - arm-displays-file: lines in form "<arm>:<count>" where count is an integer number of displays for that ARM
 * <p>
 * Delimiter between left and right in mapping files is a colon ':'.
 */
public class Main {

    public final static Logger logger = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Expected 6 arguments: <signals-path> <frames-file> <arms-file> <signal-to-frames-file> <arm-to-frames-file> <arm-displays-file>");
            System.out.println("Example (from project root):");
            System.out.println("  java -jar build/project.jar \"Файлы с тестовыми данными/1. Файлы с идентификаторами сигналов\" \"Файлы с тестовыми данными/2. Файл с идентификаторами видеокадров.txt\" \"Файлы с тестовыми данными/3. Файл с идентификаторами АРМов.txt\" \"Файлы с тестовыми данными/4. Файл с отображением сигнал - видеокадр.txt\" \"Файлы с тестовыми данными/5. Файл с отображением АРМ-видеокадр.txt\" \"Файлы с тестовыми данными/6. Файл с описанием количества мониторов на АРМах.txt\"");
            System.exit(1);
        }

        File signalsPath = new File(args[0]);
        File framesFile = new File(args[1]);
        File armsFile = new File(args[2]);
        File signalToFramesFile = new File(args[3]);
        File armToFramesFile = new File(args[4]);
        File armDisplaysFile = new File(args[5]);

        try {
            Set<String> signals = readSignals(signalsPath);
            Set<String> frames = readIdFile(framesFile);
            Set<String> arms = readIdFile(armsFile);

            Map<String, Set<String>> signalToFrames = readMappingFile(signalToFramesFile);
            Map<String, Set<String>> armToFrames = readMappingFile(armToFramesFile);
            Map<String, Integer> armDisplays = readDisplaysFile(armDisplaysFile);

            logger.info("Read {} signals, {} frames, {} ARMs", signals.size(), frames.size(), arms.size());
            logger.info("Signal to frames mappings: {}", signalToFrames.size());
            logger.info("ARM to frames mappings: {}", armToFrames.size());
            logger.info("ARM displays: {}", armDisplays.size());

            // Run algorithm 1
            List<Configuration> configs = ParallelValidationScenarioGenerator.algorithm1(signalToFrames, armToFrames, armDisplays);

            // Print results in readable form
            printConfigurations(configs);

        } catch (IOException e) {
            logger.error("IO error: {}", e.getMessage(), e);
            System.err.println("IO error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static Set<String> readSignals(File path) throws IOException {
        Set<String> res = new LinkedHashSet<>();
        if (!path.exists()) return res;
        if (path.isDirectory()) {
            File[] files = path.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".names"));
            if (files == null) return res;
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File f : files) res.addAll(readLinesTrimmed(f));
        } else {
            res.addAll(readLinesTrimmed(path));
        }
        return res.stream().filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<String> readIdFile(File f) throws IOException {
        if (!f.exists()) return Collections.emptySet();
        return readLinesTrimmed(f).stream().filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Map<String, Set<String>> readMappingFile(File f) throws IOException {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        if (!f.exists()) return map;
        for (String line : readLinesTrimmed(f)) {
            if (line.isEmpty()) continue;
            if (line.startsWith("#") || line.startsWith("//")) continue;
            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue;
            String left = parts[0].trim();
            String right = parts[1].trim();
            if (left.isEmpty() || right.isEmpty()) continue;
            map.computeIfAbsent(left, k -> new LinkedHashSet<>()).add(right);
        }
        return map;
    }

    private static Map<String, Integer> readDisplaysFile(File f) throws IOException {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (!f.exists()) return map;
        for (String line : readLinesTrimmed(f)) {
            if (line.isEmpty()) continue;
            if (line.startsWith("#") || line.startsWith("//")) continue;
            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue;
            String arm = parts[0].trim();
            String num = parts[1].trim();
            if (arm.isEmpty() || num.isEmpty()) continue;
            try {
                int v = Integer.parseInt(num);
                map.put(arm, v);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse display count for {}: {}", arm, num);
            }
        }
        return map;
    }

    private static List<String> readLinesTrimmed(File f) throws IOException {
        List<String> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                out.add(line.trim());
            }
        }
        return out;
    }

    private static void printConfigurations(List<Configuration> configs) {
        System.out.println("Produced configurations: " + configs.size());
        for (Configuration c : configs) {
            System.out.println("--- Configuration #" + c.getNumber() + " ---");
            System.out.println("ARM -> frames:");
            for (Map.Entry<String, List<String>> e : c.getArmToFrames().entrySet()) {
                System.out.println("  " + e.getKey() + " -> " + String.join(",", e.getValue()));
            }
            System.out.println("Signals:");
            List<String> sigs = new ArrayList<>(c.getSignals());
            Collections.sort(sigs);
            for (String s : sigs) System.out.println("  " + s);
        }
    }
}
