package ru.cyberc3dr.project.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public final class Cluster {
    private Set<String> frames = new HashSet<>();
    private Set<String> signals = new HashSet<>();
}