package kr.ac.kopo.lsw.frameworktermproject.repository;
import java.util.List;
import java.time.LocalDateTime;
import kr.ac.kopo.lsw.frameworktermproject.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByMember_LoginIdOrderByCreatedAtDesc(String loginId);

    List<Diary> findByMember_LoginIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String loginId,
            LocalDateTime start,
            LocalDateTime end
    );
}
