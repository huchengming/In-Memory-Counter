package hu.chengming.util.client;

public interface InMemoryCounterPersistClient<T> {
    public void increment(T key, long value);
}

