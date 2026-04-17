package com.myspringboot.Project1.attachment;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.myspringboot.Project1.meeting.Meeting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_attachment")
@Builder
@Getter 
@Setter
@NoArgsConstructor   // 파라미터 없는 기본 생성자
@AllArgsConstructor  // 모든 필드를 포함하는 생성자
public class Attachment {
	//long vs Long의 차이 : 기본형인 long은 초기값이 0 인지 아니면 아직 할당 되지않은 건지 모르기 때문에 객체 타입인 Long을 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //AUTO_INCREMENT 연동
    @Column(name = "attachment_id", columnDefinition = "BIGINT UNSIGNED") // 양수만 사용
    private Long attachmentId; // 첨부파일 ID (PK, AUTO_INCREMENT) 

    @Column(name = "meeting_id", nullable = false, columnDefinition = "BIGINT UNSIGNED") // 양수만 사용
    private Long meetingId; // 회의록 ID (FK) 

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName; // 원본 파일명 

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName; // 저장 파일명

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath; // 파일 저장 경로 

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기 (bytes) 

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 등록일시 (자동 저장) 
}
