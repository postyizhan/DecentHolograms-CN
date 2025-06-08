package eu.decentsoftware.holograms.api.utils;

import eu.decentsoftware.holograms.api.utils.collection.DList;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DExecutor {

    private static boolean initialized = false;
    private static ExecutorService service;

    /**
     * Initialize DExecutor. This method will set up ExecutorService for DecentHolograms.
     *
     * @param threads Number of threads to use.
     */
    public static void init(int threads) {
        if (!initialized) {
            AtomicInteger threadId = new AtomicInteger(0);
            service = Executors.newFixedThreadPool(threads, runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("DecentHolograms Thread #" + threadId.incrementAndGet());
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, ex) -> Log.warn("Exception encountered in %s", ex, t.getName()));
                return thread;
            });
            initialized = true;
        }
    }

    /**
     * Shutdown the service immediately.
     */
    public static void shutdownNow() {
        service.shutdownNow();
        initialized = false;
    }

    /**
     * Create new instance of DExecutor that handles scheduled runnables.
     *
     * @param estimate The estimated amount of runnables.
     * @return The new instance.
     * @throws IllegalStateException If the service is not initialized.
     */
    @NonNull
    @Contract("_ -> new")
    public static DExecutor create(int estimate) {
        if (!initialized) {
            throw new IllegalStateException("DExecutor is not initialized!");
        }
        return new DExecutor(service, estimate);
    }

    /**
     * Execute a runnable using the ExecutorService.
     *
     * @param runnable The runnable.
     */
    public static void execute(@NonNull Runnable runnable) {
        service.execute(runnable);
    }

    private final ExecutorService executor;
    private final DList<CompletableFuture<Void>> running;

    DExecutor(@NonNull ExecutorService executor, int estimate) {
        this.executor = executor;
        this.running = new DList<>(estimate);
    }

    /**
     * Schedule a new runnable.
     *
     * @param r The runnable.
     * @return CompletableFuture executing the runnable.
     */
    public CompletableFuture<Void> queue(@NonNull Runnable r) {
        synchronized (running) {
            CompletableFuture<Void> c = CompletableFuture.runAsync(r, executor);
            running.add(c);
            return c;
        }
    }

}
