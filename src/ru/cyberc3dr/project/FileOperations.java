package ru.cyberc3dr.project;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class FileOperations {

    private final static Logger logger = Main.logger;

    public static @NotNull List<String> readFile(@NotNull File file) {
        final var list = new ArrayList<String>();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            var line = reader.readLine();

            while(line != null) {
                list.add(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return list;
    }

    public static byte @NotNull [] readBytes(@NotNull File file) {
        try(BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            return stream.readAllBytes();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new byte[0];
    }

    public static void writeLiteral(@NotNull File file, String content) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void writeBytes(@NotNull File file, byte[] content) {
        try(BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
            stream.write(content);
            stream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
