package com.zelon.juc;

import org.openjdk.jol.info.ClassLayout;

/*

一、synchronized 在 JDK 1.6 之前直接使用 操作系统的 管程（monitor）重量级锁，需要用户态内核态切换，存在阻塞，效率低。
因此，引入了 锁升级机制（锁信息记录在对象头中），升级过程如下：
1. 锁对象创建时，对象头中记录锁状态为无锁状态
2. 第一次sync加锁时，对象头中记录锁状态为偏向锁，锁记录在对象头中，其中记录了线程ID，
   后续如果一直是这个线程，则直接访问，不需要 CAS 操作。
3. 如果有多个线程交替获取锁，即 一个线程获取锁之后，接下来只有一个线程等待获取锁，则将锁升级为轻量锁
4. 如果多个线程并发获取锁，即 同一时刻有多个线程获取锁，则升级为重量级锁，即 monitor 重量锁.


二、偏向锁在 JDK15 之后已经被废弃, 废弃原因：
偏向锁涉及的初衷是假设一个线程一直持有锁，降低这个线程获取锁的开销，
但是目前大多都是高并发场景，这个假设很难成立，优化效果不大。
同时，高并发场景下，多次锁撤销成本过高（每次撤销需要在安全点暂停所有线程，遍历堆栈，检查锁记录，更新锁记录到对象头），
不如直接 使用 CAS 轻量级锁，避免了频繁锁撤销的开销。

三、轻量级在没有并发安全的情况下可以降到 无锁状态，但是重量级锁无法降级，即便后续没有了并发安全情况。
 */

public class SyncLockUpgrade {
    public static void main(String[] args) throws InterruptedException {

        Object obj = new Object();
        System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // non-biasable

        System.out.println("--------------------------------");

        synchronized (obj) {
            System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // thin lock
        }

        System.out.println("--------------------------------");

        Thread t1 = new Thread(() -> {
            synchronized (obj) {
                System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // thin lock
            }
        });
        t1.start();

        t1.join();

        System.out.println("--------------------------------");

        System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // non-biasable

        System.out.println("--------------------------------");

        Thread t2 = new Thread(() -> {
            synchronized (obj) {
                System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // fat lock
            }
        });

        Thread t3 = new Thread(() -> {
            synchronized (obj) {
                System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // fat lock
            }
        });

        t2.start();
        t3.start();

        t2.join();
        t3.join();

        System.out.println("--------------------------------");

        System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // fat lock

        System.out.println("--------------------------------");

        synchronized (obj) {
            System.out.println(ClassLayout.parseInstance(obj).toPrintable()); // fat lock
        }


    }
}
