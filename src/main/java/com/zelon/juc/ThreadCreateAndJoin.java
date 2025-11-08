package com.zelon.juc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ThreadCreateAndJoin {

    /*
    线程创建的两种方式：
    1. 通过继承 Thread 类，重写 run()
    2. 通过实现 Runnable 接口 并将实现类对象传入到 Thread 类的构造方法中创建线程
    3. 通过 FutureTask 实现 callable 接口，再将 FutureTask 对象传入到 Thread 类构造方法中创建线程,
       为了获取返回值并兼容原来的线程创建方式，FutureTask 实现了 Runnable 接口和 Callable 接口，
       将 Callable 封装到 FutureTask 对象

    注意：Runnable 接口不能获取返回值，callable 可以获取返回值。
    原理：
    Thread 类内部实现了 Runnable 接口，start() 启动线程后，几种方式本质都是调用 Thread 类重写的 run()，不同的是
    ① 继承的方式执行的是我们重写的 Thread run()
    ② 实现 Runnable 方式，调用的是 Runnable 接口的 run() 方法
    ③ 传入实现 callable FutureTask 的方式，也是先调用 Runnable run() 方法，然后转化为调用 FutureTask call()

    t1.join(): 当前线程等待t1线程执行结束后才继续执行，默认一直等，也可以设置超时时间
     */

    public static void main(String[] args) {
        // 1. 通过继承 Thread 类创建线程
        Thread t1 = new Thread() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + " is running");
            }
        };

        t1.setName("t1");
        t1.start();

        // 2. 通过实现 Runnable 接口，将 Runnable 接口实现类对象传入到 Thread 类的构造方法中创建线程
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + " is running");
            }
        });
        t2.setName("t2");
        t2.start();


        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(3000);
                return "t3";
            }
        });
        Thread t3 = new Thread(futureTask);
        t3.setName("t3");
        t3.start();
        try {
            t1.join(3000); // 最多等待 t1 线程3秒
            t2.join(); // 必须等待 t2 线程执行结束
            System.out.println(futureTask.get());// 会阻塞，直到获取返回值
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
