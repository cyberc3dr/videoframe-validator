package ru.cyberc3dr.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
public final class ARM implements Serializable {
    private final String armName;
    private final int displayCount;
    private final Set<String> videoFrames;
}
