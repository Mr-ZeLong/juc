package com.zelon.juc;

import java.util.LinkedList;
import java.util.UUID;

/*
1. obj.wait() 让线程进入阻塞状态，obj.notify() 唤醒
其中一个调用 obj.wait() 的线程, 也可以使用 obj.notifyAll() 唤醒所有线程。
消息队列中的生产者消费者模型可以通过 wait() 和 notify() 来实现。

2. obj.wait() 和 Thread.sleep() 的区别？
二者都可以让线程进入阻塞状态，但是两者有区别：
① wait() 是 Object 类的方法，sleep() 是 Thread 类的方法，wait() 调用需要依赖 synchronized,
② wait() 释放锁，sleep() 不会释放锁,
③ wait() 等待时间参数是可选的，线程可以转化为 time_waiting/waiting 两种状态, sleep 只能转化为 time_waiting 状态
 */

public class ProducerConsumerModelCase {
    private static final LinkedList<String> que = new LinkedList<>();
    private static final int MAX_SIZE = 10;
    public static void main(String[] args) {

        // 生产者
        for(int i = 0; i < 5; i++){
            Thread producer = getProducer(i);
            producer.start();
        }

        // 消费者
        Thread consumer = new Thread(() -> {
            while(true){
                synchronized(que){
                    if(!que.isEmpty()){
                        String message = que.pollFirst();
                        System.out.println(Thread.currentThread().getName() + " 消费消息：" + message);
                        que.notifyAll(); // 唤醒生产者, 可以生产消息了
                    }else{
                        try {
                            System.out.println("队列已空，消费者先休息");
                            que.wait(); // 阻塞当前线程，并释放锁
                        } catch (InterruptedException e) {
                            que.notifyAll(); // 如果出现异常，则手动唤醒其他线程
                        }
                    }
                }
            }
        });
        consumer.setName("消费者");
        consumer.start();

    }

    private static Thread getProducer(int i) {
        Thread producer = new Thread(() -> {
            while(true){
                synchronized(que){
                    if(que.size() < MAX_SIZE){
                        String message = UUID.randomUUID().toString();
                        que.addLast(message);
                        System.out.println(Thread.currentThread().getName() + " 生产消息：" + message);
                        que.notifyAll(); // 唤醒消费者，让他们继续消费
                    }else{
                        try {
                            System.out.println("队列已饱和，" + Thread.currentThread().getName() + "先休息");
                            que.wait();
                        } catch (InterruptedException e) {
                            que.notifyAll();
                        }
                    }
                }
            }
        });
        producer.setName("生产者" + i);
        return producer;
    }
}