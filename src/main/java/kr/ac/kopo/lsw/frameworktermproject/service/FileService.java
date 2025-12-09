package kr.ac.kopo.lsw.frameworktermproject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {

    // 작업 디렉터리 기준으로 uploads/diary 에 저장
    private final Path diaryUploadDir = Paths.get("uploads", "diary");

    /**
     * 일기 이미지 파일 저장
     * @param file 업로드된 파일
     * @return 서버에 저장된 파일명(랜덤 UUID 기반)
     */
    public String saveDiaryImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 저장 폴더 없으면 생성
            if (!Files.exists(diaryUploadDir)) {
                Files.createDirectories(diaryUploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex != -1) {
                    extension = originalFilename.substring(dotIndex);
                }
            }

            String randomName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = diaryUploadDir.resolve(randomName);

            // 실제 파일 저장 (임시파일 → 목적지 직접 복사)
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return randomName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 중 오류가 발생했습니다.", e);
        }

    }

    /**
     * 저장된 파일명을 기반으로 브라우저에서 접근할 URL 경로 생성
     * 예: storedFilename = abc.jpg -> /uploads/diary/abc.jpg
     */
    public String buildDiaryImageUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isEmpty()) {
            return null;
        }
        return "/uploads/diary/" + storedFilename;
    }
    public void deleteDiaryImage(String storedFilename) {
        try {
            Path path = Paths.get("uploads/diary").resolve(storedFilename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("이미지 삭제 중 오류 발생: " + storedFilename, e);
        }
    }

}
