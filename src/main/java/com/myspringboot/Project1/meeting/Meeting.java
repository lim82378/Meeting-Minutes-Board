package com.myspringboot.Project1.meeting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.myspringboot.Project1.attendee.Attendee;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_meeting")
@Builder
@Getter 
@Setter
@NoArgsConstructor   // 파라미터 없는 기본 생성자
@AllArgsConstructor  // 모든 필드를 포함하는 생성자
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //AUTO_INCREMENT 연동
    @Column(name = "meeting_id", columnDefinition = "BIGINT UNSIGNED") // 양수만 사용
    private Long meetingId; // 회의록 ID (PK, AUTO_INCREMENT) 

    @Column(nullable = false, length = 200)
    private String title; // 회의 제목 

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate; // 회의 일시 

    @Column(length = 100)
    private String location; // 회의 장소 (NULL 허용) 

    @Column(nullable = false, length = 50)
    private String emplno; // 작성자 사번 

    @Column(length = 50)
    private String emplnm; // 작성자 이름 

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 회의 내용 

    @Column(name = "delete_at")
    private LocalDateTime deleteAt; // 삭제일시 (Soft Delete용) 

    @CreationTimestamp // 자동 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 등록일시 (자동 저장) 

    @UpdateTimestamp // 자동 시간 갱신
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시 (자동 갱신) 
    
    //DB에 없는 엔터티(수정 기록용)
    private String modifierNo;   // 수정자 사번
    private String modifierName; // 수정자 이름
    
    // mappedBy : "연관관계의 주인이 내가 아님!" One:나, Many:쟤(=FK)
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true) // 삭제되면 같이 삭제됨(Cascade) 
    private List<Attendee> attendees = new ArrayList<>();
    
}