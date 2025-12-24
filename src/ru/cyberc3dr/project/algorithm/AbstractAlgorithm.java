package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.FileOperations;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.model.*;
import ru.cyberc3dr.project.solver.DisplayAssignmentSolver;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public abstract class AbstractAlgorithm {

    public final DataCenter dataCenter;
    public final Logger logger = Main.logger;
    public final StringBuilder logBuilder = new StringBuilder();
    public final List<Configuration> configurations = new ArrayList<>();

    private final String algorithmName;

    public AbstractAlgorithm(DataCenter dataCenter, String algorithmName) {
        this.dataCenter = SerializationUtils.clone(dataCenter);
        this.algorithmName = algorithmName;
    }

    public abstract void doAlgorithmLogic();

    public final void execute() {
        logger.info(dataCenter.toLogString());
        logBuilder.append(dataCenter.toLogString()).append("\n");

        doAlgorithmLogic();

        logBuilder.append("\n");

        configurations.forEach(it -> {
            logBuilder.append(it.toLogString()).append("\n\n");
            logger.info(it.toLogString());
        });

        logBuilder.append("Total configurations: ").append(configurations.size());
        logger.info("Total {} configurations found:", configurations.size());

        FileOperations.writeLiteral(new File("latest-" + algorithmName + ".log"), logBuilder.toString());
    }

    public final List<VCluster> findGoodCombos(List<Signal> signals, Set<String> frames, int power) {
        var clusters = Sets.combinations(frames, power).stream()
                .filter(combo -> signals.stream().anyMatch(signal -> signal.getVideoFrames().containsAll(combo)))
                .map(combo -> {
                    var rules = dataCenter.generateRules(combo);

                    var checkResult = vfClusterCheck(combo, rules);

                    logBuilder.append("\n");

                    if(!checkResult.isValid()) return null;

                    var clusterSignals = signals.stream()
                            .filter(signal -> signal.getVideoFrames().containsAll(combo))
                            .collect(Collectors.toSet());

                    return new VCluster(power, combo, clusterSignals, checkResult.getAssignments());
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
        });

        return clusters;
    }

    @Contract("_, _ -> new")
    public final @NotNull ClusterCheckResult vfClusterCheck(@NotNull Set<String> frames, Set<Rule> rules) {
        logBuilder.append("VFClusterCheck - Testing vfcluster: ")
                .append(frames)
                .append("\n");

        logger.info("Testing vfCluster: {}", frames);

        if (frames.size() < 2) {
            logBuilder.append("No need to test - 1 vframe\n");

            Set<DisplayAssignment> assignments = new HashSet<>();

            var rule = rules.stream().findFirst().orElseThrow();

            var frame = rule.getVideoframe();
            var display = rule.getDisplays().stream().findFirst().orElseThrow();

            assignments.add(new DisplayAssignment(display, frame));

            return new ClusterCheckResult(true, assignments);
        }

//        LinkedList<DisplayAssignment> assignments = new LinkedList<>();
//
//        var isValid = solveAssignment(new ArrayList<>(rules), new LinkedList<>(), assignments);
//
//        if(!isValid) {
//            logBuilder.append("TEST FAILED - vfcluster not valid\n");
//        }

        DisplayAssignmentSolver solver = new DisplayAssignmentSolver(this);
        Set<DisplayAssignment> assignments = solver.solve(rules);

        var isValid = !assignments.isEmpty();
        if(!isValid) {
            logBuilder.append("TEST FAILED - vfcluster not valid\n");
        }

        return new ClusterCheckResult(isValid, assignments);
    }

    @Deprecated
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
