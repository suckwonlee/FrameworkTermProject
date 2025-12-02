package kr.ac.kopo.lsw.frameworktermproject.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String emotion;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
