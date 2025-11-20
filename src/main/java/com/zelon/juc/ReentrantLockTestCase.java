package com.zelon.juc;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
ReentrantLock 和 synchronized 的区别?
synchronized 是 Java 关键字，由 JVM 通过 C++ 实现，ReentrantLock 是 Java 类，二者都是可重入锁，
但是 ReentrantLock 更加灵活，提供了很多功能：
① 可以查看当前锁是否被其他或者当前线程获取
② 获取锁可以不用一直阻塞，获取不到就马上返回false, 或者等待设定的时间，等待期间可以被中断
② 可以自主设置 锁的公平性（默认非公平锁），synchronized 只能是非公平
③ 可以设置 读写锁 来提高读多写少场景的吞吐量。

为什么默认是非公平锁？
在一些场景，一个线程需要多次获取同一个锁，那么 CPU 会出于性能考量，一直让这个线程优先获取锁，
这样就可以减少线程上下文切换次数以及 CPU 空闲时间，提升整体吞吐量。

非公平锁存在哪些问题？
非公平锁虽然能提高整体的吞吐量，但是存在线程饥饿问题，即 一个线程迟迟没有获取到锁，严重影响响应时间。

怎么决定使用公平策略还是非公平策略？
除非不允许有少部分的线程饥饿问题或者每次线程获取锁后执行时间很长，
不然从整体性能角度来讲，非公平锁在高并发场景下，吞吐量会更高，优先使用非公平锁

ReentrantLock.lock() 原理？
底层主要通过 AQS 同步器来实现的，
公平锁使用公平同步器，非公平锁使用非公平同步器，
①公平锁：不管当前同步器中的锁是否被获取（通过同步器中的 state == 0 判断），都会判断是否需要排序（阻塞队列是否有线程在等待），
如果不需要排队，则通过 CAS 获取锁，否则通过 CAS 将线程加入到同步器的同步队列尾，然后 park(),
队列中前一个线程释放锁后，会 unpark() 唤醒当前线程
②非公平锁：不管有没有人排队，直接通过两次 CAS 获取锁(state)，
如果获取不到则通过 CAS 将线程节点加入到同步器的阻塞队列尾，然后 park()

AQS 同步器原理？
AQS 内部组成：
① 将线程封装为一个个节点（独占/共享），每个节点封装了线程信息，比如线程ID，线程状态，线程等待时间。
   这些节点使用指针连接起来，串成一个双向同步队列。
② 使用 state 标识锁资源，state == 0 表示锁可用，大于 0 表示 锁被获取的次数，实现可重入。
③ 记录 独占锁 的持有线程

AQS 是基于模板方法模式的，ReentrantLock，Semaphore，ReentrantReadWriteLock，SynchronousQueue，FutureTask 等
皆是基于 AQS 实现的，通过实现以下方法，可以实现不同的同步器：
isHeldExclusively()：该线程是否正在独占资源。只有用到 condition 才需要去实现它。
tryAcquire(int)：独占方式。尝试获取资源，成功则返回 true，失败则返回 false。
tryRelease(int)：独占方式。尝试释放资源，成功则返回 true，失败则返回 false。
tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0 表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int)：共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回 true，否则返回 false。
这些方法都是 protected (并没有在 AQS 具体实现，而是直接抛出异常) 而不是 abstract 方法,
这样子类只需要实现自己关心的抽象方法即可，不用全部实现一遍,
比如 信号 Semaphore 只需要实现 tryAcquire 方法而不用实现其余不需要用到的模版方法：
 */

public class ReentrantLockTestCase {
    private ReentrantLock reentrantLock = new ReentrantLock();

    @Test
    public void testReentrant() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            int i = 3;
            @Override
            public void run() {
                doSomething();
            }

