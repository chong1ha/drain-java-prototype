package com.example.drainjava.common;

import com.example.drainjava.common.util.CommonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kang-geonha
 * @version 0.1
 * @since 2024/04/11 12:15 AM
 */
public class CommonUtilTests {

    @Test
    @DisplayName("로그 포맷(문자열)에서 헤더 추출 및 정규표현식 생성")
    public void generateLogFormatRegexTest() throws Exception {
        try {
            //given
            String zookeeperLogFormat = "<Date> <Time> - <Level>  [<Node>:<Component>@<Id>] - <Content>";
            String zookeeperLogMessage = "2015-07-30 23:52:53,800 - INFO  [main:QuorumPeer@933] - minSessionTimeout set to -1";

            List<String> expectedHeaders =new ArrayList<>(Arrays.asList(
                    "Date", "Time", "Level", "Node", "Component", "Id", "Content"
            ));
            String expectedRegex = "^\\Q\\E(?<Date>.*?)\\Q \\E(?<Time>.*?)\\Q - \\E(?<Level>.*?)\\Q  [\\E(?<Node>.*?)\\Q:\\E(?<Component>.*?)\\Q@\\E(?<Id>.*?)\\Q] - \\E(?<Content>.*?)\\Q\\E$";

            //when
            List<String> actualHeaders = new ArrayList<>();
            Pair<Pattern, List<String>> result = CommonUtil.generateLogFormatRegex(zookeeperLogFormat, actualHeaders);
            Pattern actualRegex = result.getFirst();
            String actualRegexStr = actualRegex.toString();
            actualHeaders = result.getSecond();

            //then
            assertEquals(actualHeaders, expectedHeaders);
            assertEquals(actualRegexStr, expectedRegex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}