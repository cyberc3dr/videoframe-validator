package ru.cyberc3dr.project.model;

import lombok.Data;

import java.util.*;

@Data
public final class Configuration {
    private int number;
    private Map<String, List<String>> armToFrames = new HashMap<>();
    private Set<String> signals = new HashSet<>();
}