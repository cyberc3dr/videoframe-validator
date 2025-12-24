package ru.cyberc3dr.project.solver;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.cyberc3dr.project.Main;
import ru.cyberc3dr.project.algorithm.AbstractAlgorithm;
import ru.cyberc3dr.project.model.DisplayAssignment;
import ru.cyberc3dr.project.model.Rule;

import java.util.*;

public final class DisplayAssignmentSolver {

    public final Logger logger = Main.logger;

    private Map<String, String> match;  // display -> videoframe
    private Map<String, String> reverseMatch;  // videoframe -> display
    private Set<String> visited;

    private final StringBuilder logBuilder;

    @Contract(pure = true)
    public DisplayAssignmentSolver(@NotNull AbstractAlgorithm algorithm) {
        this.logBuilder = algorithm.logBuilder;
    }

    /**
     * Решает задачу назначения дисплеев через алгоритм Куна (венгерский алгоритм)
     * Сложность: O(V * E) вместо O(V!)
     */
    public Set<DisplayAssignment> solve(Set<Rule> rules) {
        match = new HashMap<>();
        reverseMatch = new HashMap<>();

        // Сортируем правила: сначала те, у которых меньше вариантов
        List<Rule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparingInt(r -> r.getDisplays().size()));

        for (Rule rule : sortedRules) {
            visited = new HashSet<>();
            tryKuhn(rule, rules);
        }

        // Проверяем, все ли правила назначены
        Set<DisplayAssignment> result = new HashSet<>();
        for (Rule rule : rules) {
            String vframe = rule.getVideoframe();
            String display = reverseMatch.get(vframe);

            if (display == null) {
                logger.error("Cannot assign display for videoframe: {}", vframe);
                return Collections.emptySet();  // Решение не найдено
            }

            logger.info("Selected display {} for vframe {}", display, vframe);
            logBuilder.append("Selected display ")
                    .append(display)
                    .append(" for vframe ")
                    .append(vframe)
                    .append("\n");

            result.add(new DisplayAssignment(display, vframe));
        }

        return result;
    }

    /**
     * Алгоритм Куна — пытается найти увеличивающий путь
     */
    private boolean tryKuhn(Rule rule, Set<Rule> allRules) {
        String vframe = rule.getVideoframe();

        for (String display : rule.getDisplays()) {
            if (visited.contains(display)) continue;
            visited.add(display);

            String currentOwner = match.get(display);

            if (currentOwner == null) {
                // Дисплей свободен — назначаем
                match.put(display, vframe);
                reverseMatch.put(vframe, display);
                return true;
            } else {
                // Дисплей занят — пробуем "вытеснить" текущего владельца
                Rule ownerRule = findRuleByVideoframe(allRules, currentOwner);
                if (ownerRule != null && tryKuhn(ownerRule, allRules)) {
                    // Владелец нашёл другой дисплей — забираем этот
                    match.put(display, vframe);
                    reverseMatch.put(vframe, display);
                    return true;
                }
            }
        }

        return false;
    }

    private Rule findRuleByVideoframe(Set<Rule> rules, String vframe) {
        for (Rule r : rules) {
            if (r.getVideoframe().equals(vframe)) return r;
        }
        return null;
    }
}