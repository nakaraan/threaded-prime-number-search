package primesearch;

/**
 * Holds the data. Config-parsed fields are immutable and should not be changed after being read by every thread.
 * <p> {@code threadCount} parsed from {@code x} in {@code config.txt}.
 * {@code upperLimit} parsed from {@code y} in {@code config.txt}.
 *
 * @param threadCount number of threads
 * @param upperLimit upper bound to search primes
 */
public record Config (int threadCount, long upperLimit) { }