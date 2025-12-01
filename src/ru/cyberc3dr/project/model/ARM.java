package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public final class ARM implements Serializable {
    private final String armName;
    private final int displayCount;
    private final Set<String> videoFrames;
    private final Set<String> displays = new HashSet<>();
}
