package com.example.drainjava.builtins.drain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오전 10:54
 */
@SpringBootTest
public class LogClusterCacheTests {

    @Test
    @DisplayName("PUT, GET 테스트")
    public void LogClusterCacheTest() throws Exception {
        try {
            //given
            int capacity = 10;
            LogClusterCache cache = new LogClusterCache(capacity);

            int clusterId = 1;
            String key = "json://1";
            String[] valueArr = {
                    "PacketResponder","<:NUM:>","for","block","blk","<:*:>","terminating"
            };
            List<String> log_template_tokens = new ArrayList<>(Arrays.asList(valueArr));
            LogCluster value = new LogCluster(log_template_tokens, clusterId);

            //when
            cache.put(key, value);

            //then
            assertEquals(value, cache.get(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
