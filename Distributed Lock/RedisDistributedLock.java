
import java.util.Collections;
import java.util.UUID;

public class RedisDistributedLock {

    private final StringRedisTemplate redisTemplate;

    private static final String RELEASE_SCRIPT = """
        if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
        else
            return 0
        end
        """;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String acquireLock(String key, int expiryTime, int waitTime){
        String token = UUID.randomUUID().toString();

        long deadline = System.currentTimeMillis() + waitTime;

        while (System.currentTimeMillis() < deadline) {

            Boolean success = redisTemplate.opsForValue().setIfAbsent(key,token,Duration.ofMillis(expiryTime));

            if (Boolean.TRUE.equals(success)) {
                return token;
            }

            try {
                Thread.sleep(50); // simple backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }

    public boolean realeaseLock(String key, String token){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(RELEASE_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,Collections.singletonList(key),token);

        return result != null && result == 1;
    }
}