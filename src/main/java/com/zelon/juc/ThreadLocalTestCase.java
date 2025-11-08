package com.zelon.juc;

import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
ThreadLocal: 线程本地变量，每个线程都有自己的变量副本，线程之间互不影响。
原理：每个 Thread 对象中都有一个 ThreadLocalMap，key = ThreadLocal对象，val = 数据,
     每次对 ThreadLocal 操作时，都会到线程中的 ThreadLocalMap 查找对应的 entry, 对entry 进行 get、set、remove,
     ThreadLocal 生命周期 和 Thread 绑定，数据不需要使用的时候一定要 remove() , 尤其是在线程池中, 否则容易造成内存泄露。
 */


public class ThreadLocalTestCase {
    private final ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1,
            1,
            60, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(500)
    );
    @Test
    public void testThreadLocal() {
        for(int i = 0; i < 5; i++){
            executor.execute(() -> {
                int value = threadLocal.get();
                threadLocal.set(value + 1);
                System.out.println(Thread.currentThread().getName() + ": " + threadLocal.get()); // 1-5
            });
        }

        for (int i = 0; i < 5; i++){
            executor.execute(() -> {
                int value = threadLocal.get();
                threadLocal.set(value + 1);
                System.out.println(Thread.currentThread().getName() + ": " + threadLocal.get()); // 6-10
            });
        }
        // 当使用ThreadLocal时，如果数据不再使用，要调用remove()方法删除，否则可能会造成内存泄露，尤其是使用线程池时。
    }
}
