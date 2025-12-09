package kr.ac.kopo.lsw.frameworktermproject.service;

import kr.ac.kopo.lsw.frameworktermproject.domain.Member;
import kr.ac.kopo.lsw.frameworktermproject.dto.JoinRequest;
import kr.ac.kopo.lsw.frameworktermproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean register(JoinRequest req) {

        // 아이디 중복
        if (memberRepository.existsByLoginId(req.getLoginId())) {
            return false;
        }

        // 이메일 중복
        if (memberRepository.existsByEmail(req.getEmail())) {
            return false;
        }

        Member member = Member.builder()
                .loginId(req.getLoginId())
                .email(req.getEmail())
                .name(req.getName())
                .password(encoder.encode(req.getPassword()))
                .role("USER")
                .build();

        memberRepository.save(member);
        return true;
    }
}
