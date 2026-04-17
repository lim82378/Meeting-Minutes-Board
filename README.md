1. 회의록 관리 프로젝트 - Spring Boot를 활용한 효율적인 회의 데이터 관리 시스템 

2. 주요 기능 (핵심 위주) 
① 회의록 목록 조회 및 검색
② 참석자 등록 및 수정 기능
③ 첨부파일 업로드(미리보기) 및 다운로드
④ 논리 삭제(Soft Delete) 기반의 데이터 

3.관리기술 스택 (Tech Stack)
Backend: Java 17, Spring Boot, JPA, MariaDB, Lombok, Swagger
Frontend: HTML/CSS, JavaScript, jQuery, Ajax, Bootstrap
DevOps: AWS Lightsail, Docker

4. 서버 주소: http://43.200.73.126:8080/meet/meetings/list

5. 트러블 슈팅 
초기 설계 미흡으로 인한 레이어 전체 수정 경험 (설계의 중요성 체감)
직관적인 변수 네이밍(keyword)으로 가독성 개선
