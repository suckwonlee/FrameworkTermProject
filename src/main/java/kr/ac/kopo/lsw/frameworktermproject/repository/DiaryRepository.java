package kr.ac.kopo.lsw.frameworktermproject.repository;

import kr.ac.kopo.lsw.frameworktermproject.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
