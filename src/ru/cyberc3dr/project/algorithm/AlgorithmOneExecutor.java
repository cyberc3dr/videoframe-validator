package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.model.DataCenter;
import ru.cyberc3dr.project.model.Rule;
import ru.cyberc3dr.project.model.VCluster;

import java.util.*;
import java.util.stream.Collectors;

public final class AlgorithmOneExecutor {

    private final Logger logger = Main.logger;

    private final DataCenter dataCenter;

    public AlgorithmOneExecutor(DataCenter dataCenter) {
        this.dataCenter = SerializationUtils.clone(dataCenter);
    }

    public @NotNull List<VCluster> find(int power) {
        var applicableSignals = dataCenter.getSignals().stream()
                .filter(signal -> signal.getVideoFrames().size() >= power)
                .toList();

        if(applicableSignals.isEmpty()) {
            logger.warn("No applicable signals found for power {}", power);
            return Collections.emptyList();
        }

        var frames = applicableSignals.stream()
                .flatMap(signal -> signal.getVideoFrames().stream())
                .collect(Collectors.toSet());

        var clusters = Sets.combinations(frames, power).stream()
                .filter(combo -> applicableSignals.stream().anyMatch(signal -> signal.getVideoFrames().containsAll(combo)))
                .filter(combo -> {
                    var rules = dataCenter.generateRules(combo);

                    var result = vfClusterCheck(combo, rules);

                    logger.info("VCluster {} check result: {}", combo, result);

                    return true; // TODO
                })
                .map(combo -> {
                    var signals = applicableSignals.stream()
                            .filter(signal -> signal.getVideoFrames().containsAll(combo))
                            .collect(Collectors.toSet());

                    return new VCluster(combo, signals);
                })
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        clusters.forEach(cluster -> {
            logger.info("{} {}", cluster.getVideoframes(), cluster.getSignals());
//            logger.info("\n{}", dataCenter.toLogString());
        });

        return clusters;
    }

    public boolean vfClusterCheck(@NotNull Set<String> frames, Set<Rule> rules) {
        if (frames.size() < 2) return true;

        logger.info("Testing vfCluster: {}", frames);

        return solveAssignment(new ArrayList<>(rules), new LinkedList<>());
    }

    private boolean solveAssignment(List<Rule> pendingRules, LinkedList<String> usedDisplays) {
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
            if(solveAssignment(nextStepRules, usedDisplays)) {
                logger.info("Selected display {} for vframe {}", d, candidate.getVideoframe());
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
