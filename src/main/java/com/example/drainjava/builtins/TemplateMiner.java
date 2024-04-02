package com.example.drainjava.builtins;

import com.example.drainjava.builtins.drain.Drain;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.InflaterInputStream;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오후 5:56
 */
@Log4j2
@Component
public class TemplateMiner {

    private final AbstractPersistenceHandler abstractPersistenceHandler;
    private final TemplateMinerConfig templateMinerConfig;
    private final Drain drain;
    @Autowired
    public TemplateMiner(AbstractPersistenceHandler abstractPersistenceHandler, TemplateMinerConfig templateMinerConfig, Drain drain) {
        this.abstractPersistenceHandler = abstractPersistenceHandler;
        this.templateMinerConfig = templateMinerConfig;
        this.drain = drain;
        try {
            // TemplateMinerConfig 초기화
            init();
        } catch (IOException e) {
            log.error("Config file not found: drain3.ini", e);
        }
    }

    /**
     * 초기화
     */
    private void init() throws IOException {
        // TemplateMinerConfig 초기화
        templateMinerConfig.load("src/main/ext/drain/drain3.ini");

        // 엔진값 유효성 검사 (Drain 또는 JaccardDrain)
        if (EngineType.isValid(templateMinerConfig.getEngine()) == false) {
            throw new IllegalArgumentException("Invalid engine: must be either 'Drain' or 'JaccardDrain'");
        }

        // Drain 설정값 세팅
        drain.setDepth(templateMinerConfig.getDepth());
        drain.setSimTh(templateMinerConfig.getSimTh());
        drain.setMaxChildren(templateMinerConfig.getMaxChildren());
        drain.setMaxClusters(templateMinerConfig.getMaxClusters());
//        drain.setExtraDelimiters(templateMinerConfig.get());
//        drain.setParamStr(templateMinerConfig.getDrainParamStr());
//        drain.setParametrizeNumericTokens(templateMinerConfig.isParametrizeNumericTokens());

//        if (abstractPersistenceHandler != null) {
//            loadState();
//        }
    }

    /**
     * 이전에 저장된 상태 로드
     */
    public void loadState() throws IOException {
        try {
            // 상태 로드
            byte[] state = abstractPersistenceHandler.loadState();

            // 예외처리
            if (state == null) {
                return;
            }

            // state Base64 디코딩 및 압축 해제
            if (templateMinerConfig.isSnapshotCompressState() == true) {
                state = decompressState(state);
            }

            // JSON 형식의 상태를 Drain 객체로 파싱
            Drain loadedDrain = parseJsonState(state);
            if (loadedDrain == null) {
                log.warn("Failed to parse loaded state. State may be corrupted.");
            }
        } catch (IOException e) {
            log.error("Error loading saved state", e);
        }
    }

    /**
     * state 압축 해제<br>
     *
     * (1) Base64 디코딩
     * (2) 압축 해제
     *
     * @param compressedState
     * @throws IOException
     * @return byteArray
     */
    private byte[] decompressState(byte[] compressedState) throws IOException {

        // Base64 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(compressedState);

        // 압축 해제하는데 사용되는 InflaterInputStream 객체 생성
        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(decodedBytes))) {
            // 압축 해제된 데이터를 담는 곳
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;
            // 압축 해제된 데이터 읽고, outputStream 에 쓰기
            while ((length = inflaterInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // 바이트배열로 반환
            return outputStream.toByteArray();
        }
    }

    /**
     * 바이트 배열 -> 문자열 -> Drain 객체
     *
     * @param state JSON 형식의 상태를 담은 byte 배열
     * @return Drain 객체로 파싱된 상태
     */
    private Drain parseJsonState(byte[] state) {
        Gson gson = new Gson();
        return gson.fromJson(new String(state, StandardCharsets.UTF_8), Drain.class);
    }

    /**
     * 새로운 로그 메시지를 받아, 클러스터에 추가
     */
    private void addLogMessage(String logMessage) {}

    /**
     * 기존 클러스터와 로그 메시지를 매칭
     */
    private void match(String logMessage, FullSearchStrategy fullSearchStrategy) {}
}
