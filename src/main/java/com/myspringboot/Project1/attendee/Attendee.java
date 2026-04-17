package com.myspringboot.Project1.attendee;

import com.myspringboot.Project1.meeting.Meeting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_attendee")
@Getter 
@Setter
public class Attendee {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //AUTO_INCREMENT 연동
    @Column(name = "attendee_id")
    private Long attendeeId;

    @Column(name = "name")
    private String name;

    // 회의 엔티티와 연결 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY) // 데이터를 가져올 때 지연 로딩으로 필요할 때 마다 사용할 수 있게 만들어 줌
    @JoinColumn(name = "meeting_id", nullable = false, columnDefinition = "BIGINT UNSIGNED") // DB의 FK 컬럼명, null x, 양수만 사용
    private Meeting meeting;
}
