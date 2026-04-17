package com.myspringboot.Project1.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    // 특정 회의록(meeting_id)에 연결된 첨부파일 목록 조회(FK 사용)
    List<Attachment> findByMeetingId(Long meetingId);
}