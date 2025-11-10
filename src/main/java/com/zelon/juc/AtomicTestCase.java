package com.zelon.juc;

/*
原子类
1. 原子基本类型: AtomicInteger、AtomicLong、AtomicBoolean
2. 原子数组：AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray
3. 原子引用：AtomicReference、AtomicStampedReference
4. 字段更新类：将引用对象中的成员变量原子化，比如 AtomicIntegerFieldUpdater、AtomicReferenceFieldUpdater
5. 累加器类：LongAdder、LongAccumulator
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.*;

public class AtomicTestCase {
    @Test
    public void atomicIntegerTest() {
        AtomicInteger atomicInt = new AtomicInteger(0);
        System.out.println(atomicInt.getAndIncrement()); // 0
        System.out.println(atomicInt.get()); // 1
        System.out.println(atomicInt.incrementAndGet()); // 2
        System.out.println(atomicInt.compareAndSet(2, 5) + ": " + atomicInt.get()); // true: 5
        System.out.println(atomicInt.compareAndSet(2, 7) + ": " + atomicInt.get()); // false: 5

        // 如果只是一些简单操作，建议使用性能更高的 LongAdder 和 LongAccumulator
        LongAdder longAdder = new LongAdder();
        longAdder.add(1);
        longAdder.increment();
        System.out.println(longAdder.sum()); // 2
        longAdder.add(-2);
        System.out.println(longAdder.sum()); // 0

        // 稍微复杂一些的操作可以使用 LongAccumulator, 可以设定初始值和自定义每次的累计运算规则
        LongAccumulator longAccumulator = new LongAccumulator((x, y) -> x + y * 2, 1);
        longAccumulator.accumulate(3);
        System.out.println(longAccumulator.get()); // 7
    }

    @Test
    public void atomicIntegerArrayTest() {
        int[] arr = new int[]{1, 2, 3};
        AtomicIntegerArray atomicIntArray = new AtomicIntegerArray(arr);

        atomicIntArray.set(0, 5);
        // 原子数组会拷贝原数组，而不是直接在原数组上修改
        System.out.println(atomicIntArray.get(0)); // 5
        System.out.println(arr[0]); // 1

        System.out.println(atomicIntArray.getAndSet(0, 7));
        System.out.println(atomicIntArray.get(0)); // 7

        System.out.println(atomicIntArray.incrementAndGet(0)); // 8

        System.out.println(atomicIntArray.addAndGet(0, 2)); // 10
    }

    @Test
    public void atomicReferenceTest() {
        Product product = new Product("小米17promax", 100);
        AtomicReference<Product> productReference = new AtomicReference<>(product);
        // 使用的是原对象，不是拷贝
        System.out.println(productReference.get() == product);// true

        Product newProduct = new Product("小米17promax", 90);
        productReference.compareAndSet(product, newProduct);
        System.out.println(productReference.get()); // Product(name=小米17promax, stock=90)

        // 如果要更新对象中的成员变量，又不想更新变量类型，则可以使用原子更新类
        // 注意：这里一定要把成员变量声明为 public volatile，否则会报错
        AtomicIntegerFieldUpdater<Product> stockUpdater = AtomicIntegerFieldUpdater.newUpdater(Product.class, "stock");
        System.out.println(stockUpdater.incrementAndGet(newProduct)); // 91
        System.out.println(productReference.get()); // Product(name=小米17promax, stock=91)
    }

}
@Data
@AllArgsConstructor
class Product{
    private String name;
    public volatile int stock;
}