import org.junit.jupiter.api.Test;

public class ThreadSleepInterrupt {
    /*
    1. Thread.sleep() 会阻塞当前线程，并让出 cpu 资源, 一般用于一直执行的守护线程内，避免线程占用过多的 cpu 资源
    2. Thread.yield() 告诉线程调度器，当前线程愿意让出 cpu 资源，至于让不让由线程调度器根据线程的优先级、状态、资源情况来决定。
    3. t1.intercept(): 将当前线程打上停止的标记，并不代表真的停止，需要配合
        t1.interrupted() 和 Thread.interrupted(会将打断标记清楚) 使用，根据打断标记，停止一些长时间执行的线程
    4. t1.setDaemon(true): 将一些长时间执行的线程设置为守护线程，默认非守护线程。守护线程守护开启它的线程，如果原来的主线程停止，则守护线程也停止
       例如JVM的垃圾回收线程，Tomcat服务器处理请求的线程。

     */
    public static void main(String[] args){
        Thread t1 = new Thread(() -> {
            while (true){
                try {
                    System.out.println(Thread.currentThread().getName() + " is interrupted ：" + Thread.currentThread().isInterrupted()); // true
                    // 打断标记不会被清除
                    System.out.println(Thread.currentThread().getName() + " is interrupted ：" + Thread.currentThread().isInterrupted()); // true
                    Thread.sleep(1000); // 长时间不断执行的线程，要设置睡眠时间，避免线程占用过多的cpu资源（cpu时间片）
                } catch (InterruptedException e) {
                    // 当线程被打上停止的标记时睡眠，会抛出 InterruptedException，此时线程会清除打断标记，并继续执行
                    e.printStackTrace();
                    System.out.println(Thread.currentThread().getName() + " is interrupted ：" + Thread.currentThread().isInterrupted());//false
                    Thread.currentThread().interrupt();// 重新设置打断标记
                }
                if(Thread.interrupted()){ // 打断标记会被清除
                    System.out.println(Thread.currentThread().getName() + "线程，被停止");
                    System.out.println(Thread.currentThread().getName() + " is interrupted ：" + Thread.currentThread().isInterrupted());// false
                    return;
                }

                System.out.println(Thread.currentThread().getName() + "线程，执行任务");
            }
        });
        t1.setName("t1");
        t1.setDaemon(true); // 设置为守护线程
        t1.start(); // 启动线程

        t1.interrupt(); // 给线程记上打断标记，不会直接停止线程

        try {
            Thread.sleep(5000); // 主线程睡眠
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("主线程结束"); // 主线程结束，其守护线程也会结束
    }

    @Test
    public void testYield(){
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 执行");
        });
        t1.setName("t1");
        t1.start();
        // 主线程请求让出cpu资源
        Thread.yield();
        System.out.println("主线程执行");
    }
}
