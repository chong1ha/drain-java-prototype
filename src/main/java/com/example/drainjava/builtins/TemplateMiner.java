package com.example.drainjava.builtins;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-03-28 오후 5:56
 */
@Log4j2
public class TemplateMiner {

    private final AbstractPersistenceHandler abstractPersistenceHandler;
    private final TemplateMinerConfig templateMinerConfig;
    @Autowired
    public TemplateMiner(AbstractPersistenceHandler abstractPersistenceHandler, TemplateMinerConfig templateMinerConfig) {
        this.abstractPersistenceHandler = abstractPersistenceHandler;
        this.templateMinerConfig = templateMinerConfig;
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
        templateMinerConfig.load("drain3.ini");
    }

    /**
     * 이전에 저장된 상태 로드
     */
    private void loadState() throws IOException {
        try {
            byte[] state = abstractPersistenceHandler.loadState();
            if (state != null) {
                //
            }
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    /**
     * 새로운 로그 메시지를 받아, 클러스터에 추가
     */
    private void addLogMessage(String logMessage) {
        // TODO: 2024-03-29(금), 17:13 (4) addLogMessage 기능 작성
        // 1. 로그 메시지 마스킹 처리
        // 2. 마스킹된 로그 메시지를 Drain에 전달하여 클러스터에 추가
    }

    /**
     * 기존 클러스터와 로그 메시지를 매칭
     */
    private void match(String logMessage, FullSearchStrategy fullSearchStrategy) {
        // TODO: 2024-03-29(금), 17:7 (4) match 기능 작성
    }

}
