package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.model.ARM;
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

                    return vfClusterCheck(frames, rules);
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
        var disps = new HashSet<String>();

        var appliedRules = new HashSet<Rule>();

        for(var frame : frames) {
            var rule = rules.stream()
                    .filter(r -> r.getVideoframe().equals(frame))
                    .findFirst().orElseThrow();

            var displays = rule.getDisplays();

            if(displays.size() == 1) {
                disps.addAll(displays);

                appliedRules.add(rule);
            }
        }

        if(!disps.isEmpty()) {
            var d = rules.stream().map(Rule::getDisplays)
                    .collect(Collectors.toSet());

            var combos = Sets.combinations(d, 2)
                    .stream()
                    .map(List::copyOf)
                    .collect(Collectors.toSet());

            var hasIntersection = combos.stream().anyMatch(
                    combo -> !Sets.intersection(combo.getFirst(), combo.get(1)).isEmpty());

            if(!hasIntersection) {
                rules.removeAll(appliedRules);
                appliedRules.clear();


            }
        }

        return false;
    }

}
