package kr.ac.kopo.lsw.frameworktermproject.domain;

import jakarta.persistence.*;
import lombok.*;


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

    private String url;
}

