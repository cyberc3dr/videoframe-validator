package ru.cyberc3dr.project.algorithm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.model.DataCenter;
import ru.cyberc3dr.project.model.VCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AlgorithmOneExecutor {

    private final Logger logger = Main.logger;

    private final DataCenter dataCenter;
    private int k;

    public AlgorithmOneExecutor(DataCenter dataCenter) {
        this.dataCenter = SerializationUtils.clone(dataCenter);
        this.k = dataCenter.getAllDisplays();
    }

    public @NotNull List<VCluster> find(int power) {
        var clusters = new ArrayList<VCluster>();

        var applicableSignals = dataCenter.getSignals().stream()
                .filter(signal -> signal.getVideoFrames().size() >= power)
                .toList();

        if(applicableSignals.isEmpty()) {
            logger.warn("No applicable signals found for power {}", power);
            return clusters;
        }

        var frames = applicableSignals.stream()
                .flatMap(signal -> signal.getVideoFrames().stream())
                .collect(Collectors.toSet());

        var combos = Sets.combinations(frames, power).stream()
                .filter(combo -> applicableSignals.stream().allMatch(signal -> signal.getVideoFrames().containsAll(combo)))
                .collect(Collectors.toSet());

        combos.forEach(combo -> logger.info(combo.toString()));

        return clusters;
    }

}
