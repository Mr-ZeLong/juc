package com.zelon.juc;

import java.util.concurrent.*;

public class ThreadPool {
    /*
    线程池
    1. 作用：
    ① 复用线程，避免频繁创建和销毁线程，提升处理效率;
    ② 管理线程，当接口的 TPS非常高，不可能每个请求都创建一个线程来处理，就需要
       通过线程池来管理最大创建线程数、设置阻塞队列、拒绝策略等。

    2. execute() 和 submit() 的区别：
    ① 入参：execute() 只能是 Runnable，submit() 还可以是 Callable
    ② 返回值：execute() 无返回值，submit() 返回 Future 对象，通过 Future.get() 获取返回值
    ③ 异常捕获：execute() 主线程捕获不到异常，只能在子线程捕获异常，submit() 在执行 future.get() 时主线程能够捕获异常，方便统一异常处理
    注意：execute() 方法也可以通过传入 FutureTask 来执行有返回值的线程，因为FutureTask 实现了 Runnable 接口
    本质上，submit 也只是对 execute() 方法的封装

    3. 线程池核心参数：
    ① corePoolSize: 线程池中的核心线程数
    ② maximumPoolSize: 线程池中的最大线程数
    ③ keepAliveTime: 线程的最大空闲时间，超过这个时间可能会被淘汰掉
    ④ workQueue: 存放任务的阻塞队列
    ⑤ RejectedExecutionHandler: 拒绝策略

    4. 为什么要用阻塞队列？
    ① 使用队列是为了缓存未处理的任务
    ② 阻塞能保证并发时队列数据安全，同时让线程存活，复用线程

    5. 线程池提供了哪些拒绝策略？
    ① AbortPolicy: 默认拒绝策略，直接由主线程抛出 RejectedExecutionException 异常，并停止接受新任务
    ② CallerRunsPolicy：主线程执行策略，直接由主线程执行 run() 方法，不由线程池创建新的线程执行，
                        这种方式会阻塞主线程，不适合执行流程复杂的任务
    ③ DiscardPolicy: 丢弃策略，一个空实现，什么都不做，相当于直接丢弃
    ④ DiscardOldestPolicy：丢弃队列最前面的任务，然后将当前任务加入到队列中

    6. 怎么自定义拒绝策略？
    实现 RejectedExecutionHandler 接口，重写 rejectedExecution(), 在创建线程池时传入

    7. 怎么解决冷启动问题？
    使用 prestartAllCoreThreads() 方法，提前启动所有核心线程

    8. 淘汰策略？
    内部的线程没有核心线程和非核心线程的标识，因此，不是说只有那些刚开始创建的所谓核心线程会有淘汰时间。
    当线程池的线程数超过corePoolSize时，所有的线程获取任务时，都会在阻塞队列进行有时间限制的阻塞，
    超过时间限制的线程会通过锁竞争的机制来尝试释放线程，留下的就是所谓的核心线程。
    （线程数没有超过corePoolSize时，则进行无限制的阻塞）

    9. 使用 execute() 执行的时候，线程出现异常，线程会停止吗？
    出现异常，线程会停止，将异常往外抛，然后重新添加一个新的线程到线程池中。
    个人认为这是 官方想保留 UncaughtExceptionHandler 机制，只有往外抛，才能执行这个机制。

     */
    public static void main(String[] args) {
        try (ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>(500))) {

            // 通过 execute() 方法执行线程
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " is running");
            });

            // 通过 submit() 方法执行线程
            Future<Integer> future = executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + " is running");
                return 1 / 0;
            });
            // 此时才会抛出异常
            future.get();

            // 通过 execute() 方法执行 FutureTask 线程, 与上面的 submit() 方法效果一样
            FutureTask<Integer> futureTask = new FutureTask<>(() -> {
                System.out.println(Thread.currentThread().getName() + " is running");
                return 1 / 0;
            });
            executor.execute(futureTask);
            // 获取结果，此时才会抛出异常
            futureTask.get();

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
