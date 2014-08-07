package hu.chengming.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import hu.chengming.util.InMemoryClock;

public class InMemoryClockTest {
    @Test
    public void testTimeMillis() {
        assertEquals(System.currentTimeMillis() - InMemoryClock.getInstance().currentTimeMillis() < 10, true);
    }   

    @Test
    public void testPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.currentTimeMillis();
        }
        long end = System.currentTimeMillis();
        System.out.println("system call : " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            InMemoryClock.getInstance().currentTimeMillis();
        }
        end = System.currentTimeMillis();
        System.out.println("cached call : " + (end - start));

    }
}
