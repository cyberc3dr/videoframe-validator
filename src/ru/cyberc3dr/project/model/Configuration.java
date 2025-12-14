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
            sb.append(String.join(", ", armToFrame.get(arm))).append("\n");
        }

        sb.append("Signals:\n");
        sb.append(String.join(", ", signals));

        return sb.toString();
    }
}
