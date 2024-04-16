package com.example.drainjava.common.util;

import com.example.drainjava.common.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오후 4:47
 */
public class CommonUtil {

    /**
     * 로그 포맷(문자열)에서 헤더 추출 및 정규표현식 생성
     *
     * @param logFormat 로그포맷 문자열
     * @param headers 헤더
     * @return regex
     */
    public static Pair<Pattern, List<String>> generateLogFormatRegex(String logFormat, List<String> headers) {

        // 정규표현식 빌더
        StringBuilder regexBuilder = new StringBuilder("^");

        // 헤더 추출
        Pattern pattern = Pattern.compile("<([^<>]+)>");
        Matcher matcher = pattern.matcher(logFormat);

        int lastEnd = 0;
        while (matcher.find()) {
            String textBeforeHeader = logFormat.substring(lastEnd, matcher.start());
            regexBuilder.append(Pattern.quote(textBeforeHeader));

            // 헤더 추출
            String header = matcher.group(1);

            // 그룹 추가
            regexBuilder.append("(?<").append(header).append(">.*?)");
            headers.add(header);

            // 마지막 헤더 끝 위치 갱신
            lastEnd = matcher.end();
        }

        // 나머지 부분 처리
        String textAfterHeaders = logFormat.substring(lastEnd);
        regexBuilder.append(Pattern.quote(textAfterHeaders));

        regexBuilder.append("$");
        return new Pair<>(Pattern.compile(regexBuilder.toString()), headers);
    }
}
