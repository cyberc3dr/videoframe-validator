package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@RequiredArgsConstructor
public final class Signal implements Serializable {
    private final String name;
    private final Set<String> videoFrames;
}
