package ru.cyberc3dr.project;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cyberc3dr.project.algorithm.AlgorithmOneExecutor;
import ru.cyberc3dr.project.model.ARM;
import ru.cyberc3dr.project.model.DataCenter;
import ru.cyberc3dr.project.model.Signal;
import ru.cyberc3dr.project.util.FileLoader;

import java.util.Set;

public final class Main {

    public final static Logger logger = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {
        DataCenter datacenter = null;

        // если переданы как минимум 4 аргумента — пытаемся загрузить из файлов
        if (args != null && args.length >= 4) {
            try {
                var signalsPath = args[0];
                var signalsToFramesPath = args[1];
                var armsToFramesPath = args[2];
                var armMonitorCountsPath = args[3];

                logger.info("Loading DataCenter from files: {}, {}, {}, {}", signalsPath, signalsToFramesPath, armsToFramesPath, armMonitorCountsPath);
                datacenter = FileLoader.load(signalsPath, signalsToFramesPath, armsToFramesPath, armMonitorCountsPath);
            } catch (Exception ex) {
                logger.error("Failed to load DataCenter from provided files, will use default dataset. Error: {}", ex.getMessage(), ex);
            }
        } else {
            logger.info("Insufficient arguments for file loading. Expected 4 paths: <signals> <signalsToFrames> <armsToFrames> <armMonitorCounts>. Using default dataset.");
        }

        // fallback — встроенный пример, если загрузка из файлов не удалась или аргументы не переданы
        if (datacenter == null) {
            datacenter = new DataCenter(
                Set.of(
                    new Signal("s1", Sets.newHashSet("v1", "v2", "v3", "v4")),
                    new Signal("s2", Sets.newHashSet("v1", "v2", "v3")),
                    new Signal("s3", Sets.newHashSet("v3", "v4"))
                ),
                Set.of(
                    new ARM("a1", 2, Sets.newHashSet("v1", "v2")),
                    new ARM("a2", 1, Sets.newHashSet("v3", "v4"))
                )
            );
        }

        // Log full datacenter using Main.logger
        printDataCenter(datacenter);

        var executor = new AlgorithmOneExecutor(datacenter);

        executor.execute();

//        for(int k = datacenter.getAllDisplays(); k > 0; k--) {
//            logger.info("Testing for k={}", k);
//            var clusters = executor.find(k);
//        }
//
//        logger.info("Testing for k={}", 1);
//        executor.find(1);
    }

    private static void printDataCenter(DataCenter dc) {
        logger.info("\n{}", dc.toLogString());
    }
}
