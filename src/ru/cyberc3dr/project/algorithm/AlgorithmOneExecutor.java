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

                    vfClusterCheck(combo, rules);

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

        var disps = new Stack<String>();

        var appliedRules = new HashSet<Rule>();

        for(var frame : frames) {
            var rule = rules.stream()
                    .filter(r -> r.getVideoframe().equals(frame))
                    .findFirst().orElseThrow();

            var displays = rule.getDisplays();

            if(displays.size() == 1) {
                displays.forEach(disps::push);

                appliedRules.add(rule);
            }
        }

        logger.info("Stack");
        disps.forEach(disp -> logger.info(" - {}", disp));

        if(!disps.isEmpty()) {
            var combos = Sets.combinations(rules, 2)
                    .stream()
                    .map(List::copyOf)
                    .collect(Collectors.toSet());

            var conflicts = combos.stream()
                    .filter(combo -> {
                        var rule1 = combo.get(0);
                        var rule2 = combo.get(1);

                        var intersection = Sets.intersection(rule1.getDisplays(), rule2.getDisplays());

                        return !intersection.isEmpty();
                    })
                    .collect(Collectors.toSet());

            if(conflicts.isEmpty()) {
                rules.removeAll(appliedRules);
                appliedRules.clear();

                rules.forEach(rule -> disps.forEach(rule.getDisplays()::remove));

                rules.forEach(rule -> {
                    logger.info("After cleaning:");
                    logger.info("{} {}", rule.getVideoframe(), rule.getDisplays());
                });

                if(rules.stream().anyMatch(rule -> rule.getDisplays().isEmpty())) {
                    logger.info("Some rules have no displays left after assignment.");

                    return false;
                }
            } else {
                logger.info("Conflicts:");
                conflicts.forEach(conflict -> {
                    var rule1 = conflict.get(0);
                    var rule2 = conflict.get(1);

                    logger.info(" - {} {} <-> {} {}", rule1.getVideoframe(), rule1.getDisplays(),
                            rule2.getVideoframe(), rule2.getDisplays());
                });
            }
        }

        return false;
    }

}