            private void doSomething() {
                if(i <= 0) return;
                try {
                    reentrantLock.lock();
                    i--;
                    doSomething();
                } finally {
                    // 锁几次就可以解几次
                    reentrantLock.unlock();
                }
            }
        });
        t1.start();

        t1.join();

        System.out.println(reentrantLock.isLocked()); // false 判断锁是否被某一个线程持有
        System.out.println(reentrantLock.isHeldByCurrentThread()); // false 判断锁是否被当前线程持有
        reentrantLock.lock();
        reentrantLock.lock();
        reentrantLock.unlock();

        System.out.println(reentrantLock.isLocked()); // true
        System.out.println(reentrantLock.isHeldByCurrentThread()); // true
        reentrantLock.unlock();

        System.out.println(reentrantLock.isLocked()); // false

    }


    @Test
    public void testTryLockInterruptibly(){

        Thread t1 = new Thread(() -> {
            reentrantLock.lock(); // 阻塞式获取锁, 不会被打断
            try {
                System.out.println(Thread.currentThread().getName() + " 获取到锁");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                // 记住，一定要先判断，否则可能会抛异常
                if(reentrantLock.isHeldByCurrentThread())
                    reentrantLock.unlock();
            }
        });
        t1.setName("t1");
        t1.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Thread t2 = new Thread(() -> {
            try {
                // 获取锁的等待过程可以被打断
                reentrantLock.lockInterruptibly();
                System.out.println(Thread.currentThread().getName() + " 获取到锁");
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " 获取锁等待时, 被打断");
            } finally {
                if(reentrantLock.isHeldByCurrentThread())
                    reentrantLock.unlock();
            }
        });
        t2.setName("t2");
        t2.start();

        Thread t3 = new Thread(() -> {
            // 不设置时间，获取不到就马上返回 false，不会阻塞
            if(reentrantLock.tryLock()){
                System.out.println(Thread.currentThread().getName() + " 获取到锁");
                reentrantLock.unlock();
            }else{
                System.out.println(Thread.currentThread().getName() + " 获取锁失败"); // ✔
            }
        });
        t3.setName("t3");
        t3.start();

        Thread t4 = new Thread(() -> {
            try {
                // 只在特定的时间内获取锁，超时则返回 false，可以被打断
                if(reentrantLock.tryLock(5, TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread().getName() + " 获取到锁");
                }else{
                    System.out.println(Thread.currentThread().getName() + " 获取锁失败");
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " 获取锁等待时, 被打断"); // ✔
            }
        });
        t4.setName("t4");
        t4.start();

        t2.interrupt();
        t4.interrupt();
    }


    @Test
    public void testFairLock(){
        reentrantLock = new ReentrantLock(true); // 设置为公平锁
        Runnable task = () -> {
            /*
            默认为非公平锁：
            t1 获取到锁
            t1 获取到锁
            t1 获取到锁
            t2 获取到锁
            t2 获取到锁
            t2 获取到锁
            ----------
            公平锁结果：
            t1 获取到锁
            t2 获取到锁
            t1 获取到锁
            t2 获取到锁
            t1 获取到锁
            t2 获取到锁
             */
            for(int i = 0; i < 3; i++){
                reentrantLock.lock();
                System.out.println(Thread.currentThread().getName() + " 获取到锁");
                reentrantLock.unlock();
            }
        };

        Thread t1 = new Thread(task);
        t1.setName("t1");

        Thread t2 = new Thread(task);
        t2.setName("t2");

        t1.start();
        t2.start();
    }

    @Test
    public void testReadWriteLock(){
        // 获取读写锁
        ReentrantReadWriteLock locker = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = locker.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = locker.writeLock();

        Runnable readTask = () -> {
            readLock.lock();
            System.out.println(Thread.currentThread().getName() + " 获取到读锁, 时间：" + LocalDateTime.now());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            readLock.unlock();
        };

        // 两个读锁获取的时间接近，说明：读锁是共享锁，可以同时获取
        new Thread(readTask, "t1").start();
        new Thread(readTask, "t2").start();

        Runnable writeTask = () -> {
            writeLock.lock();
            System.out.println(Thread.currentThread().getName() + " 获取到写锁, 时间：" + LocalDateTime.now());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            writeLock.unlock();
        };

        // ① 第一个获取到写锁的线程与前面读锁的线程差了5秒
        // ② 第二个写锁 和 第一个写锁 时间也差了 5 秒
        // ③ 最后一个读锁 和 第二个写锁 时间相差了 5 秒
        // 说明：写锁是排他锁，只允许一个线程获取，要等其他线程的读锁和写锁都释放了才能获取到
        new Thread(writeTask, "t3").start();
        new Thread(writeTask, "t4").start();
        Thread t5 = new Thread(readTask, "t5");
        t5.start();

        try {
            t5.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }



}
