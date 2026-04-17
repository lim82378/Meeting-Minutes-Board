package com.myspringboot.Project1.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
	
	// Soft Delete : DeleteAtIsNull을 사용해서 Null인 경우에만(삭제되지 않은) 데이터를 가져오게 설정함 
	//Page 사용 이유 : List로 데이터를 가져오기보다 Page로 페이징 처리하면서 가져오기 위함(10개 단위로 설정함)
    // 삭제되지 않은 회의록 목록 조회 (페이징(pageable) 적용)
    Page<Meeting> findByDeleteAtIsNull(Pageable pageable);

    // 제목으로 검색 (삭제되지 않은 데이터 중 검색, 페이징(pageable) 적용)
    Page<Meeting> findByTitleContainingAndDeleteAtIsNull(String keyword, Pageable pageable);

    // 상세 조회 (삭제되지 않은 데이터만 확인)
    Optional<Meeting> findByMeetingIdAndDeleteAtIsNull(Long meetingId);
}