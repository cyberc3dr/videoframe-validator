package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import ru.cyberc3dr.project.model.Configuration;
import ru.cyberc3dr.project.model.DataCenter;
import ru.cyberc3dr.project.model.Signal;
import ru.cyberc3dr.project.model.VCluster;

import java.util.*;
import java.util.stream.Collectors;

public final class AlgorithmTwoExecutor extends AbstractAlgorithm {

    public AlgorithmTwoExecutor(DataCenter dataCenter) {
        super(dataCenter, "two");
    }

    @Override
    public void doAlgorithmLogic() {
        var k = dataCenter.getAllDisplays();

        while (dataCenter.getSignals().stream().anyMatch((it) -> !it.getVideoFrames().isEmpty())) {
            logBuilder.append("Testing power ").append(k).append("\n");
            var vclusters = find(k);

            while(vclusters.isEmpty() && k > 1) {
                logBuilder.append("Power ").append(k).append(" is empty.\n");
                k--;
                logBuilder.append("Testing power ").append(k).append("\n");
                vclusters = find(k);
            }

            var vcluster = vclusters.getFirst();
            updateConfigurations1(vcluster);

            var q = 1;

            var power = vcluster.getVideoframes().size();

            while(q < power) {
                var nextPower = power - q;

                logBuilder.append("Testing sub-power ").append(nextPower).append("\n");
                logger.info("Testing sub-power {}", nextPower);

                var signals = dataCenter.getSignals().stream()
                        .filter(signal -> signal.getVideoFrames().size() >= nextPower)
                        .toList();

                Sets.combinations(vcluster.getVideoframes(), nextPower).stream()
                        .filter(combo -> signals.stream().anyMatch(signal -> signal.getVideoFrames().containsAll(combo)))
                        .map(combo -> {
                            var rules = dataCenter.generateRules(combo);

                            var checkResult = vfClusterCheck(combo, rules);

                            logBuilder.append("\n");

                            if(!checkResult.isValid()) return null;

                            var clusterSignals = signals.stream()
                                    .filter(signal -> signal.getVideoFrames().containsAll(combo))
                                    .collect(Collectors.toSet());

                            return new VCluster(nextPower, combo, clusterSignals, checkResult.getAssignments());
                        })
                        .filter(Objects::nonNull)
                        .forEach(this::updateConfigurations2);

                q++;
            }
        }
    }

    public void updateConfigurations2(VCluster vcluster) {
        logBuilder.append("Update configurations 2 executed.\n");
        logBuilder.append(vcluster.getVideoframes().toString()).append("\n");
        logger.info("Update configurations 2 executed.");
        logger.info(vcluster.getVideoframes().toString());

        var signals = vcluster.getSignals().stream()
                .map(Signal::getName)
                .collect(Collectors.toSet());

        var lastConfiguration = configurations.getLast();

        lastConfiguration.getSignals().addAll(signals);

        vcluster.getSignals().forEach((signal) -> signal.getVideoFrames().removeAll(vcluster.getVideoframes()));
    }

    public void updateConfigurations1(VCluster vcluster) {
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

        return findGoodCombos(applicableSignals, frames, power);
    }
}
