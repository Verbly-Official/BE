# Verbly: 틀릴까 봐 말하지 못했던 영어를, 자연스럽게!

![image]()

<br/>

## 🌍 <span id="프로젝트-소개">프로젝트 소개</span>
>**“AI 1차 교정 + 원어민 2차 첨삭의 2단계 학습 플랫폼"**

많은 한국인 학습자들은
틀릴까 봐 영어 사용을 망설입니다.

버블리는 이 장벽을 낮추기 위해
AI의 빠른 문법 교정과
원어민의 문화적·맥락적 피드백을 결합했습니다.

교정된 문장은 자동으로 개인 라이브러리에 저장되고,
퀴즈와 복습을 통해 장기 기억으로 전환됩니다.

학습은 혼자가 아니라,
글로벌 커뮤니티 속에서 함께 성장하는 경험이 됩니다.

<br/>

## 🚀 <span id="배포-주소">배포 주소</span>
> **🌐 프론트엔드 주소** <br/>
> **⚙️ 백엔드 주소** <br/>

<br/>

## 👫 <span id="팀원-소개">팀원 소개</span>
<div align="center">

| 지니(김현진) | 유니(정세윤) | 파울로(박시윤) | 또치(박소희) | 솜(윤영주) |
|:----------------:|:------------:|:------------:|:-------------:| :-------------: |
| <img src="https://github.com/tellgeniewish.png" width="100"/><br/> | <img src="https://github.com/yunnij.png" width="100"/><br/> | <img src="https://github.com/keypang.png" width="100"/><br/> | <img src="https://github.com/Ddiy0ng.png" width="100"/><br/> | <img src="https://github.com/som141.png" width="100"/><br/> |
| [@tellgeniewish](https://github.com/tellgeniewish) | [@yunnij](https://github.com/yunnij) | [@keypang](https://github.com/keypang) | [@Ddiy0ng](https://github.com/Ddiy0ng) | [@som141](https://github.com/som141) |

</div>

<br/>

## 💻 <span id="사용-기술-스택">사용 기술 스택</span>
| 분류              | 기술 |
|-------------------|------|
| Backend Library  | SpringBoot |
| Language          | Java |
| DataBase          | MySQL |
| Cloud          | AWS |

<img width="1024" height="559" alt="image" src="https://github.com/user-attachments/assets/95b5b7f3-b865-4ac2-9e11-aaa6643f142b" />

<br/>

## 📚 <span id="git-컨벤션">git 컨벤션</span>
### 브랜치 전략
**Git Flow 방식: main ← dev ← feat/닉네임/이슈번호**
- main: 배포 브랜치
- dev: 개발 브랜치
- feat/닉네임/이슈 번호: 기능 개발 브랜치

### 브랜치 명명 규칙 
**브랜치 형식: 브랜치종류/닉네임/이슈번호**
- 브랜치 종류: feat, refactor, bug  등등…

### 커밋 메세지 규칙 
**Gitmoji 사용**

| 이모지 | 타입          | 설명               |
|--------|---------------|--------------------|
| ✨     | Feat          | 새로운 기능 추가    |
| 🐛     | Bug           | 오류 수정          |
| 🔨     | Fix           | 코드 수정          |
| ♻️     | Refactor      | 코드 리팩토링      |
| 🚑     | Hotfix         | 급한 오류 수정 |
| 👷     | Ci         | ci/cd 파이프라인 구축        |

### 커밋 메세지 형식 
```
:이모지: [Feat/Bug/Refactor] Title

ex) ✨[Feat] API 구현
```

<br/>

## 🗂️ <span id="폴더-구조">폴더 구조</span>
```
verbly.spring
├── domain
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   │   ├── request
│   │   └── response
│   ├── converter
│   ├── validator
│   ├── enums
│   └── exception
│
├── global
│   ├── common
│   │   ├── aws
│   │   │   └── s3
│   │   ├── code
│   │   ├── constants
│   │   ├── dto
│   │   │   ├── gemini
│   │   │   └── openai
│   │   ├── entity
│   │   ├── exception
│   │   ├── handler
│   │   ├── response
│   │   └── utils
│   │
│   ├── config
│   │   └── properties
│   │
│   ├── security
│   │   ├── auth
│   │   ├── config
│   │   ├── handler
│   │   ├── jwt
│   │   │   ├── handler
│   │   │   ├── service
│   │   │   └── userinfo
│   │   ├── oauth
│   │   └── utils
│   │
│   └── websocket
│       ├── exception
│       ├── handler
│       ├── interceptor
│       └── utils
```

<br/>
