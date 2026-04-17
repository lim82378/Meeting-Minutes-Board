package com.myspringboot.Project1.attendee;

import java.util.List;

import lombok.Data;

@Data
public class AttendeeRequestDTO {
	private Long meetingId;      // 회의_id(FK)
    private List<String> names;  // 선택된 참석자 이름 리스트
}
