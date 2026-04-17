# 📝 회의록 관리 프로젝트
> **Spring Boot를 활용한 효율적인 회의 데이터 관리 시스템**

---

### 🚀 1. 배포 주소
* **서비스 URL:** [http://43.200.73.126:8080/meet/meetings/list](http://43.200.73.126:8080/meet/meetings/list)

### ✨ 2. 주요 기능
* **회의록 관리:** 목록 조회, 상세 보기, 검색(JPA 페이징 처리)
* **참석자 관리:** 회의별 참석자 등록 및 수정 기능
* **파일 시스템:** 첨부파일 업로드(미리보기 지원) 및 다운로드
* **데이터 보안:** 논리 삭제(Soft Delete)를 통한 데이터 보존성 확보

### 🛠 3. 기술 스택 (Tech Stack)
* **Backend:** Java 17, Spring Boot, JPA, MariaDB, Lombok, Swagger
* * **Frontend:** HTML/CSS, JavaScript, jQuery, Ajax, Bootstrap
* **DevOps:** AWS Lightsail, Docker

### 🔍 4. 트러블 슈팅 (Troubleshooting)
* **초기 설계 미흡:** 참석자 기능 추가로 인한 전체 레이어 수정 경험 → **"설계의 중요성 체감"**
* **변수 네이밍 개선:** 검색어 변수명을 `title`에서 `keyword`로 변경하여 가독성 및 유지보수성 향상
