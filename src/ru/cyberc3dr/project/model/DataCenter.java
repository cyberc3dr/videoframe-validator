package ru.cyberc3dr.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
public final class DataCenter implements Serializable {
    private final Set<Signal> signals;
    private final Set<ARM> arms;

    public int getAllDisplays() {
        return arms.stream().mapToInt(ARM::getDisplayCount).sum();
    }

    public String toLogString() {
        var sb = new StringBuilder();
        sb.append("DataCenter:\n");
        sb.append("Signals (").append(signals.size()).append("):\n");
        for (Signal s : signals) {
            sb.append("  - ").append(s.getName()).append(": ").append(s.getVideoFrames()).append("\n");
        }
        sb.append("ARMs (").append(arms.size()).append("):\n");
        for (ARM a : arms) {
            sb.append("  - ").append(a.getArmName())
              .append(" (displays=").append(a.getDisplayCount()).append(") : ")
              .append(a.getVideoFrames()).append("\n");
        }
        return sb.toString();
    }

}
