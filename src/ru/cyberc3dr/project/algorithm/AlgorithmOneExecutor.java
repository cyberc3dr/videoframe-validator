package ru.cyberc3dr.project.algorithm;

import org.jetbrains.annotations.NotNull;
import ru.cyberc3dr.project.model.*;

import java.util.*;
import java.util.stream.Collectors;

public final class AlgorithmOneExecutor extends AbstractAlgorithm {

    public AlgorithmOneExecutor(DataCenter dataCenter) {
        super(dataCenter, "first");
    }

    @Override
    public void doAlgorithmLogic() {
        var k = dataCenter.getAllDisplays();
        var h = 0;

        while(dataCenter.getSignals().stream().anyMatch((it) -> !it.getVideoFrames().isEmpty())) {
            logBuilder.append("Testing power ").append(k).append("\n");
            var vclusters = find(k);

            while(vclusters.isEmpty() && k > 1) {
                logger.info("Power {} is empty", k);
                logBuilder.append("Power ").append(k).append(" is empty.\n");
                h++;
                k--;
                logBuilder.append("Testing power ").append(k).append("\n");
                vclusters = find(k);
            }

            final var vcluster = vclusters.getFirst();

            if(!configurations.isEmpty()) {
                final var kk = k;
                final var hh = h;
                final var p = k + h;

                var configs = configurations.stream()
                        .filter(it -> {
                            var clusters = it.getArmToFrame().values().stream()
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toSet());

                            logger.info("clusters: {}", clusters);
                            logger.info("p={}, k={}, h={}", p, kk, hh);

                            return clusters.size() == p && clusters.containsAll(vcluster.getVideoframes());
                        })
                        .collect(Collectors.toSet());

                logger.info("configs: {}", configs);

                // Rule 2
                if(!configs.isEmpty()) {
                    configs.forEach(it -> updateConfigurations2(vcluster, it));
                } else {
                    updateConfigurations(vcluster);
                }

            } else {
                updateConfigurations(vcluster);
            }
            h = 0;

            logBuilder.append("Found vclusters! Power: ")
                    .append(k)
                    .append(", Clusters: ")
                    .append(vclusters.size())
                    .append("\n\n");

            logger.info("Found vclusters! Power: {}, Clusters: {}", k, vclusters.size());
        }
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

    public void updateConfigurations2(VCluster vcluster, Configuration config) {
        var signals = vcluster.getSignals().stream()
                .map(Signal::getName)
                .collect(Collectors.toSet());

        config.getSignals().addAll(signals);

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
