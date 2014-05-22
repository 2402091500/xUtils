package com.lidroid.xutils.util.core;

import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 */
public class PriorityExecutor implements Executor {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 256;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "PriorityExecutor #" + mCount.getAndIncrement());
        }
    };

    private int poolSize;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new PriorityBlockingQueue<Runnable>();
    private static ThreadPoolExecutor threadPoolExecutor;

    public PriorityExecutor() {
        this(CORE_POOL_SIZE);
    }

    public PriorityExecutor(int poolSize) {
        this.poolSize = poolSize;
        threadPoolExecutor = new ThreadPoolExecutor(
                poolSize,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                sPoolWorkQueue,
                sThreadFactory);
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        threadPoolExecutor.setCorePoolSize(poolSize);
    }

    private final LinkedList<Runnable> mTasks = new LinkedList<Runnable>();
    private Runnable mActive;

    @Override
    public synchronized void execute(final Runnable r) {
        Priority priority;
        if (r instanceof PriorityObject) {
            priority = ((PriorityObject) r).priority;
        } else {
            priority = Priority.UI_LOW;
        }
        mTasks.offer(new PriorityRunnable(
                priority,
                new Runnable() {
                    public void run() {
                        try {
                            r.run();
                        } finally {
                            scheduleNext();
                        }
                    }
                }
        ));
        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            threadPoolExecutor.execute(mActive);
        }
    }
}
