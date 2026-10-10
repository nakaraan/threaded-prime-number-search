package primesearch;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// to lookup unique characteristics per variant, search "UNIQUE" (case-sensitive, UPPERCASE)

/**
 * <b> VARIANT 3 (A1, B2):  Print immediately + Linear search but threaded divisibility testing per number. </b>
 * <br>
 * Class where prime search threading logic occurs. Employs linear search and threaded divisibility testing for every number.
 * For every candidate number, thread pool divides tasks and searches through every potential divisor.
 * UNIQUE: each task decrements a shared AtomicInteger to verify if search is complete across threads.
 * UNIQUE: individual threads print verified results immediately
 */
public final class PrimeSearch {
    // DateTimeFormatter for timestamp consistency
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }


    /**
     * Checks if there is a divisor for a given candidate key, updates AtomicBoolean and AtomicInteger for synchronization
     * @param number candidate number being checked for divisors
     * @param workerIndex index of worker
     * @param totalThreads totalThreads (x)
     * @param isDivisorFound AtomicBoolean, shared per-number flag, and so every task sees another's write
     * @param remainingCounter AtomicInteger, tracks how many worker threads are left to finish checking candidate number
     * @return boolean. {@code true} if composite (has divisor), {@code false} if prime (no divisor)
     */
    private static boolean findDivisors(long number, int workerIndex, int totalThreads, AtomicBoolean isDivisorFound, AtomicInteger remainingCounter) {
        try {
            for (long d = 2 + workerIndex; d <= number / d; d += totalThreads) {
                if (isDivisorFound.get()) {
                    return false;   // if divisor already found in other threads (flag set), immediately halt search
                }
                if (number % d == 0) {   // set flag after finding a divisor
                    isDivisorFound.set(true);
                    return true;
                }
            }
            return false; // no divisors found in entire range
        } finally {
            if (remainingCounter.decrementAndGet() == 0 && !isDivisorFound.get()) {
              System.out.println("[" + now() + "] thread-" + Thread.currentThread().getId() + ": " + number);
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

        final int x = config.threadCount(); // load values from config
        final long y = config.upperLimit();

        // Log START of worker threads for transparency
        System.out.println("[" + now() + "] START threads=" + x + " limit=" + y);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

        // Fixed pool of x threads
        long keepAliveTime = 0; // unused when core == max
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                x, // corePoolSize
                x, // maximumPoolSize
                keepAliveTime, // keepAliveTime
                TimeUnit.SECONDS,
                workQueue
        );

        try {
            executor.prestartAllCoreThreads();
        } catch (OutOfMemoryError e) {
            System.err.println("[OS Refusal : Thread Creation] Could not fully start workers of " + x + "; cannot create thread");
            System.exit(1);
        }
        try {

            try {
                for (long i = 2; i <= y; i++) { // PER CANDIDATE NUMBER (LONG)

                    AtomicBoolean isDivisorFound = new AtomicBoolean(false);
                    AtomicInteger remaining = new AtomicInteger(x);

                    List<Callable<Boolean>> tasks = new ArrayList<>(x);

                    for (int k = 0; k < x; k++) { // PER TASK
                        final int workerIndex = k;
                        final long number = i;

                        tasks.add(() -> // PER THREAD, FIND DIVISORS
                                findDivisors(number, workerIndex, x, isDivisorFound, remaining)
                        );
                    }

                    List<Future<Boolean>> results = executor.invokeAll(tasks); // submit all tasks

                    for (Future<Boolean> result : results) {
                        result.get(); // rethrow task exception
                    }
                    if (i == y) break; // stop before n++ (prevent overflow)
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // set interrupt flag of thread -> make it transparent to higher program layers
                System.err.println("Interrupted while waiting for workers.");
                System.exit(1);
            } catch (ExecutionException e) {
                System.err.println("Worker failed: " + e.getCause());
                System.exit(1);
            }

            System.out.println("[" + now() + "] END");
        } finally {
            executor.shutdown();

            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
