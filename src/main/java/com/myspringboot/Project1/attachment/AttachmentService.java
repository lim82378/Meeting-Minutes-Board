package com.myspringboot.Project1.attachment;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // final 사용(불변성)
public class AttachmentService {

    // 설정 파일에 적힌 경로를 자동으로 가져오기(서버 배포 시 application.properties만 수정)
    @Value("${file.upload-dir}")
    private String uploadPath;
    
    private final AttachmentRepository aRepo;
    
    // 특정 회의록의 모든 첨부파일 리스트 가져오기(여러개의 첨부파일 대비)
    public List<AttachmentDTO> getAttachmentsByMeetingId(Long meetingId) {
        List<Attachment> entities = aRepo.findByMeetingId(meetingId);
        // Entity 리스트를 DTO 리스트로 변환하여 반환
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
    
    // 첨부파일 저장하기
    @Transactional
    public void saveAttachment(Long meetingId, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return;

        // 파일 형식 체크 (PDF만 허용) 시스템의 안정성을 위해 서버에서도 체크
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
        }

        // 파일 용량 체크 (10MB 제한) 시스템의 안정성을 위해 서버에서도 체크
        long maxSize = 10 * 1024 * 1024; // 10MB를 바이트로 계산
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        
        // 폴더 생성 로직 만약에 폴더가 없다면 "C:/meeting_uploads/"
        File folder = new File(uploadPath);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }

        // 파일명 변환 (중복 방지를 위해 UUID 사용)
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + ".pdf"; //.pdf 파일로 생성

        // 물리적 파일 저장
        File targetFile = new File(uploadPath, storedName); // 주소 경로에 / 가 없어도 자바에서 운영체제에 따라 알아서 주소에 /넣어줌 (안정성)
        file.transferTo(targetFile);

        // DB에 정보 저장 (빌더 패턴 사용)
        //.filePath(targetFile.getAbsolutePath()) > .filePath(storedName)
        Attachment attachment = Attachment.builder()
                .meetingId(meetingId)
                .originalName(originalName)
                .storedName(storedName)
                .filePath(storedName)
                .fileSize(file.getSize())
                .build();
        //시간은 자동 저장
        
        aRepo.save(attachment);
    }
    
    // 없으면 DB에 null
    public AttachmentDTO getAttachment(Long attachId) {
        return aRepo.findById(attachId)
                .map(this::entityToDto)
                .orElse(null); // 없으면 null 반환 (컨트롤러에서 예외처리 가능)
    }
    
    //없으면 DB에 null
    public AttachmentDTO getAttachmentByMeetingId(Long meetingId) {
        List<Attachment> entities = aRepo.findByMeetingId(meetingId);
        if (entities.isEmpty()) return null;
        
        return entityToDto(entities.get(0));
    }
    
    //파일 교체용 메서드
    @Transactional
    public void replaceAttachment(Long meetingId, MultipartFile file) throws Exception {
        // 기존 파일 목록 조회
        List<Attachment> oldFiles = aRepo.findByMeetingId(meetingId);
        
        // 기존 물리 파일 삭제 (용량 관리 및 안정성)
        for (Attachment old : oldFiles) {
        	//File physicalFile = new File(old.getFilePath());
        	File physicalFile = new File(uploadPath, old.getStoredName());
            if (physicalFile.exists()) {
                physicalFile.delete(); 
            }
            aRepo.delete(old); // DB에서도 삭제 (교체니까 Hard Delete)
        }
        
        // 새 파일 저장
        this.saveAttachment(meetingId, file);
    }
    
    //파일 삭제
    @Transactional
    public void deleteAttachmentByMeetingId(Long meetingId) {
        // 1. 해당 회의록에 연결된 모든 파일 정보 조회 (List로 변경)
        List<Attachment> oldFiles = aRepo.findByMeetingId(meetingId);

        // 2. 리스트가 비어있지 않다면 반복문으로 모두 삭제
        if (oldFiles != null && !oldFiles.isEmpty()) {
            for (Attachment old : oldFiles) {
                // 물리적 파일 삭제
                //File physicalFile = new File(old.getFilePath());
            	File physicalFile = new File(uploadPath, old.getStoredName());
                if (physicalFile.exists()) {
                    physicalFile.delete();
                }
                // DB 레코드 삭제
                aRepo.delete(old);
            }
        }
    }
    
    // --- 변환 메서드 (Entity -> DTO) ---
    private AttachmentDTO entityToDto(Attachment entity) {
        return AttachmentDTO.builder()
                .attachId(entity.getAttachmentId())
                .meetingId(entity.getMeetingId())
                .originName(entity.getOriginalName()) 
                .storedName(entity.getStoredName())   
                .fileSize(entity.getFileSize())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}