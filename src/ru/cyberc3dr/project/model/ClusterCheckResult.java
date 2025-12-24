package ru.cyberc3dr.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public final class ClusterCheckResult {
    private final boolean isValid;
    private final Set<DisplayAssignment> assignments;
}
