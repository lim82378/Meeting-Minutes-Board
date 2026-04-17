package com.myspringboot.Project1.meeting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.myspringboot.Project1.attachment.AttachmentDTO;
import com.myspringboot.Project1.attachment.AttachmentService;
import com.myspringboot.Project1.attendee.AttendeeRequestDTO;



@RestController
@RequestMapping("/api/meetings") // 회의록 자원을 다루는 공통 경로
public class MeetingRestController {
    
	@Value("${file.upload-dir}")
	private String uploadPath;
	
    @Autowired
    private MeetingService mSvc;
    
    @Autowired
    private AttachmentService aSvc;
    
    /**
     * 회의록 조회(페이징) 및 검색
     */
    @GetMapping
    public PagedModel<MeetingDTO> getMeetingList(
            @RequestParam(value = "page", defaultValue = "0") int page, 
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<MeetingDTO> resultPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            resultPage = mSvc.searchMeetings(keyword, pageable);
        } else {
            resultPage = mSvc.getMeetingList(pageable);
        }
        
        return new PagedModel<>(resultPage);
    }

    /**
     * 회의록 및 파일 등록
     */
    @PostMapping
    public ResponseEntity<?> register(MeetingDTO meetingDTO, 
                                      @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        
        // 회의록 본문 저장 후 생성된 ID 반환
        Long meetingId = mSvc.saveMeeting(meetingDTO);
        
        // 파일이 존재하면 파일 서비스 호출하여 저장
        if (file != null && !file.isEmpty()) {
            aSvc.saveAttachment(meetingId, file);
        }
        
        return ResponseEntity.ok(Map.of("id", meetingId, "message", "성공"));
    }
    /**
     * 회의록 상세보기
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingDTO> getMeetingDetail(@PathVariable(value = "id") Long id) {
        // 서비스에서 ID로 데이터 조회
        MeetingDTO dto = mSvc.getMeeting(id);
        
        // 데이터가 없을 경우 예외 처리
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * 특정 회의록에 첨부된 파일 정보 가져오기(한개만 가능하지만 설계는 여러개도 가능)
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<AttachmentDTO> getFileByMeetingId(@PathVariable(value = "id") Long id) {
        List<AttachmentDTO> files = aSvc.getAttachmentsByMeetingId(id);
        
        if (files == null || files.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        // 첫 번째 파일만 변환 나중에 여러개의 파일 반환할 때 사용가능
        return ResponseEntity.ok(files.get(0));
    }
    
    /**
     * 다운로드 기능
     */
    @GetMapping("/files/download/{attachId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable(value = "attachId") Long attachId) throws IOException {
        
        AttachmentDTO fileDto = aSvc.getAttachment(attachId); 
        if (fileDto == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(uploadPath, fileDto.getStoredName());
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        String encodedFileName = UriUtils.encode(fileDto.getOriginName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
    
    /**
     * 수정하기
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateMeeting(
            @PathVariable("id") Long id,
            @RequestPart("meeting") MeetingDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {

        mSvc.updateMeeting(id, dto);

        // 새로운 파일이 들어온 경우 (교체)
        if (file != null && !file.isEmpty()) {
            aSvc.replaceAttachment(id, file);
        } 
        // 새로운 파일은 없지만 '삭제' 체크박스를 선택한 경우
        else if (dto.isDeleteFile()) {
            aSvc.deleteAttachmentByMeetingId(id); // 파일 및 DB 정보 삭제 서비스 호출
        }

        return ResponseEntity.ok("수정이 완료되었습니다.");
    }
    
    /**
     * 삭제하기 (Soft Delete)
     * 로그인이 없어 사용자 권한 없이 다 삭제 가능하지만 이름을 적게하여 DB에 삭제한 누군가를 알 수 있음.
     */
    @PutMapping("/{id}/delete")
    public ResponseEntity<String> deleteMeeting(@PathVariable("id") Long id, @RequestBody MeetingDTO dto) {
        mSvc.deleteMeeting(id, dto);
        return ResponseEntity.ok("회의록이 삭제되었습니다.");
    }
    
    /**
     * 다중 삭제 기능 (Soft Delete)
     */
    @PutMapping("/bulk-delete")
    public ResponseEntity<String> bulkDelete(@RequestBody MeetingDTO dto) {
    	mSvc.deleteMultipleMeetings(dto.getIds(), dto.getModifierName(), dto.getModifierNo());
        return ResponseEntity.ok("성공적으로 삭제되었습니다.");
    }
    
    /**
     * 참석자 명단 저장 및 수정
     */
    @PostMapping("/attendees")
    public ResponseEntity<String> saveAttendees(@RequestBody AttendeeRequestDTO dto) {
        mSvc.saveAttendees(dto);
        return ResponseEntity.ok("success");
    }

    /**
     * 특정 회의의 참석자 명단 조회
     */
    @GetMapping("/{meetingId}/attendees")
    public ResponseEntity<List<String>> getAttendees(@PathVariable(value = "meetingId") Long meetingId) {
        List<String> attendees = mSvc.getAttendeeNames(meetingId);
        return ResponseEntity.ok(attendees);
    }
}