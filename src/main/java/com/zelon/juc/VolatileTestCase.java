package com.zelon.juc;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
volatile
1. 为什么要有 volatile ?
在多线程环境下访问共享变量场景下，缓存数据不一致和指令重排等产生的可见性、有序性问题。
JMM 提供了 volatile 关键字来解决。

2. 缓存一致性协议是什么？
CPU缓存一致性协议是为了解决CPU缓存和主内存的数据一致性问题，当多个CPU操作共享数据时，会将数据缓存在CPU缓存中，
只要一个CPU核心修改了数据，那么数据将会马上通过总线同步到主内存中，其他CPU核心监控总线，
发现数据被修改，会将自身缓存中的数据标记为失效，后续会重新从主内存中获取数据。
（JMM 实现了缓存一致性原则。）
注意：不同的硬件和操作系统平台，实现缓存一致性协议的方案可能不同。

3. 指令重排是什么？
CPU处理器、JIT 编译器 会遵循 as-if-serial 语义（不能改变单线程的执行结果），会直接运行当前有能力立即执行的后续指令，
虽然打乱了顺序，但是能够避开获取下一条指令所需数据时造成的等待，提升了程序运行效率。
但是在多线程环境访问共享数据的场景下，指令重排会比较复杂，可能会导致数据不一致。
因此，各个平台和系统都提供了多线程场景下指令重排的优化方案。

4. JMM 是什么？
由于不同硬件环境下指令重排序的规则不尽相同，Java则指定了一套统一的规范——JMM
用来定义多线程环境中 Java 程序如何访问和操作共享变量，屏蔽平台差异性，保证线程安全。
其中定义了 volatile、synchronized 等关键字 和 happens-before 原则
（多线程的内存可见性原则，定义了两个操作之间必须存在的“先发生”关系）
① 锁 规则: 对同一个锁的解锁操作，必然发生在后续同一个锁的加锁操作之前。
② volatile 规则: 对同一个volatile变量的写操作，先于后续的读操作。
③ 线程启动规则: 主线程在执行线程B的start()方法之前修改了共享变量，则线程B执行start()时，该修改对线程B可见。
...

5. volatile 怎么保证可见性和有序性？
底层是在 使用 volatile 变量的时候，加上 lock 前缀指令, 这个指令的作用：
① 一个线程修改了 volatile 变量时，会马上将修改后的值写入主内存中，其他线程
  监听到工作内存中变量被修改，就会将缓存中的数据标记为失效，重新从主内存中获取数据。
② 提供内存屏障功能，使 lock 前后指令不能重排
 */

public class VolatileTestCase {
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,
            30,
            1000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1000));

    boolean isPrepared = false;
    @Test
    public void visibilityTest() throws InterruptedException {

        executor.execute(() -> {
            // 不加 volatile 之前, 不会感知到数据变化
            while (!isPrepared) {
                // todo
            }
            System.out.println("task done");
        });

        Thread.sleep(2000);

        executor.execute(() -> {
            System.out.println("preparing ...");
            System.out.println("prepare done");
            // 不加 volatile 之前, isPrepared 修改不会马上写入主内存，只能等到线程结束后或者其他情况才写入主内存。
            // 加入 volatile 之后，isPrepared 的修改会马上写入主内存中，并通知其他线程工作内存中的数据失效，重新从主内存中获取数据。

            isPrepared = true;
        });

        // 没加 volatile 之前，第一个线程会一直循环，因为 工作内存中的数据没有
        executor.awaitTermination(1000, TimeUnit.SECONDS);
    }

    private int a, b, x, y;
    @Test
    public void OrderlinessTest() {
        HashSet<String> resultSet = new HashSet<>();

        while(resultSet.size() < 4){
            // 两个线程都可能发生指令重排
           executor.execute(() -> {
               a = y; // 3
               x = 1; // 1
           });
           executor.execute(() -> {
               b = x; // 4
               y = 1; // 2
           });
           resultSet.add("a=" + a + ",b=" + b);
            System.out.println(resultSet);
        }
    }


}
