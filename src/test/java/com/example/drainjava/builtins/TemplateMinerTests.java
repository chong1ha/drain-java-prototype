package com.example.drainjava.builtins;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오전 11:06
 */
@SpringBootTest
public class TemplateMinerTests {

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
}
