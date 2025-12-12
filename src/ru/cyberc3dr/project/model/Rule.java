package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.SortedSet;

@Data
@RequiredArgsConstructor
public final class Rule {
    private final String videoframe;
    private final SortedSet<String> displays;
}
