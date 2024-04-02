package com.example.drainjava.builtins;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-04-01 오전 9:19
 */
public enum EngineType {
    DRAIN("Drain"),
    JACCARD_DRAIN("JaccardDrain");

    /** 사용자 정의 값 */
    private final String value;
    EngineType(String value) {
        this.value = value;
    }

    /**
     * 파라미터 체크
     *
     * @param value
     * @return boolean
     */
    public static boolean isValid(String value) {
        for (EngineType engine : EngineType.values()) {
            // 비교 (대소문자 무시)
            if (engine.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
