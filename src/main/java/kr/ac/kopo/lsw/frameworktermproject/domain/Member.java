package kr.ac.kopo.lsw.frameworktermproject.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인에 사용하는 아이디 (중복 불가)
    @Column(nullable = false, unique = true)
    private String loginId;

    // 연락용 이메일 (중복도 막고 싶으면 unique = true 유지)
    @Column(nullable = false, unique = true)
    private String email;

    // 화면에 표시할 이름
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    // 단순 USER 권한 기본값
    @Column(nullable = false)
    private String role = "USER";
}
