package com.ruoyi.common.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DocNoGenerator 单元测试
 * 验证格式、前缀处理及并发下的唯一性
 */
public class DocNoGeneratorTest {

    public static void main(String[] args) throws InterruptedException {
        testPrefixHandling();
        testConcurrencyUniqueness();
    }

    private static void testPrefixHandling() {
        System.out.println("--- 测试前缀处理 ---");
        System.out.println("Input: ABC -> Output: " + DocNoGenerator.nextDocNo("ABC"));
        System.out.println("Input: ABCDE -> Output: " + DocNoGenerator.nextDocNo("ABCDE"));
        System.out.println("Input: null -> Output: " + DocNoGenerator.nextDocNo(null));
        System.out.println("Input: AB -> Output: " + DocNoGenerator.nextDocNo("AB"));
    }

    private static void testConcurrencyUniqueness() throws InterruptedException {
        System.out.println("\n--- 测试高并发下的唯一性 ---");
        int threadCount = 50;
        int iterations = 1000;
        int totalExpected = threadCount * iterations;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> generatedIds = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        generatedIds.add(DocNoGenerator.nextDocNo("TEST"));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("线程数: " + threadCount);
        System.out.println("每线程生成数: " + iterations);
        System.out.println("理论生成总数: " + totalExpected);
        System.out.println("实际去重后总数: " + generatedIds.size());

        if (generatedIds.size() == totalExpected) {
            System.out.println(">>> 结果: 唯一性校验通过！无重号。");
        } else {
            System.err.println(">>> 结果: 唯一性校验失败！存在重号。");
        }
    }
}
