package com.example.drainjava.builtins;

import com.example.drainjava.builtins.drain.LogCluster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오전 11:06
 */
@SpringBootTest
public class TemplateMinerTests {

    @Autowired
    private TemplateMiner templateMiner;

    @Autowired
    private FilePersistence filePersistence;

    @Test
    @DisplayName("파이썬에서 사용되는 JSON 키 문자열('json://1')에서 숫자 추출")
    public void extractJsonNumberTest() throws Exception {
        try {
            //given
            String jsonString = "json://11";
            String expectedJsonString = "11";

            //when
            String actualJsonString = TemplateMiner.extractJsonNumber(jsonString);

            //then
            assertEquals(expectedJsonString, actualJsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    @DisplayName("클러스터와 로그메시지 매칭")
    public void matchTest() throws Exception {
        // init
        try {
            String binFilePath = "/Volumes/LOCAL/drain_java_scripts/HDFS_2k_snapshot.bin";
            filePersistence.setFilePath(binFilePath);
            templateMiner.loadState();
        } catch (Exception e) {
           e.printStackTrace();
        }

        try {
            //given
            String logMessage = "PacketResponder 1 for block blk_38865049064139660 terminating";
            String fullSearchStrategy = "never";

            int clusterId = 1;
            int size = 311;
            String[] logTemplate = {
                    "PacketResponder","<*>","for","block","blk","<*>","terminating"
            };
            List<String> log_template_tokens = new ArrayList<>(Arrays.asList(logTemplate));
            LogCluster expectedLogCluster = new LogCluster(log_template_tokens, clusterId);

            //when
            LogCluster actualLogCluster = templateMiner.match(logMessage, fullSearchStrategy);

            //then
            assertEquals(expectedLogCluster.getLogTemplateTokens(), actualLogCluster.getLogTemplateTokens());
            assertEquals(expectedLogCluster.getClusterId(), actualLogCluster.getClusterId());
            assertEquals(size, actualLogCluster.getSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
