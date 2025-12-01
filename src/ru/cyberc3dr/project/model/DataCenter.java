package ru.cyberc3dr.project.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public final class DataCenter implements Serializable {
    private final Set<Signal> signals;
    private final Set<ARM> arms;

    public DataCenter(Set<Signal> signals, Set<ARM> arms) {
        this.signals = signals;
        this.arms = arms;

        assignDisplays();
    }

    public void assignDisplays() {
        int counter = 1;

        for(var arm : arms) {
            for(int i = 0; i < arm.getDisplayCount(); i++) {
                arm.getDisplays().add("d" + counter++);
            }
        }
    }

    public @NotNull Set<Rule> generateRules(@NotNull Set<String> frames) {
        var rules = new HashSet<Rule>();

        for(var frame : frames) {
            rules.add(
                    new Rule(frame, arms.stream()
                    .filter(arm -> arm.getVideoFrames().contains(frame))
                    .map(ARM::getDisplays)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet()))
            );
        }

        return rules;
    }

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
