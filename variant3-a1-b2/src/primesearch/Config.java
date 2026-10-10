package primesearch;

/**
 *  Holds the data. Config-parsed fields are immutable and should not be changed after being read by every thread.
 */
public record Config (int threadCount, long upperLimit) { }