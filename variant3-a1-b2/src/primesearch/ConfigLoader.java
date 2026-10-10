package primesearch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads config.txt file, parses x and y values. Handles invalid String inputs.
 * <p>Format: one {@code key = value} per line, with exactly one {@code int x} and {@code long y}.
 * Only lines starting with {@code x} and {@code y} are valid. Format checks against non-ASCII
 * digits, valid values range from {@code ASCII 0 to 10}.
 *
 * <p>{@code x} is parsed as the {@code numThreads} and {@code y} is parsed as the {@code max} upper limit
 * to search for primes.
 */
public final class ConfigLoader {
    private ConfigLoader() {}
    private static final long MAX_CONFIG_BYTES = 1_048_576; // 1 MB; prevents bloated text files (not in format) from being read

    /**
     * Loads the config and returns a config object containing {@code x threadCount} and {@code y upperLimit}
     * @param path contains path to config file (default {@code config.txt})
     * @return static Config object containing parsed values
     * @throws ConfigException custom exception case for all config-related errors
     */
    public static Config load(Path path) throws ConfigException {
        List<String> lines;
        int threads = 0; // initialize values
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
            if (line.startsWith("#") || line.startsWith("//") || line.isBlank() ) {
                continue; // skip blank lines, comments
            }

            // Verify if format is "x = n" or "y = n" where n is a number
            String[] parts = line.split("=", 2); // Split into "<key>, '=', <value>"
            if (parts.length != 2) { // handle case if x=n not followed
                throw new ConfigException("Line " + lineNo + ": Expected 'key = value'. (\"" + line + "\")");
            }
            String key = parts[0].trim(); // isolates left side of '=' (x || y)
            String value = parts[1].trim(); // isolates right side of '=' (n)

            if (key.equals("x")) {
                if (!xIngested) {
                    xIngested = true;
                } else { // prevent duplicate; runs if x found more than once
                    throw new ConfigException ("Line " + lineNo + ": Duplicate x value found. (\"" + line + "\")");
                }

                try {
                    if (!value.matches("[0-9]+")) { // ensures only valid ASCII 0-9 digits are entered
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
                } else { // prevent duplicate; runs if y found more than once
                    throw new ConfigException("Line " + lineNo + ": Duplicate y value found. (\"" + line + "\")");

                }

                try {
                    if (!value.matches("[0-9]+")) { // ensures only valid ASCII 0-9 digits are entered
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
                throw new ConfigException( // handle unknown characters
                        "Line " + lineNo + ": Unknown config key. (\"" + line + "\")");
            }
        }
        if (!xIngested) { // prevent missing x
            throw new ConfigException("Missing required key 'x'.");
        }
        if (!yIngested) { // prevent missing y
            throw new ConfigException("Missing required key 'y'.");
        }
        return new Config(threads, max); // return complete Config object
    }
}
