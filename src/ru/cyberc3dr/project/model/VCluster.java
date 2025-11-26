package ru.cyberc3dr.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public final class VCluster implements Serializable {
    private final List<String> videoframes;
    private final List<Signal> signals;
}
