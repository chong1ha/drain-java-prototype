package com.example.drainjava.common.util;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-05 오전 9:58
 */
public class EnumUtil {

    /**
     * 주어진 이름의 enum 이 존재하는지
     *
     * @return 유효하면 true
     */
    public static <E extends Enum<E>> boolean isValid(Class<E> enumClass, String name) {

        // enum class null값 확인
        if (enumClass == null) {
            throw new IllegalArgumentException("Enum class cannot be null.");
        }

        // 확인할 enum 의 이름 빈값 확인
        if (StringUtil.isEmpty(name)) {
            return false;
        }

        // 존재 여부 확인
        try {
            Enum.valueOf(enumClass, name);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
