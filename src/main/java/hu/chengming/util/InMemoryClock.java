package hu.chengming.util;

public class InMemoryClock {

    private static class Holder {
        private static final InMemoryClock INSTANCE = new InMemoryClock();
    }

    private volatile long rate = 50L;
    private volatile long now = 0L;

    private InMemoryClock() {
        this.now = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    now = System.currentTimeMillis();
                    try {
                        Thread.sleep(rate);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }).start();
    }

    public long currentTimeMillis() {
        return now;
    }

    public long currentTimestamp() {
        return now/1000;
    }

    public static InMemoryClock getInstance() {
        return Holder.INSTANCE;
    }
}
