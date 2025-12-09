package kr.ac.kopo.lsw.frameworktermproject.service;
import java.util.List;
import kr.ac.kopo.lsw.frameworktermproject.domain.Diary;
import kr.ac.kopo.lsw.frameworktermproject.domain.DiaryImage;
import kr.ac.kopo.lsw.frameworktermproject.domain.Member;
import kr.ac.kopo.lsw.frameworktermproject.repository.DiaryImageRepository;
import kr.ac.kopo.lsw.frameworktermproject.repository.DiaryRepository;
import kr.ac.kopo.lsw.frameworktermproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    /**
     * 일기 + (선택) 이미지 저장
     */
    @Transactional
    public void createDiary(String loginId,
                            String title,
                            String content,
                            String emotion,
                            MultipartFile image) {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인한 사용자를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        Diary diary = Diary.builder()
                .member(member)
                .title(title)
                .content(content)
                .emotion(emotion)
                .createdAt(now)
                .updatedAt(now)
                .build();

        diaryRepository.save(diary);

        // 이미지가 있는 경우 파일 저장 + DiaryImage 연동
        if (image != null && !image.isEmpty()) {
            String storedFilename = fileService.saveDiaryImage(image);

            DiaryImage diaryImage = DiaryImage.builder()
                    .diary(diary)
                    .originalFilename(image.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .createdAt(now)
                    .build();

            diaryImageRepository.save(diaryImage);
        }
    }

    @Transactional(readOnly = true)
    public List<Diary> getDiariesForUser(String loginId) {
        return diaryRepository.findByMember_LoginIdOrderByCreatedAtDesc(loginId);
    }

    /**
     * 일기 삭제
     * - 작성자 본인만 삭제 가능
     */
    @Transactional
    public void deleteDiary(Long diaryId, String loginId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        // 작성자 검증
        if (diary.getMember() == null ||
                diary.getMember().getLoginId() == null ||
                !diary.getMember().getLoginId().equals(loginId)) {
            throw new IllegalStateException("다른 사용자의 일기는 삭제할 수 없습니다.");
        }

        // ==============================
        // 1) 실제 이미지 파일 삭제
        // ==============================
        if (diary.getImages() != null && !diary.getImages().isEmpty()) {
            diary.getImages().forEach(img -> {
                String stored = img.getStoredFilename();
                if (stored != null && !stored.isEmpty()) {
                    fileService.deleteDiaryImage(stored); // 실제 파일 삭제
                }
            });
        }

        // ==============================
        // 2) Diary + DiaryImage DB 삭제
        // ==============================
        diaryRepository.delete(diary);
    }

    @Transactional(readOnly = true)
    public List<Diary> getDiariesForUserOnDate(String loginId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return diaryRepository.findByMember_LoginIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                loginId, start, end
        );
    }
    @Transactional(readOnly = true)
    public List<Diary> getDiariesForUserInRange(String loginId,
                                                LocalDate startDate,
                                                LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return diaryRepository.findByMember_LoginIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                loginId, start, end
        );
    }

    /**
     * 일기 수정
     * - 본인 글인지 확인
     * - 이미지 교체 시 기존 파일 삭제 후 새 파일 저장
     */
    @Transactional
    public void updateDiary(String loginId,
                            Long diaryId,
                            String title,
                            String content,
                            String emotion,
                            MultipartFile newImage) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        // 본인 글인지 검증
        if (!diary.getMember().getLoginId().equals(loginId)) {
            throw new IllegalStateException("다른 사용자의 일기는 수정할 수 없습니다.");
        }

        // 오늘 작성한 일기만 수정 가능
        if (!diary.getCreatedAt().toLocalDate().equals(LocalDate.now())) {
            throw new IllegalStateException("오늘 작성한 일기만 수정할 수 있습니다.");
        }

        // 기본 텍스트 수정
        diary.setTitle(title);
        diary.setContent(content);
        diary.setEmotion(emotion);
        diary.setUpdatedAt(LocalDateTime.now());

        // ==================
        // 이미지 수정 처리
        // ==================
        if (newImage != null && !newImage.isEmpty()) {

            // 기존 이미지 삭제
            if (diary.getImages() != null && !diary.getImages().isEmpty()) {
                DiaryImage old = diary.getImages().get(0);
                fileService.deleteDiaryImage(old.getStoredFilename());
                diaryImageRepository.delete(old);
            }

            // 새 이미지 저장
            String storedFilename = fileService.saveDiaryImage(newImage);

            DiaryImage diaryImage = DiaryImage.builder()
                    .diary(diary)
                    .originalFilename(newImage.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .createdAt(LocalDateTime.now())
                    .build();

            diaryImageRepository.save(diaryImage);
        }
    }

    /**
     * 단일 일기 조회 (상세보기)
     * - 본인 글인지 검증 포함
     */
    @Transactional(readOnly = true)
    public Diary getDiary(String loginId, Long diaryId) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!diary.getMember().getLoginId().equals(loginId)) {
            throw new IllegalStateException("다른 사용자의 일기에 접근할 수 없습니다.");
        }

        return diary;
    }

}


