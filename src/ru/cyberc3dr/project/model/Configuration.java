package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@RequiredArgsConstructor
public final class Configuration {
    private final Set<String> signals;
    private final Map<String, Set<String>> armToFrame;

    public String toLogString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Configuration:\n");
        for (String arm : armToFrame.keySet()) {
            sb.append("ARM: ").append(arm).append("\n");
            sb.append("Video Frames:\n");
            sb.append(String.join(", ", armToFrame.get(arm))).append("\n\n");
        }

        sb.append("Signals (").append(signals.size()).append("):\n");
        sb.append(String.join("\n", signals));
        sb.append("\n");
        sb.append("-------------------------");

        return sb.toString();
    }
}
