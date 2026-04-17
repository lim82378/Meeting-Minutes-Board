package com.myspringboot.Project1.meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myspringboot.Project1.attendee.Attendee;
import com.myspringboot.Project1.attendee.AttendeeRepository;
import com.myspringboot.Project1.attendee.AttendeeRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // @Autowired 대신 final사용 (불변성)
@Transactional(readOnly = true)
public class MeetingService {
	
    private final MeetingRepository mRepo;
    
    private final AttendeeRepository atRepo;
    

    // 회의록 등록 insert (MeetingDTO를 받아 엔티티로 변환 후 저장)
    @Transactional
    public Long saveMeeting(MeetingDTO dto) {
        Meeting meeting = dtoToEntity(dto);
        return mRepo.save(meeting).getMeetingId();
    }

    // 회의록 목록 조회 (삭제되지 않은 데이터만(isNull), 페이징 적용(pageable))
    public Page<MeetingDTO> getMeetingList(Pageable pageable) {
    	// 최신순 정렬 ascending() < 오래된 순
    	Pageable sortedByDesc = PageRequest.of(
    	        pageable.getPageNumber(), 
    	        pageable.getPageSize(), 
    	        Sort.by("createdAt").descending()
    	    );
    	    
	    Page<Meeting> result = mRepo.findByDeleteAtIsNull(sortedByDesc);
	    return result.map(this::entityToDto);
    }

    // 회의록 제목 검색 (삭제되지 않은 데이터 중 검색, 페이징 적용(pageable))
    public Page<MeetingDTO> searchMeetings(String keyword, Pageable pageable) {
    	// 최신순 정렬 ascending() < 오래된 순
    	Pageable sortedByDesc = PageRequest.of(
    	        pageable.getPageNumber(), 
    	        pageable.getPageSize(), 
    	        Sort.by("createdAt").descending()
    	    );

	    Page<Meeting> result = mRepo.findByTitleContainingAndDeleteAtIsNull(keyword, sortedByDesc);
	    return result.map(this::entityToDto);
    }

    // 회의록 상세 조회
    public MeetingDTO getMeeting(Long id) {
        Meeting meeting = mRepo.findByMeetingIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 회의록입니다."));
        return entityToDto(meeting);
    }

    // 회의록 수정 update(파일 제외)
    @Transactional
    public void updateMeeting(Long id, MeetingDTO updateParam) {
        // 내부 상세 조회 로직 활용 (수정을 위해 엔티티가 필요하므로 리포지토리 직접 조회)
        Meeting meeting = mRepo.findByMeetingIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 회의록입니다."));
        
        meeting.setTitle(updateParam.getTitle());
        meeting.setMeetingDate(updateParam.getMeetingDate());
        meeting.setLocation(updateParam.getLocation());
        meeting.setContent(updateParam.getContent());
        
        //수정 기록용
        meeting.setModifierNo(updateParam.getModifierNo());   // 수정자 사번
        meeting.setModifierName(updateParam.getModifierName()); // 수정자 성명
        // update_at은 엔터티의 @UpdateTimestamp에 의해 자동 갱신
    }

    // 회의록 Soft Delete 실행 (실제 삭제 대신 삭제 시간 기록)
    @Transactional
    public void deleteMeeting(Long id, MeetingDTO deleteParam) {
        Meeting meeting = mRepo.findByMeetingIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 회의록입니다."));
        meeting.setDeleteAt(LocalDateTime.now());
        meeting.setModifierName(deleteParam.getModifierName());
    }
    
    // 다중 삭제 기능
    @Transactional
    public void deleteMultipleMeetings(List<Long> ids, String modifierName, String modifierNo) {
        for (Long id : ids) {
            Meeting meeting = mRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회의록이 없습니다. ID: " + id));
            
            // Soft-delete: 상태값 변경
            meeting.setDeleteAt(LocalDateTime.now());
            meeting.setModifierName(modifierName);
            meeting.setModifierNo(modifierNo);
            
            // JPA 감티(Dirty Checking)로 인해 따로 save 호출 안 해도 트랜잭션 종료 시 반영됩니다.
        }
    }
    
    // 회의 참석자 리스트 조회
    @Transactional(readOnly = true)
    public List<String> getAttendeeNames(Long meetingId) {
        // 회의_id 존재 여부 확인
        Meeting meeting = mRepo.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회의가 없습니다."));

        // 이름만 리스트로 변환
        return meeting.getAttendees().stream()
                      .map(Attendee::getName)
                      .collect(Collectors.toList());
    }
    
    // 회의 참석자 추가
    @Transactional
    public void saveAttendees(AttendeeRequestDTO dto) {
        // 회의_id 존재 여부 확인
        Meeting meeting = mRepo.findById(dto.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회의가 존재하지 않습니다."));
        
        // 기존 참석자 덮어쓰기
        atRepo.deleteByMeetingMeetingId(dto.getMeetingId());
        
        // DTO <> 엔티티
        for (String name : dto.getNames()) {
            Attendee attendee = new Attendee();
            attendee.setName(name);
            attendee.setMeeting(meeting); // meeting_id
            
            atRepo.save(attendee);
        }
    }
    
 // --- 변환 메서드 (DTO <-> Entity) ---

    private Meeting dtoToEntity(MeetingDTO dto) {
        return Meeting.builder()
                .meetingId(dto.getMeetingId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .emplno(dto.getEmplno())
                .emplnm(dto.getEmplnm())
                .location(dto.getLocation())
                .meetingDate(dto.getMeetingDate())
                .modifierNo(dto.getModifierNo())
                .modifierName(dto.getModifierName())
                .build();
    }

    private MeetingDTO entityToDto(Meeting meeting) {
        return MeetingDTO.builder()
                .meetingId(meeting.getMeetingId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .emplno(meeting.getEmplno())
                .emplnm(meeting.getEmplnm())
                .location(meeting.getLocation())
                .meetingDate(meeting.getMeetingDate())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                //수정할 때 사용
                .modifierNo(meeting.getModifierNo())
                .modifierName(meeting.getModifierName())
                .build();
    }
}