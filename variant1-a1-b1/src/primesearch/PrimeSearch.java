package primesearch;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class where prime search threading logic occurs.
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

    // Gets the range for a singular partition
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

    // Collates all range partitions into a single list
    public static List<Range> splitRange(int x, long y) {
        ArrayList<Range> ranges = new ArrayList<>();

        for (int i = 0; i < x; i++) {
            ranges.add(getRange(i, x, y));
        }
        return ranges;
    }

    // Code to execute per-thread
    private static void searchRange(int workerId, Range range) {
        for (long n = range.start(); n<= range.end(); n++) {
            if (isPrime(n)) {
                String primeLog = "[" + now() + "] worker-" + workerId + ": " + n;
                System.out.println(primeLog); // each thread prints log
            }
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
        int x = config.threadCount();
        long y = config.upperLimit();

        // Log START of worker threads for transparency
        System.out.println("[" + now() + "] START threads=" + x + " limit=" + y);
        List<Range> ranges = splitRange(x, y);

        List<Thread> workers = new ArrayList<>();

        // create threads
        for (int i = 0; i < x; i++) {
            final int id = i;
            final Range myRange = ranges.get(i);

            // create single thread
            Thread t = new Thread(() -> searchRange(id, myRange), "worker-" + id);
            workers.add(t);

            try {
                t.start();
            } catch (OutOfMemoryError e) {
                System.err.println("[OS Refusal : Thread Creation] Could not start worker " + i + " of " + x + "; cannot create thread");
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

        System.out.println("[" + now() + "] END");
    }
}
