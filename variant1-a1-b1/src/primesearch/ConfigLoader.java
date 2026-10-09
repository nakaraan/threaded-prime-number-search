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

    public static Config load(Path path) throws ConfigException {
        List<String> lines;
        int threads = 0;
        long max = 0;

        boolean xIngested = false, yIngested = false;

        // Read file
        try {
        lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ConfigException(
              "Failed to read configuration file: " + path,
                    e
            );
        }


        // Iterate through each line
        for (String line : lines) {
            line = line.trim(); // Strings are immutable, must reassign to itself
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }

            // Verify if format is "x = n" where n is a number
            String[] parts = line.split("=", 2); // Split into "<key>, '=', <value>"
            if (parts.length != 2) { // handle case if x=n not followed
                throw new ConfigException("Invalid line in config: \"" + line + "\"");
            }
            String key = parts[0].trim(); // x
            String value = parts[1].trim(); // n

            if (key.equals("x")) {
                if (!xIngested) {
                    xIngested = true;
                } else { // runs if x found more than once
                    throw new ConfigException ("Duplicate x value found.");
                }

                try {
                    threads = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ConfigException(
                            "Invalid integer in line: \"" + line + "\"", e
                    );
                }
                if (threads <= 0) {
                    throw new ConfigException("x threads cannot be less than 1.");
                }

            }
            else if (key.equals("y")) {
                if (!yIngested) {
                    yIngested = true;
                } else { // runs if y found more than once
                    throw new ConfigException ("Duplicate y value found.");
                }

                try {
                    max = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new ConfigException(
                            "Invalid integer in line: \"" + line + "\"", e
                    );
                }

                if (max <= 1) {
                    throw new ConfigException("y prime limit cannot be less than 2.");
                }
            }
            else {
                throw new ConfigException(
                        "Unknown config key in line : \"" + line + "\""
                );
            }
        }
        if (!xIngested || !yIngested) {
            throw new ConfigException(
                    "Config file has missing x or y values."
            );
        }
        return new Config(threads, max);
    }
}
