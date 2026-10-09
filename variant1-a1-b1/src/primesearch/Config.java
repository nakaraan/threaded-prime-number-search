package primesearch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 *  Holds the data. Config-parsed fields are immutable and should not be changed after being read by every thread.
 */
public record Config (int threadCount, long upperLimit) { }