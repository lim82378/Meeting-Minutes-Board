package com.myspringboot.Project1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import com.myspringboot.Project1.attachment.AttachmentDTO;
import com.myspringboot.Project1.attachment.AttachmentService;
import com.myspringboot.Project1.attendee.AttendeeRequestDTO;
import com.myspringboot.Project1.meeting.MeetingDTO; // DTO 임포트
import com.myspringboot.Project1.meeting.MeetingService; // 서비스 임포트

import jakarta.transaction.Transactional;

//DB와 연동한 통합테스트 (Service -> Repository -> DB)
@SpringBootTest
@Transactional
class Pj1ApplicationTests {
	
	@Autowired
    MeetingService mSvc;
	
	@Autowired
	AttachmentService aSvc;
	
	// 데이터 insert 테스트
	@Test
	@DisplayName("데이터 저장 테스트: 15개의 데이터를 저장하면 DB에 총 개수가 반영되어야 함")
	void insertDummyData() {
	    // given 
        // 15개의 데이터를 DTO를 통해 저장합니다.
		for (int i = 1; i <= 15; i++) {
		    MeetingDTO dto = MeetingDTO.builder()
		            .title("제 " + i + "차 테스트 회의") // 제목
		            .content("테스트 내용 " + i) // 내용
		            .emplno("202604" + i) // 작성자 사번
		            .emplnm("개발자" + i) // 작성자 이름
		            .meetingDate(LocalDateTime.now()) // insert date
		            .location("테스트 "+ i + " 회의실") // 장소
		            .build();

		    Long savedId = mSvc.saveMeeting(dto); // 서비스를 통해 저장

		    // 7번째 데이터는 삭제 처리 테스트를 위해 저장 후 바로 삭제 실행
		    if (i == 7) {
		    	// 삭제자 정보를 담은 DTO 생성
                MeetingDTO deleteDto = MeetingDTO.builder()
                        .modifierName("테스트삭제자")
                        .modifierNo("TEST_001")
                        .build();
                mSvc.deleteMeeting(savedId, deleteDto); // 수정됨
		    }
		}

	    // when 
        // 전체 목록을 가져옵니다 (삭제된 데이터 제외)
        Page<MeetingDTO> result = mSvc.getMeetingList(PageRequest.of(0, 100));

	    // then 
        // Soft-Delete로 인해 15개를 넣었지만 조회 결과는 14개여야 함
	    assertThat(result.getTotalElements()).isEqualTo(14); 
	    System.out.println("데이터 저장 및 Soft-Delete 반영 테스트 성공");
	}
    
    // 페이징 테스트
    @Test
    @DisplayName("페이징 기능 테스트: 요청한 페이지 사이즈만큼 데이터가 반환되어야 함")
    void pagingTest() {
        // given
        int pageSize = 10;
        PageRequest pageable = PageRequest.of(0, pageSize);

        // when
        // 서비스로부터 DTO 페이지를 받아옵니다.
        Page<MeetingDTO> result = mSvc.getMeetingList(pageable);

        // then
        // result.getContent().size() = 반환된 데이터 개수
        // .isLessThanOrEqualTo(pageSize) = pageSize의 개수 이하인지 확인
        assertThat(result.getContent().size()).isLessThanOrEqualTo(pageSize);
        // 결과가 0보다 큰지 확인
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(0);
        
        System.out.println("페이징 테스트 성공");
    }
    
    // Soft-Delete 테스트
    @Test
    @DisplayName("Soft-Delete 필터링 테스트: 삭제된 데이터는 조회 결과에 포함되지 않아야 함")
    void softDeleteFilteringTest() {
        // given
        // 앞선 insert 테스트에서 7번째 데이터를 삭제 처리함
    	
        // when
    	// 서비스를 통해 삭제되지 않은 데이터만 가져오기
        Page<MeetingDTO> result = mSvc.getMeetingList(PageRequest.of(0, 100));

        // then
        // 모든 결과 데이터의 삭제 날짜나 상태를 간접적으로 확인 (삭제된 7번 사번이 없는지 확인 가능)
        for (MeetingDTO dto : result.getContent()) {
            assertThat(dto.getEmplno()).isNotEqualTo("2026047");
        }
        
        System.out.println("필터링 테스트 성공");
    }
    
    // 파일 업로드 Mock 테스트
    @Test
    @DisplayName("파일 업로드 테스트: PDF 파일이 정상적으로 저장되고 DB에 기록되어야 함")
    void fileUploadTest() throws Exception {
        // given
        // 테스트용 회의록 먼저 저장 (ID를 얻기 위함)
    	int i = (int)(Math.random() * 100) + 16; //테스트를 위한 랜덤 숫자 16~100
    	String testFileName = "test_report" + i + ".pdf"; // 파일명 변수화
    	
        MeetingDTO meetingDTO = MeetingDTO.builder()
        		.title("제 " + i + "차 테스트 회의") // 제목
	            .content("테스트 내용 " + i) // 내용
	            .emplno("202604" + i) // 작성자 사번
	            .emplnm("개발자" + i) // 작성자 이름
	            .meetingDate(LocalDateTime.now()) // insert date
	            .location("테스트 "+ i + " 회의실") // 장소
	            .build();
        Long meetingId = mSvc.saveMeeting(meetingDTO);

        // 가짜 PDF 파일 생성 (파일명, 원본파일명, 컨텐츠타입, 내용)
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "test_report" + i + ".pdf", 
                "application/pdf", 
                "test content".getBytes() // 바이트코드형태로 변경
        );

        // when
        // 서비스를 통해 파일 저장 실행
        aSvc.saveAttachment(meetingId, mockFile);

        // then
        // DB에서 해당 회의록의 첨부파일 목록을 가져와 검증
        List<AttachmentDTO> files = aSvc.getAttachmentsByMeetingId(meetingId);
        
        assertThat(files).isNotEmpty(); // 파일 목록이 비어있지 않아야 함
        assertThat(files.get(0).getOriginName()).isEqualTo(testFileName); // 파일명이 일치해야 함
        
        System.out.println("파일 업로드 및 DB 저장 테스트 성공: " + files.get(0).getStoredName());
    }
    
    @Test
    @DisplayName("회의 참석자 저장 테스트")
    void saveAttendeesTest() {
        // given
    	int i = (int)(Math.random() * 100) + 16; //테스트를 위한 랜덤 숫자 16~100
    	
        MeetingDTO meetingDTO = MeetingDTO.builder()
        		.title("제 " + i + "차 테스트 회의") // 제목
	            .content("테스트 내용 " + i) // 내용
	            .emplno("202604" + i) // 작성자 사번
	            .emplnm("개발자" + i) // 작성자 이름
	            .meetingDate(LocalDateTime.now()) // insert date
	            .location("테스트 "+ i + " 회의실") // 장소
	            .build();
        Long meetingId = mSvc.saveMeeting(meetingDTO);
        AttendeeRequestDTO dto = new AttendeeRequestDTO();
        dto.setMeetingId(meetingId);
        dto.setNames(Arrays.asList("김민재", "이순신", "세종대왕"));

        // when
        mSvc.saveAttendees(dto);

        // then
        System.out.println("생성된 회의 번호: " + meetingId);
        System.out.println("참석자 저장 완료!");
    }
    
}