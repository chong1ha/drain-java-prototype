package com.example.drainjava.builtins.drain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kang-geonha
 * @version 0.1
 * @since 2024/04/11 11:40 PM
 */
public class SimpleProfilerTests {

    @Test
    @DisplayName("특정 메소드 경과시간 측정")
    public void executeWithProfilingTest() throws Exception {
        try {
            //given
            SimpleProfiler simpleProfiler = new SimpleProfiler();

            String sectionName = "test";
            Runnable mockMethod = () -> {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            //when
            long startTime = System.currentTimeMillis();
            simpleProfiler.executeWithProfiling(sectionName, mockMethod);

            //then
            assertTrue(System.currentTimeMillis() - startTime >= 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
