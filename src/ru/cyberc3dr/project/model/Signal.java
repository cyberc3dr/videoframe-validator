package ru.cyberc3dr.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
public final class Signal implements Serializable {
    private final String name;
    private final Set<String> videoFrames;
}
