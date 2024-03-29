package com.example.drainjava.builtins;

import java.io.IOException;

/**
 * (File, Memory, Kafka, Redis, None) Persistence
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-29 오전 10:59
 */
public abstract class AbstractPersistenceHandler {

    /**
     * 현재 상태정보를 저장소에 저장
     *
     * @param state 저장할 상태 (바이트 배열)
     * @throws IOException 입출력 예외
     */
    public abstract void saveState(byte[] state) throws IOException;

    /**
     * 이전에 저장된 상태를 저장소에서 로드
     *
     * @throws IOException 입출력 예외
     * @return 로드된 상태 (바이트 배열)
     */
    public abstract byte[] loadState() throws IOException;

}
