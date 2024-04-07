package com.example.drainjava.builtins.drain;

import com.example.drainjava.common.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오전 10:34
 */
@SpringBootTest
public class DrainBaseTests {

    @Autowired
    private Drain drain;

    @Test
    @DisplayName("주어진 문자열이 숫자를 포함하는지 여부 확인")
    public void hasNumbersTest() throws Exception {
        try {
            //given
            String strWithNum = "asdf12asdgvb";
            String strWithoutNum = "pqowerkq";

            //when
            boolean actualStrWithNum = drain.hasNumbers(strWithNum);
            boolean actualStrWithoutNum = drain.hasNumbers(strWithoutNum);

            //then
            assertTrue(actualStrWithNum);
            assertFalse(actualStrWithoutNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("문자열을 토큰으로 분할")
    public void getContentAsTokensTest() throws Exception {
        try {
            //given
            String content = "PacketResponder 1 for block blk_38865049064139660 terminating";
            List<String> expectedTokens = Arrays.asList(
                    "PacketResponder", "1", "for", "block", "blk", "38865049064139660", "terminating"
            );
            drain.setExtraDelimiters(Collections.singletonList("_")); // 추가 구분 기호

            //when
            List<String> actualTokens = drain.getContentAsTokens(content);

            //then
            assertEquals(expectedTokens, actualTokens);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("유사도 계산")
    public void getSeqDistanceTest() throws Exception {
        try {
            //given
            List<String> seq1 =new ArrayList<>(Arrays.asList(
                    "Receiving", "block", "blk", "<*>", "src:", "<*>", "dest:", "<*>"
            )); // 로그 템플릿

            List<String> seq2 =new ArrayList<>(Arrays.asList(
                    "Receiving", "block", "blk", "-295306975763175640", "src:", "/10.250.9.207:53270", "dest:", "/10.250.9.207:50010"
            )); // 들어오는 새로운 데이터

            Double expectedRetVal = 1.0;
            Integer expectedParamCount = 3;

            //when
            Pair<Double, Integer> result =  drain.getSeqDistance(seq1, seq2, true);
            Double actualRetVal = result.getFirst();  // 8
            Integer actualParamCount = result.getSecond();  // 8

            //then
            assertEquals(expectedRetVal, actualRetVal);
            assertEquals(expectedParamCount, actualParamCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
