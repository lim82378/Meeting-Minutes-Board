package com.myspringboot.Project1.attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
	// 회의_id 를 기준으로 모든 참석자 삭제 후 다시 넣기
	void deleteByMeetingMeetingId(Long meetingId);
}
