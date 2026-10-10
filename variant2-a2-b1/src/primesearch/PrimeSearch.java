package primesearch;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// to lookup unique characteristics per variant, search "UNIQUE" (case-sensitive, UPPERCASE)

/**
 * <b> VARIANT 2 (A2, B1):  Wait for all threads -> print + Straight division of search range. </b>
 * <br>
 * Class where prime search threading logic occurs. Custom trial division algorithm was selected for simplicity.
 * Handles Long overflow via employing {@code i <= n / i} instead of {@code i * i <= n}.
 */
public final class PrimeSearch {
    // DateTimeFormatter for timestamp consistency
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    // Checks if given value is prime
    public static boolean isPrime(long n) {

        if (n < 2) return false; // do not allow negatives, 0, or 1
        if (n == 2) return true; // 2 is always prime
        if (n % 2 == 0) return false;

        for (long i = 3; i <= n / i; i += 2) { // start at 3
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the range for a singular partition.
     * @param index current threadIndex
     * @param x total number of threads
     * @param y upper bound to search primes
     * @return Range object containing a unique range for one worker thread
     */
    public static Range getRange(int index, int x, long y) {
        // threads -> x, max -> y for mathematical brevity
        long total, base, remaining, size, start, end;

        total = y - 1; // numbers to search
        base = total / x; // every thread has at least this many
        remaining = total % x; // every 0..rem-1 gets one extra each

        size = base;
        if (index < remaining) {
            size = size + 1;
        }
        start = 2 + index * base + Math.min(index, remaining);
        end = start + size - 1;

        return new Range(start, end);
    }

    /**
     * Partitions ranges, collates into a single List object
     * @param x total number of threads
     * @param y upper bound to search primes
     * @return List<Range> object containing partitioned set of ranges
     */
    public static List<Range> splitRange(int x, long y) {
        ArrayList<Range> ranges = new ArrayList<>();

        for (int i = 0; i < x; i++) {
            ranges.add(getRange(i, x, y));
        }
        return ranges;
    }

    /**
     * Searches range for all known primes.
     * UNIQUE: Each worker will add to its OWN list (no printing, no shared list) to be joined at the end
     * @param range Range object to be populated by individual thread
     * @param primes List<Long> object to be populated with found primes
     */
    private static void searchRange(Range range, List<Long> primes) {
        for (long n = range.start(); n <= range.end(); n++) {
            if (isPrime(n)) {
                primes.add(n);
            }
            if (n == range.end()) break; // stop before n++ (prevent overflow)
        }
    }

    public static void main(String[] args) {
        String configPath = (args.length > 0) ? args[0] : "config.txt"; // config default, allow for custom config for testing

        // Load config
        Config config;
        try {
            config = ConfigLoader.load(Path.of(configPath));
        } catch (ConfigException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
            return;
        }
        int x = config.threadCount(); // load values from config
        long y = config.upperLimit();

        // Log START of worker threads for transparency
        System.out.println("[" + now() + "] START threads=" + x + " limit=" + y);
        List<Range> ranges = splitRange(x, y);


        List<Thread> workers = new ArrayList<>();

        // ---- CHANGE from variant1: Build listOfLists in advance; to pass/retrieve individual lists later to/from threads
        List<List<Long>> listOfLists = new ArrayList<>(x);
        for (int i = 0; i < x; i++) {
            listOfLists.add(new ArrayList<>());
        }

        // Create threads
        for (int i = 0; i < x; i++) {
            final int id = i; // made final to use as lambda parameter
            final Range myRange = ranges.get(i);

            // Create single thread
            final List<Long> myPrimes = listOfLists.get(i);
            Thread t = new Thread(() -> searchRange(myRange, myPrimes), "worker-" + id);
            workers.add(t);

            try {
                t.start();
            } catch (OutOfMemoryError e) {
                System.err.println("[OS Refusal : Thread Creation] Could not start worker " + id + " of " + x + "; cannot create thread");
                System.exit(1);
            }
        }

        try {
            for (Thread t : workers) {
                t.join(); // wait for every thread
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // set interrupt flag of thread -> make it transparent to higher program layers
            System.err.println("Interrupted while waiting for workers.");
            System.exit(1);
        }

        // ---- UNIQUE: Combine all into one StringBuilder, then print once (e.g. 2, 3, 5, 7 instead of a println per prime).
        // ------- [happens-before] : join() called above guarantees all worker writes are visible here.
        StringBuilder sb = new StringBuilder();
        for (List<Long> primes : listOfLists) {   // worker order 0..x-1 = ascending ranges -> output already sorted
            for (long p : primes) {
                sb.append(p).append(", ");
            }
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 2); // remove final trailing comma + space (", ")
        }
        System.out.println(sb); // print output containing primes

        System.out.println("[" + now() + "] END");
    }
}
