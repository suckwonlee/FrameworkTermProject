package kr.ac.kopo.lsw.frameworktermproject.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    // 원본 파일명
    @Column(nullable = false)
    private String originalFilename;

    // 서버에 저장된 파일명 (UUID 등)
    @Column(nullable = false)
    private String storedFilename;

    private LocalDateTime createdAt;
}
