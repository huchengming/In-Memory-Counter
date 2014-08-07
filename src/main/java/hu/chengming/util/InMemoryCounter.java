package hu.chengming.util;

import hu.chengming.util.client.InMemoryCounterPersistClient;
import hu.chengming.util.InMemoryClock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCounter<T> {
    private final ConcurrentHashMap<T, CounterNode> map;
    private final ConcurrentHashMap<T, ReentrantReadWriteLock> locks;

    private final InMemoryCounterPersistClient client;
    private final long max_size;
    private final long max_ttl; // ttl in seconds
    private final long max_value;

    private Runnable persist_thread = null;

    public InMemoryCounter(InMemoryCounterPersistClient client, long max_ttl, long max_size, long max_value) {
        this.client = client;
        this.max_size = max_size;
        this.max_ttl = max_ttl;
        this.max_value = max_value;

        map = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();

        persist_thread = new PersistThread();
        new Thread(persist_thread).start();
    }

    public long increment(T key) {
        return this.increment(key, 1L);
    }

    public long increment(T key, long interval) {
        long value = -1L;

        if (locks.contains(key) == false) {
            locks.putIfAbsent(key, new ReentrantReadWriteLock());
        }

        locks.get(key).readLock().lock();

        try {
            map.putIfAbsent(key, new CounterNode());
            CounterNode element = map.get(key);
            element.timestamp = InMemoryClock.getInstance().currentTimeMillis();
            value = element.value.addAndGet(interval);
        } finally {
            locks.get(key).readLock().unlock();
        }

        return value;
    }

    class CounterNode {
        public AtomicLong value;
        public volatile long timestamp;

        public CounterNode() {
            this.value = new AtomicLong(0L);
            this.timestamp = InMemoryClock.getInstance().currentTimeMillis();
        }
    }

    class PersistThread implements Runnable {

        @Override
        public void run() {
            while(true) {
                for (Map.Entry<T, CounterNode> entry:map.entrySet()) {
                    T key = entry.getKey();
                    CounterNode element = entry.getValue();
                    long value = element.value.longValue();
                    if (element.timestamp + max_ttl * 1000 <= InMemoryClock.getInstance().currentTimeMillis()) {
                        // too old
                        locks.get(key).writeLock().lock();
                        try {
                            map.remove(key);
                        } finally {
                            locks.get(key).writeLock().unlock();
                        }
                    } else {
                        // just do deduct
                        element.value.addAndGet(-1 * value);
                    }

                    client.increment(key, value);
                }

                try {
                    Thread.sleep(max_ttl * 1000);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
