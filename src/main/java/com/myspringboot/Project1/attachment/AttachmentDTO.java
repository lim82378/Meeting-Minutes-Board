package com.myspringboot.Project1.attachment;

import lombok.*;
import java.time.LocalDateTime;

@Data                // Getter, Setter, ToString 등을 한 번에 해결
@Builder             // 객체 생성을 편리하게 (MeetingDTO.builder()... 방식)
@NoArgsConstructor   // 파라미터 없는 기본 생성자
@AllArgsConstructor  // 모든 필드를 포함하는 생성자
public class AttachmentDTO {

    private Long attachId;      // 파일 고유 번호
    private Long meetingId;     // 연결된 회의록 번호
    private String originName;  // 사용자가 올린 원래 파일명
    private String storedName;    // 서버에 저장된 파일명 (중복 방지용)
    //private String filePath; >> 보안상 저장경로는 DTO에서 제외
    private Long fileSize;      // 파일 크기
    private LocalDateTime createdAt; // 등록일

}