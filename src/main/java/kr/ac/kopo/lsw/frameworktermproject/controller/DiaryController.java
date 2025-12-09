package kr.ac.kopo.lsw.frameworktermproject.controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import kr.ac.kopo.lsw.frameworktermproject.domain.Diary;
import kr.ac.kopo.lsw.frameworktermproject.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 일기 작성 화면
     * GET /diary/write
     */
    @GetMapping("/write")
    public String writeForm() {
        return "diary/write";
    }

    /**
     * 일기 작성 처리
     * POST /diary/write
     */
    @PostMapping("/write")
    public String writeDiary(@RequestParam("title") String title,
                             @RequestParam("content") String content,
                             @RequestParam(value = "emotion", required = false) String emotion,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             Principal principal) {

        String loginId = principal.getName();
        diaryService.createDiary(loginId, title, content, emotion, image);

        // 저장 후 내 일기장으로 이동
        return "redirect:/diary/my";
    }
    @GetMapping("/my")
    public String myDiary(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(value = "search", required = false)
            String searchFlag,   // 검색 버튼 눌림 여부

            Model model,
            Principal principal
    ) {
        String loginId = principal.getName();
        List<Diary> diaries;

        LocalDate today = LocalDate.now();

        // =============================
        // 1) 검색 버튼을 누르지 않은 경우 → 전체보기
        // =============================
        if (searchFlag == null) {
            diaries = diaryService.getDiariesForUser(loginId);

            // UI 기본값은 오늘(검색 아님)
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);

            model.addAttribute("diaries", diaries);
            return "diary/my";
        }

        // =============================
        // 2) 검색 버튼 눌림 → 검색 실행
        // =============================
        if (startDate == null) startDate = today;
        if (endDate == null) endDate = today;

        // 날짜 역전 시 자동 보정
        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        diaries = diaryService.getDiariesForUserInRange(loginId, startDate, endDate);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("diaries", diaries);

        return "diary/my";
    }

    /**
     * 일기 상세 보기
     * GET /diary/{diaryId}
     */
    @GetMapping("/{diaryId}")
    public String diaryDetail(@PathVariable("diaryId") Long diaryId,
                              Principal principal,
                              Model model) {

        String loginId = principal.getName();
        Diary diary = diaryService.getDiary(loginId, diaryId);

        model.addAttribute("diary", diary);
        return "diary/detail";
    }


    /**
     * 일기 삭제
     * POST /diary/{diaryId}/delete
     */
    @PostMapping("/{diaryId}/delete")
    public String deleteDiary(@PathVariable("diaryId") Long diaryId,
                              Principal principal) {

        String loginId = principal.getName();
        diaryService.deleteDiary(diaryId, loginId);

        return "redirect:/diary/my";
    }

    /**
     * 수정 화면
     * GET /diary/{id}/edit
     */
    @GetMapping("/{diaryId}/edit")
    public String editForm(@PathVariable("diaryId") Long diaryId,
                           Principal principal,
                           Model model) {

        String loginId = principal.getName();
        Diary diary = diaryService.getDiary(loginId, diaryId);

        // 오늘 작성한 일기만 수정 허용
        if (!diary.getCreatedAt().toLocalDate().equals(LocalDate.now())) {
            throw new IllegalStateException("오늘 작성한 일기만 수정할 수 있습니다.");
        }

        model.addAttribute("diary", diary);
        return "diary/edit";
    }

    /**
     * 수정 처리
     * POST /diary/{id}/edit
     */
    @PostMapping("/{diaryId}/edit")
    public String editDiary(@PathVariable("diaryId") Long diaryId,
                            @RequestParam("title") String title,
                            @RequestParam("content") String content,
                            @RequestParam(value = "emotion", required = false) String emotion,
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            Principal principal) {

        String loginId = principal.getName();
        diaryService.updateDiary(loginId, diaryId, title, content, emotion, image);

        return "redirect:/diary/" + diaryId;
    }

}


