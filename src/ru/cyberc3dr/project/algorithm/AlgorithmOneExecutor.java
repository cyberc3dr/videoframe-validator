package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.FileOperations;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class AlgorithmOneExecutor {

    private final Logger logger = Main.logger;

    private final DataCenter dataCenter;
    private final List<List<VCluster>> foundVClusters = new ArrayList<>();
    private final List<Configuration> configurations = new ArrayList<>();

    private final StringBuilder logBuilder = new StringBuilder();

    public AlgorithmOneExecutor(DataCenter dataCenter) {
        this.dataCenter = SerializationUtils.clone(dataCenter);
    }

    public void execute() {
        logBuilder.append(dataCenter.toLogString()).append("\n");

        var k = dataCenter.getAllDisplays();

        while(dataCenter.getSignals().stream().anyMatch((it) -> !it.getVideoFrames().isEmpty())) {
            logBuilder.append("Testing power ").append(k).append("\n");
            var vclusters = find(k);

            while(vclusters.isEmpty() && k > 1) {
                logBuilder.append("Power ").append(k).append(" is empty.\n");
                k--;
                logBuilder.append("Testing power ").append(k).append("\n");
                vclusters = find(k);
            }

            foundVClusters.add(vclusters);
            updateConfigurations(vclusters.getFirst());

            logBuilder.append("Found vclusters! Power: ")
                    .append(k)
                    .append(", Clusters: ")
                    .append(vclusters.size())
                    .append("\n\n");

            logger.info("Found vclusters! Power: {}, Clusters: {}", k, vclusters.size());
        }

        logBuilder.append("\n");

        configurations.forEach(it -> {
            logBuilder.append(it.toLogString()).append("\n\n");
            logger.info(it.toLogString());
        });

        logBuilder.append("Total configurations: ").append(configurations.size());
        logger.info("Total {} configurations found:", configurations.size());

        FileOperations.writeLiteral(new File("latest.log"), logBuilder.toString());
    }

    public void updateConfigurations(VCluster vcluster) {
        var signals = vcluster.getSignals().stream()
                .map(Signal::getName)
                .collect(Collectors.toSet());

        Map<String, Set<String>> armToFrame = new HashMap<>();

        for(var assignment : vcluster.getAssignments()) {
            var frame = assignment.getVideoframe();
            var display = assignment.getDisplay();

            var arm = dataCenter.getArms().stream()
                    .filter(a -> a.getDisplays().contains(display))
                    .findFirst().orElseThrow();

            armToFrame.putIfAbsent(arm.getArmName(), new HashSet<>());
            armToFrame.get(arm.getArmName()).add(frame);
        }

        configurations.add(new Configuration(signals, armToFrame));

        vcluster.getSignals().forEach((signal) -> signal.getVideoFrames().removeAll(vcluster.getVideoframes()));
    }

    public @NotNull List<VCluster> find(int power) {
        var applicableSignals = dataCenter.getSignals().stream()
                .filter(signal -> signal.getVideoFrames().size() >= power)
                .toList();

        if(applicableSignals.isEmpty()) {
            logBuilder.append("No applicable signals found for power ")
                    .append(power)
                    .append("\n");

            logger.warn("No applicable signals found for power {}", power);
            return Collections.emptyList();
        }

        var frames = applicableSignals.stream()
                .flatMap(signal -> signal.getVideoFrames().stream())
                .collect(Collectors.toSet());

        var clusters = Sets.combinations(frames, power).stream()
                .filter(combo -> applicableSignals.stream().anyMatch(signal -> signal.getVideoFrames().containsAll(combo)))
                .map(combo -> {
                    var rules = dataCenter.generateRules(combo);

                    var checkResult = vfClusterCheck(combo, rules);

                    logBuilder.append("\n");

                    if(!checkResult.isValid()) return null;

                    var signals = applicableSignals.stream()
                            .filter(signal -> signal.getVideoFrames().containsAll(combo))
                            .collect(Collectors.toSet());

                    return new VCluster(power, combo, signals, checkResult.getAssignments());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        logBuilder.append("VClusters:\n");

        clusters.forEach(cluster -> {
            logBuilder.append("vframes: ")
                    .append(cluster.getVideoframes())
                    .append("\n")
                    .append("signals: ")
                    .append(cluster.getSignals())
                    .append("\n")
                    .append("display assignments: ")
                    .append(cluster.getAssignments())
                    .append("\n\n");

            logger.info("{} {} {}", cluster.getVideoframes(), cluster.getSignals(), cluster.getAssignments());
//            logger.info("\n{}", dataCenter.toLogString());
        });

        return clusters;
    }

    public ClusterCheckResult vfClusterCheck(@NotNull Set<String> frames, Set<Rule> rules) {
        if (frames.size() < 2) {
            LinkedList<DisplayAssignment> assignments = new LinkedList<>();

            var rule = rules.stream().findFirst().orElseThrow();

            var frame = rule.getVideoframe();
            var display = rule.getDisplays().stream().findFirst().orElseThrow();

            assignments.add(new DisplayAssignment(display, frame));

            return new ClusterCheckResult(true, assignments);
        }

        logBuilder.append("VFClusterCheck - Testing vfcluster: ")
                .append(frames)
                .append("\n");

        logger.info("Testing vfCluster: {}", frames);

        LinkedList<DisplayAssignment> assignments = new LinkedList<>();

        var isValid = solveAssignment(new ArrayList<>(rules), new LinkedList<>(), assignments);

        if(!isValid) {
            logBuilder.append("TEST FAILED - vfcluster not valid\n");
        }

        return new ClusterCheckResult(isValid, assignments);
    }

    private boolean solveAssignment(@NotNull List<Rule> pendingRules, LinkedList<String> usedDisplays, LinkedList<DisplayAssignment> assignments) {
        // Все правила удовлетворены
        if(pendingRules.isEmpty()) return true;

        Rule candidate = null;
        int minOptions = Integer.MAX_VALUE;

        // Подбираем правило, которое имеет минимальное количество доступных дисплеев
        // В данном состоянии стека использованных дисплеев
        for(var rule : pendingRules) {
            int currentOptions = 0;
            for(var d : rule.getDisplays()) {
                // Считаем количество неиспользованных до этого дисплеев.
                if(!usedDisplays.contains(d)) {
                    currentOptions++;
                }
            }

            // Если для правила нет доступных дисплеев, значит реализация невозможна
            if(currentOptions == 0) return false;

            if(currentOptions < minOptions) {
                minOptions = currentOptions;
                candidate = rule;
                if(minOptions == 1) break; // если есть правило с одним вариантом, берем его сразу
            }
        }

        List<Rule> nextStepRules = new ArrayList<>(pendingRules);
        nextStepRules.remove(candidate);

        assert candidate != null;
        for(var d : candidate.getDisplays()) {
            if(usedDisplays.contains(d)) continue;

            // Выбираем дисплей для текущего правила
            usedDisplays.add(d);

            // Рекурсивно решаем оставшуюся часть задачи.
            if(solveAssignment(nextStepRules, usedDisplays, assignments)) {
                logBuilder.append("Selected display ")
                        .append(d)
                        .append(" for vframe ")
                        .append(candidate.getVideoframe())
                        .append("\n");

                logger.info("Selected display {} for vframe {}", d, candidate.getVideoframe());
                assignments.add(new DisplayAssignment(d, candidate.getVideoframe()));
                return true; // успешное завершение
            }

            // Если в итоге мы не нашли решение, делаем откат
            // и берем следующий дисплей.
            usedDisplays.remove(d);
        }

        // В случае если решений вообще не существует.
        return false;
    }

}
