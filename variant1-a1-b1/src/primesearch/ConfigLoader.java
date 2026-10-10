package primesearch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads config file, parses x and y values. Handles invalid String inputs.
 */
public final class ConfigLoader {
    private ConfigLoader() {}
    private static final long MAX_CONFIG_BYTES = 1_048_576; // 1 MB

    public static Config load(Path path) throws ConfigException {
        List<String> lines;
        int threads = 0;
        long max = 0;

        boolean xIngested = false, yIngested = false;

        // Read file
        try {
            if (Files.size(path) > MAX_CONFIG_BYTES) {
                throw new ConfigException(
                        "Configuration file is too large (max " + MAX_CONFIG_BYTES + " bytes): " + path);
            }
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ConfigException(
                    "Failed to read configuration file: " + path,
                    e
            );
        }

        // Iterate through each line
        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            line = line.trim(); // Strings are immutable, must reassign to itself
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }

            // Verify if format is "x = n" or "y = n" where n is a number
            String[] parts = line.split("=", 2); // Split into "<key>, '=', <value>"
            if (parts.length != 2) { // handle case if x=n not followed
                throw new ConfigException("Line " + lineNo + ": Expected 'key = value'. (\"" + line + "\")");
            }
            String key = parts[0].trim(); // x
            String value = parts[1].trim(); // n

            if (key.equals("x")) {
                if (!xIngested) {
                    xIngested = true;
                } else { // runs if x found more than once
                    throw new ConfigException ("Line " + lineNo + ": Duplicate x value found. (\"" + line + "\")");
                }

                try {
                    if (!value.matches("[0-9]+")) {
                        throw new ConfigException("Line " + lineNo + ": Invalid integer input. Only digits 0-9 allowed. (\"" + line + "\")");
                    }
                    threads = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ConfigException("Line " + lineNo + ": x is too large (max " + Integer.MAX_VALUE + "). (\"" + line + "\")", e);
                }
                if (threads <= 0) {
                    throw new ConfigException("Line " + lineNo + ": x threads cannot be less than 1. (\"" + line + "\")");
                }

            }
            else if (key.equals("y")) {
                if (!yIngested) {
                    yIngested = true;
                } else { // runs if y found more than once
                    throw new ConfigException("Line " + lineNo + ": Duplicate y value found. (\"" + line + "\")");

                }

                try {
                    if (!value.matches("[0-9]+")) {
                        throw new ConfigException("Line " + lineNo + ": Invalid integer input. Only digits 0-9 allowed. (\"" + line + "\")");
                    }
                    max = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new ConfigException("Line " + lineNo + ": y is too large (max " + Long.MAX_VALUE + "). (\"" + line + "\")", e);
                }

                if (max <= 1) {
                    throw new ConfigException("Line " + lineNo + ": y prime limit cannot be less than 2. (\"" + line + "\")");
                }
            }
            else {
                throw new ConfigException(
                        "Line " + lineNo + ": Unknown config key. (\"" + line + "\")");
            }
        }
        if (!xIngested) {
            throw new ConfigException("Missing required key 'x'.");
        }
        if (!yIngested) {
            throw new ConfigException("Missing required key 'y'.");
        }
        return new Config(threads, max);
    }
}
