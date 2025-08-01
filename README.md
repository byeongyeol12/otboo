# sb01-otboo-team04
<div align="center">

<!-- logo -->
<img src="https://codeit.notion.site/image/attachment%3A5c097f1f-4050-4b26-91e5-00de499f9a15%3ACloset_Hanger_(1).png?id=2196fd22-8e8d-803d-bc77-d5ac78ca119f&table=block&spaceId=a29b669d-e680-438e-b18c-08888fc54a21&width=250&freeze=true&userId=&cache=v2" width="100"/>

### 날씨 기반 OOTD 추천 소셜 서비스 옷장을 부탁해 ✅

[<img src="https://img.shields.io/badge/-readme.md-important?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/-notion-blue?style=flat&logo=google-chrome&logoColor=white" />]()
<br/> [<img src="https://img.shields.io/badge/프로젝트 기간-2025.06.23~2025.07.29-green?style=flat&logo=&logoColor=white" />]()

</div> 

## 📖 프로젝트 소개

**OTB(오늘 뭐 입지?)** 는 실시간 날씨 정보를 기반으로 사용자에게 최적의 옷차림을 추천하고, 다른 사용자들과 OOTD(Outfit of the Day)를 공유하며 소통할 수 있는 날씨 기반 OOTD 추천 소셜 서비스입니다.

- 프로젝트 소개
- 프로젝트 화면 구성 또는 프로토 타입
- 프로젝트 API 설계
- 사용한 기술 스택
- 프로젝트 아키텍쳐
- 기술적 이슈와 해결 과정
- 프로젝트 팀원

<br />

## 📖 프로젝트 담당 부분

- 의상등록
- 의상추천
- 어드민 의상 속성 정의

<br />


### 화면 구성
|Screen #1|Screen #2|
|:---:|:---:|
|<img src="" width="400"/>|<img src="" width="400"/>|


<br />

## 🗂️ API 명세
작성한 API는 아래에서 확인할 수 있습니다.

👉🏻 [API 바로보기](https://www.otboo.cloud/swagger-ui/index.html)


<br />

## ⚙️ 기술 스택

### Back‑end
- **Java 17**: 메인 프로그래밍 언어
- **Spring Boot 3**: 애플리케이션 프레임워크
- **Spring Data JPA & Hibernate**: 데이터베이스 ORM
- **QueryDSL**: 동적 쿼리 생성을 위한 라이브러리
- **Spring Security & JWT**: 인증 및 인가
- **Spring Batch**: 날씨 데이터 수집 등 배치 처리
- **PostgreSQL**: 주 데이터베이스
- **Redis**: 캐싱 및 JWT 블랙리스트 관리
- **WebSocket**: 실시간 통신 (DM)
- **SSE**: 실시간 알림

### Infra
- **AWS**: 클라우드 인프라 (EC2, S3, RDS)
- **Docker**: 애플리케이션 컨테이너화

### Tools & Collaboration
- **Git & GitHub**: 버전 관리 및 협업
- **Gradle**: 빌드 및 의존성 관리
- **IntelliJ IDEA**: 통합 개발 환경(IDE)
- **Postman**: API 테스트 도구
- **Notion**: 프로젝트 관리 및 문서화

<br />

## 🛠️ 프로젝트 구조
```text
src/
└── main/
    ├── java/com/codeit/otboo/
    │   ├── OtbooApplication.java
    │
    │   ├── domain/
    │   │   ├── auth/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   └── service/
    │   │   │
    │   │   ├── comment/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   ├── dashboard/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   └── service/
    │   │   │
    │   │   ├── dm/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   ├── follow/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   ├── interest/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   ├── notification/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   └── service/
    │   │   │
    │   │   ├── subscription/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   ├── user/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │
    │   │   └── weather/
    │   │       ├── batch/
    │   │       │   ├── job/
    │   │       │   └── tasklet/
    │   │       ├── controller/
    │   │       ├── dto/
    │   │       ├── mapper/
    │   │       ├── repository/
    │   │       └── service/
    │
    │   └── global/
    │       ├── config/
    │       ├── constants/
    │       ├── error/
    │       │   ├── exception/
    │       │   └── handler/
    │       ├── security/
    │       └── util/
    │
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        ├── application-prod.yml
        ├── schema.sql
        └── static/
```


<br />

## 🤔 기술적 이슈와 해결 과정
- Stream 써야할까?
    - [Stream API에 대하여](https://velog.io/@yewo2nn16/Java-Stream-API)
- Gmail STMP 이용하여 이메일 전송하기
    - [gmail 보내기](https://velog.io/@yewo2nn16/Email-이메일-전송하기with-첨부파일)
- AWS EC2에 배포하기
    - [서버 배포하기-1](https://velog.io/@yewo2nn16/SpringBoot-서버-배포)
    - [서버 배포하기-2](https://velog.io/@yewo2nn16/SpringBoot-서버-배포-인텔리제이에서-jar-파일-빌드해서-배포하기)


<br />


## 💁‍♂️ 프로젝트 팀원

| 이규석 (팀장) | 김상호 | 손동혁 | 공병열 | 김응진 |
|:---:|:---:|:---:|:---:|:---:|
| <img src="https://github.com/impmonzz.png?size=120" width="120"/> | <img src="https://github.com/ghtkdrla.png?size=120" width="120"/> | <img src="https://github.com/sondonghyuk.png?size=120" width="120"/> | <img src="https://github.com/byeongyeol12.png?size=120" width="120"/> | <img src="https://github.com/mmm806.png?size=120" width="120"/> |
| [impmonzz](https://github.com/impmonzz) | [ghtkdrla](https://github.com/ghtkdrla) | [sondonghyuk](https://github.com/sondonghyuk) | [byeongyeol12](https://github.com/byeongyeol12) | [mmm806](https://github.com/mmm806) |
