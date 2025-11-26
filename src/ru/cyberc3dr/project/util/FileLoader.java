package ru.cyberc3dr.project.util;

import ru.cyberc3dr.project.FileOperations;
import ru.cyberc3dr.project.model.ARM;
import ru.cyberc3dr.project.model.DataCenter;
import ru.cyberc3dr.project.model.Signal;

import java.io.File;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class FileLoader {

    public static DataCenter load(String signalsPath, String signalsToFramesPath, String armsToFramesPath, String armMonitorCountsPath) {
        var signalsFile = new File(signalsPath);
        var signals = FileOperations.readFile(signalsFile).stream()
                .map(name -> new Signal(name, new HashSet<>()))
                .collect(Collectors.toSet());

        var signalsToFramesFile = new File(signalsToFramesPath);

        FileOperations.readFile(signalsToFramesFile)
                .forEach(association -> {
                    var parts = association.split(":");
                    var signalName = parts[0];
                    var frameName = parts[1];
                    signals.stream().filter(signal -> signal.getName().equals(signalName))
                            .forEach(signal -> signal.getVideoFrames().add(frameName));
                });

        var armsMonitorCountsFile = new File(armMonitorCountsPath);
        var arms = FileOperations.readFile(armsMonitorCountsFile).stream()
                .map(association -> {
                    var parts = association.split(":");
                    var armName = parts[0];
                    var monitorCount = Integer.parseInt(parts[1]);
                    return new ARM(armName, monitorCount, new HashSet<>());
                })
                .collect(Collectors.toSet());

        var armsToFramesFile = new File(armsToFramesPath);

        FileOperations.readFile(armsToFramesFile)
                .forEach(association -> {
                    var parts = association.split(":");
                    var armName = parts[0];
                    var frameName = parts[1];
                    arms.stream().filter(arm -> arm.getArmName().equals(armName))
                            .forEach(arm -> arm.getVideoFrames().add(frameName));
                });

        return new DataCenter(
                signals,
                arms
        );
    }
}
