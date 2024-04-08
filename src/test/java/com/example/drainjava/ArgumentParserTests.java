package com.example.drainjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author kang-geonha
 * @version 0.1
 * @since 2024/04/07 1:17 AM
 */
@SpringBootTest
public class ArgumentParserTests {

    @Autowired
    private ArgumentParser argumentParser;

    @Test
    @DisplayName("Program Argument 기본 세팅")
    public void init() throws Exception {
        try {
            // 실행 시 필요한 옵션들
            String[] inputArgs = {
                    "--i", // 추론모드로 설정
                    "--s", // 스냅샷 활성화
                    "--f=/Volumes/LOCAL/drain_java_scripts/HDFS_2k_snapshot.bin",   // 스냅샷 바이너리 파일 경로
                    "/Volumes/LOCAL/drain_java_scripts/2k_dataset/HDFS/HDFS_2k.log" // 로그 데이터 파일 경로
            };
            ApplicationArguments args = new DefaultApplicationArguments(inputArgs);

            // 프로그램 실행
            argumentParser.run(args);
        } catch (Exception e) {
        }
    }
}
