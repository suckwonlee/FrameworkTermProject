package kr.ac.kopo.lsw.frameworktermproject.repository;

import kr.ac.kopo.lsw.frameworktermproject.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
