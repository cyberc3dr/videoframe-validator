package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Set;

@Data
@RequiredArgsConstructor
public final class VCluster implements Serializable, Comparable<VCluster> {
    private final Set<String> videoframes;
    private final Set<Signal> signals;

    @Override
    public int compareTo(@NotNull VCluster o) {
        return signals.size() - o.signals.size();
    }
}
