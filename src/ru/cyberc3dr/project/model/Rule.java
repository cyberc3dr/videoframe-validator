package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public final class Rule {
    private final String videoframe;
    private final Set<String> displays;
}
