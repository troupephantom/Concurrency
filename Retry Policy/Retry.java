public class Retry {
    private final int maxAttempts;
    private final int maxTotal;
    private final int baseDelay;
    private final Predicate<Throwable>  isTransient;
    private final Random rnd = new Random();

    public <T> T execute(Caller<T> fn){
        long deadline = System.currentTimeMillis() + maxTotal;
        Throwable last = null;

        for(int i = 0;i<maxAttempts;i++){
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;
            try {
                return fn.call();
            } catch (Throwable e) {
                last = e;
                if (!isTransient.test(e)) throw e;
                if (attempt == maxAttempts - 1) break;

                long maxDelay = base.toNanos() * (1L << attempt);
                long delay = (long) (rnd.nextDouble() * maxDelay);
                if (System.nanoTime() + delay > deadline) break;
                Thread.sleep(delay / 1_000_000);
            }
        }
        throw new RetriesExhausted(last);
    }
}