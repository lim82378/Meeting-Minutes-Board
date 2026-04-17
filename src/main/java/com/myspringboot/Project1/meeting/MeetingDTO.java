package com.myspringboot.Project1.meeting;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data                // Getter, Setter, ToString 등을 한 번에 해결
@Builder             // 객체 생성을 편리하게 (MeetingDTO.builder()... 방식)
@NoArgsConstructor   // 파라미터 없는 기본 생성자
@AllArgsConstructor  // 모든 필드를 포함하는 생성자
public class MeetingDTO {

    private Long meetingId;      // 회의 고유 번호
    private String title;        // 회의 제목
    private String content;      // 회의 내용
    private String emplno;       // 작성자 사번
    private String emplnm;       // 작성자 이름
    private String location;     // 회의 장소
    private LocalDateTime meetingDate; // 회의 일시

    // DB에서 자동 생성됨으로 조회용으로 남겨놓기
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    //기록용
    private String modifierNo;
    private String modifierName;
    
    private boolean deleteFile; // 파일 삭제 여부 추가
    
    private List<Long> ids; // 다중 삭제할 ID 리스트 추가
}