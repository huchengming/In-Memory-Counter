package hu.chengming.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import hu.chengming.util.InMemoryCounter;
import hu.chengming.util.client.InMemoryCounterPersistClient;

public class InMemoryCounterTest {

    @Test
    public void testSingleThread() {
        InMemoryCounterPersistClient client = new EchoClient();
        final InMemoryCounter counter = new InMemoryCounter(client, 5, 1000, 1000);
        for (int i=0; i<100; i++) {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    for (int j=0; j<10000; j++) {
                        double x = Math.random() * 100;
                        int xx = (int)x;
                        counter.increment(String.valueOf(xx));
                    }
                }
            }).start();
        }
        try {
            Thread.sleep(15*1000);
        } catch (Exception e) {

        }
        assertEquals(((EchoClient)client).sum.get(), 1000000);
    }   
}

class EchoClient implements InMemoryCounterPersistClient<String> {
    public java.util.concurrent.atomic.AtomicLong sum = new java.util.concurrent.atomic.AtomicLong(0L);
    public void increment(String key, long value) {
        sum.addAndGet(value);
    }

}
