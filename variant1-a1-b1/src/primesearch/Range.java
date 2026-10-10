package primesearch;

/**
 * Inclusive range [start, end] of numbers. To be assigned to exactly one worker thread after splitRange() is called.
 * @param start first number to test.
 * @param end last number to test (inclusive)
 */
public record Range(long start, long end) { }
