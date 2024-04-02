package com.example.drainjava.builtins;

import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일에 드레인 상태정보 저장 및 로드
 *
 * @author gunha
 * @version 0.1
 * @since 2024-03-27 오후 4:27
 */
@Component
public class FilePersistence extends AbstractPersistenceHandler {

    @Setter
    private String filePath;

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
