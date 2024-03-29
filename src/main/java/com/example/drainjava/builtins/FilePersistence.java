package com.example.drainjava.builtins;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author gunha
 * @version 0.1
 * @since 2024-03-27 오후 4:27
 */
public class FilePersistence extends AbstractPersistenceHandler {

    private final String filePath;
    @Autowired
    public FilePersistence(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 상태정보를 파일에 저장
     *
     * @param state
     */
    @Override
    public void saveState(byte[] state) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, state);
    }

    /**
     * 파일(.json)에 작성된 상태정보 로드
     *
     * @return Bytearray
     */
    @Override
    public byte[] loadState() throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
}
