package com.example.drainjava.common.util;

/**
 * 문자열 처리 관련
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오후 4:36
 */
public class StringUtil {

    /**
     * 문자열이 비어 있는지 반환하는 메소드<br>
     * -> 주어진 문자열이 null 일 경우 true를 반환함
     *
     * @param str 문자열
     * @return 문자열이 비어 있는지 여부
     */
    public static boolean isEmpty(String str) {

        if(str == null) {
            return true;
        }

        return str.isEmpty();
    }
}
