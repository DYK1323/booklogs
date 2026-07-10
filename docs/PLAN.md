# 독서 기록 안드로이드 앱 (Booklogs) — 구현 계획

## Context

사용자는 병렬 독서(여러 책을 동시에 읽는 습관)를 기록하기 위한 안드로이드 앱을 원한다. 클라우드/계정 없이 기기 내장 저장소만 사용하는 것이 핵심 제약이다. 요구사항 요약:

1. 그날그날 읽은 페이지 수를 책 구분 없이 합산해 보여주는 그래프 (병렬 독서가 핵심이므로 "오늘 총 몇 페이지 읽었나"가 가장 중요한 지표)
2. 책별 진행률(현재 페이지/전체 페이지, %) 표시
3. 간편한 책 등록: 바코드 스캔 → ISBN으로 온라인 조회(제목/저자/표지 자동완성) 또는 제목 검색, 오프라인 시 수동 입력
4. 간편한 진행률 기록: 진행 중인 책 선택 → 현재 페이지 입력 → 자동으로 델타 계산해 기록
5. 책 페이지 촬영 → OCR(한글+영어)로 텍스트 인식 → 원하는 부분 선택 후 인용구로 저장
6. 책별 상세 조회: 메타데이터, 진행 이력, 인용구 목록, 독후감 목록
7. 독후감은 책 하나에 여러 개 누적 가능 (재독 시마다 새 독후감)

레포는 현재 빈 상태(커밋 없음)이므로 그린필드로 전체 앱을 설계/구현한다. 빌드 샌드박스에는 Android SDK/에뮬레이터가 없으므로(JDK 21 + Gradle만 존재), 검증은 Gradle 컴파일 + JVM 단위 테스트로 가능한 범위까지 수행하고, 나머지(Compose UI, CameraX, ML Kit 실기기 동작)는 사용자가 직접 기기에서 확인해야 함을 계획에 명시한다.

사용자가 이미 확정한 결정:
- OCR 언어: 한글 + 영어 모두 지원 (ML Kit `text-recognition` Latin + `text-recognition-korean` 둘 다 사용)
- 바코드/제목 조회: 온라인 API로 메타데이터 자동완성 사용, 오프라인/실패 시 수동 입력으로 폴백. 독서 기록 데이터 자체는 기기에만 저장(Room DB만 사용, 서버/계정 없음). 메타데이터 API는 **카카오 책 검색 API 우선 + Google Books 보조**(카카오가 한국 도서 표지/정보 커버리지가 더 좋음; 카카오는 실패 시 Google Books로 폴백, 둘 다 실패하면 수동 입력)
- 첫 화면(대시보드)은 책장처럼 표지를 늘어놓고, 각 표지에 dim 처리 + 원형(도넛) 진행률 오버레이를 씌우는 비주얼
- 진행률 기록은 페이지 번호 직접 입력뿐 아니라, 책 페이지를 촬영해 OCR로 페이지 번호를 자동 인식해 채워주는 방식도 지원

## 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| 언어/UI | Kotlin + Jetpack Compose + Material 3 | 단일 Activity, 최신 표준 |
| 아키텍처 | MVVM: Compose UI → ViewModel → Repository → Room DAO | |
| DI | 수동 DI (`AppContainer`, Hilt 미사용) | 단일 모듈, 리포지토리 5개 내외라 Hilt/KSP 애노테이션 프로세싱 오버헤드가 불필요. 에뮬레이터 없는 샌드박스에서 생성 코드 런타임 검증이 안 되므로 리스크 최소화 |
| 로컬 DB | Room (KSP) | 유일한 데이터 저장소, 클라우드 없음 |
| 비동기 | Kotlin Coroutines + Flow | DAO가 `Flow<List<T>>` 반환, 반응형 UI |
| 카메라 | CameraX | 바코드 스캔 + 페이지 촬영 공용 |
| 바코드 인식 | ML Kit Barcode Scanning (온디바이스), EAN-13 | ISBN-13은 EAN-13 포맷 |
| OCR | ML Kit Text Recognition v2: Latin + Korean 모델 둘 다, 온디바이스 | 클라우드 미사용 요구사항 충족 |
| 메타데이터 조회 | Retrofit + OkHttp. **카카오 책 검색 API**(`GET /v3/search/book`, ISBN/제목 검색 둘 다 지원) 우선 조회, 실패/결과없음 시 **Google Books API**(`q=isbn:{isbn}` / `q=intitle:{query}`, 키 불필요)로 폴백 | 카카오가 한국 도서 표지·정보 품질이 더 좋음. 카카오는 REST API 키가 필요(카카오 디벨로퍼스에서 무료 발급 후 `local.properties`/`BuildConfig`에 보관, 저장소에 커밋하지 않음). Google Books는 키 없이 국제 도서 폴백용 |
| 이미지 로딩 | Coil (표지 썸네일) | |
| 차트 | Compose `Canvas`로 직접 구현하는 막대그래프 (외부 차트 라이브러리 미사용) | 요구사항이 "일별 합산 막대그래프" 하나뿐이라 Vico 등 의존성 추가보다 ~100줄 커스텀 Canvas가 더 가볍고, 데이터 버켓팅 로직은 순수 Kotlin 함수로 분리해 단위 테스트 가능 |
| 내비게이션 | Jetpack Compose Navigation, 단일 Activity | |
| SDK | minSdk 26, target/compile SDK 35 | ML Kit 한글 인식·CameraX 안정 지원 범위 |

## 데이터 모델 (Room)

- **BookEntity** (`books`): id, isbn?, title, author?, publisher?, coverImageUrl?, totalPages?, status(READING/FINISHED/PAUSED/PLANNED), createdAt
- **ReadingRoundEntity** (`reading_rounds`): id, bookId(FK), roundNumber, startedAt, finishedAt? — 재독마다 새 라운드 생성. 책 하나에 독후감이 여러 개 쌓이는 요구사항을 라운드 단위로 깔끔하게 표현(라운드별 진행 이력 + 독후감 그룹핑). 재독 시작 시 새 라운드를 만들면 페이지 델타 계산도 라운드 내에서만 비교하므로 "재독으로 페이지가 1로 돌아갈 때 음수 델타" 같은 엣지케이스가 자연히 해결됨
- **ReadingLogEntity** (`reading_logs`): id, bookId(FK), readingRoundId(FK), currentPage, pagesReadDelta(직전 로그 대비, 음수면 0으로 클램프), logDateEpochDay(타임존 안정적인 일자 버켓용), loggedAt — bookId, logDateEpochDay에 인덱스
- **QuoteEntity** (`quotes`): id, bookId(FK), text, pageNumber?, createdAt
- **ReviewEntity** (`reviews`): id, bookId(FK), readingRoundId?(FK), content, rating?, createdAt

모든 FK는 `onDelete = CASCADE`. 사진 원본은 디스크에 영구 저장하지 않음(OCR 처리는 메모리상에서, 표지는 Coil 캐시로 충분) — 저장공간 최소화, STORAGE 권한 불필요.

## 패키지 구조

```
com.dyk1323.booklogs/
  data/
    local/{BooklogsDatabase, dao/*, entity/*}
    remote/{GoogleBooksApi, dto/*, BookMetadataMapper}
    repository/{Book,ReadingLog,Quote,Review,BookMetadata}Repository(+Impl)
  domain/
    model/ (Book, ReadingLog, Quote, Review, ReadingRound — Room 엔티티와 분리된 순수 모델)
    usecase/
      AggregateDailyPagesUseCase.kt   ← 핵심 요구사항(#1), 순수 함수, 최우선 단위테스트 대상
      LogProgressUseCase.kt           ← 델타 계산/클램프/라운드 연결
      ComputeBookProgressUseCase.kt   ← 진행률 %
  ui/
    dashboard/ (DashboardScreen, DashboardViewModel, DailyPagesBarChart, BookProgressListItem)
    registration/ (BookRegistrationScreen, BarcodeScanScreen, BarcodeAnalyzer, TitleSearchScreen, BookConfirmFormScreen, BookRegistrationViewModel)
    logging/ (LogProgressScreen, LogProgressViewModel)
    bookdetail/ (BookDetailScreen, BookDetailViewModel, ReadingRoundSection, QuoteListSection, ReviewListSection)
    quote/ (QuoteCaptureScreen, TextRecognitionAnalyzer, QuoteTextSelectionScreen, QuoteCaptureViewModel)
    review/ (ReviewEditorScreen, ReviewEditorViewModel)
    common/{theme, components}
    navigation/{BooklogsNavHost, Destinations}
  di/AppContainer.kt
  MainActivity.kt, BooklogsApplication.kt
```

## 화면 흐름

1. **대시보드(홈, 시작 화면) — "책장" 뷰**: 상단에 최근 N일 합산 페이지 막대그래프. 하단에 진행 중인 책들을 책장처럼 그리드로 배치 — 각 표지 이미지에 dim 오버레이(반투명 검정)를 씌우고, 그 위에 원형(도넛) 진행률 링을 표지 중앙/코너에 겹쳐 그림(진행률 = currentPage/totalPages). `totalPages`가 없는 책은 도넛 대신 "?" 또는 페이지 입력 유도 배지 표시. → 책 등록/진행 기록 화면, 책 탭 시 상세로 이동
2. **책 등록**: 스캔/제목검색/수동입력 선택 → 스캔은 CameraX+ML Kit 바코드 인식 → ISBN으로 카카오 책 검색 API 조회(실패 시 Google Books 폴백) → 확인/수정 폼(둘 다 실패 시 빈 폼+수동입력 안내) → 저장(상태 READING이면 첫 라운드 자동 생성). 제목 검색도 동일하게 카카오 우선 조회.
3. **진행 기록**: 두 가지 입력 방식 제공
   - 직접 입력: 진행중 책 선택 + 현재 페이지 숫자 입력
   - 사진 인식: 책 페이지 촬영(인용구 캡처와 동일한 카메라+OCR 파이프라인 재사용) → 인식된 텍스트 중 페이지 코너에 위치한 독립된 숫자 토큰을 페이지 번호 후보로 추출해 입력창에 자동 채움 → 사용자가 확인/수정 후 저장(OCR 오인식 가능성이 있어 자동 저장은 하지 않고 항상 확인 단계를 거침)
   - 두 방식 모두 해당 라운드의 마지막 로그 대비 델타 계산 후 저장
4. **책 상세**: 메타데이터, 진행률, 라운드별 진행 이력/독후감, 인용구 목록. → 진행기록/인용구추가/독후감작성으로 이동
5. **인용구 캡처**: 단발 촬영(연속 프레임 아님, 정확도 우선) → Latin+Korean 인식기 동시 실행 → 인식된 줄(line) 단위 리스트를 체크박스로 선택 → 선택된 줄을 순서대로 합쳐 편집 가능한 텍스트필드에 표시 → 페이지번호(선택) 입력 후 저장
6. **독후감 작성**: 특정 라운드에 연결된 텍스트+평점(선택) 작성/저장

## 구현 시 유의사항

- **바코드 분석기**: `ImageAnalysis.Analyzer`로 EAN-13만 필터링, `addOnCompleteListener`에서 반드시 `imageProxy.close()`. 첫 성공 인식 후 `clearAnalyzer()`로 중복 트리거 방지. ISBN 유효성(978/979로 시작하는 13자리)까지 확인 후 조회 호출.
- **OCR**: 실시간 오버레이 대신 정지 이미지 캡처 후 처리(흔들림 있는 실시간 인식보다 안정적). 이미지 내 탭-선택 오버레이 대신 인식된 줄을 리스트 형태로 선택하게 해 구현 난이도를 낮춤(기기 없이 검증 불가한 부분이므로 최대한 단순한 UX로 설계).
- **일별 합산 그래프**: `ReadingLogEntity.logDateEpochDay`(타임존 안정) 기준으로 버켓팅하는 순수 Kotlin 함수(`AggregateDailyPagesUseCase`)로 분리 — 이 함수가 이번 프로젝트의 핵심 로직이자 최우선 단위 테스트 대상.
- **메타데이터 연동(카카오 우선 + Google Books 보조)**: `BookMetadataRepositoryImpl`이 카카오 책 검색 API를 먼저 호출하고, 결과 없음/에러/오프라인이면 Google Books를 호출, 그마저 실패하면 수동입력 폼으로 폴백(`MetadataLookupResult.Success/NotFound/NetworkError`로 구분). 두 API 모두 5초 타임아웃. 카카오 REST API 키는 `local.properties`에 저장 후 `BuildConfig` 필드로 노출(레포에 커밋되지 않도록 `.gitignore` 확인), 사용자가 카카오 디벨로퍼스에서 직접 발급받아야 함을 README/설정 안내에 명시.
- **페이지 번호 사진 인식**: 인용구용 `TextRecognitionAnalyzer`를 그대로 재사용. 인식된 `Line` 중 (a) 순수 숫자로만 구성되고 (b) 자릿수가 1~4자리이며 (c) 이미지 상하단 코너 영역의 바운딩 박스에 위치하는 것을 페이지 번호 후보로 스코어링해 가장 그럴듯한 값을 입력창에 프리필. 후보가 여러 개면 가장 코너에 가까운 것을 우선하되 항상 사용자 확인/수정 단계를 거쳐 자동 저장하지 않음.
- **도넛 진행률 오버레이**: `DashboardScreen`의 책장 그리드 아이템은 `Box`로 표지(Coil `AsyncImage`) + 반투명 검정 `Box`(dim) + `Canvas`로 그리는 도넛(진행률 arc, `drawArc(startAngle=-90, sweepAngle=360*progress)`)을 겹쳐 그림. 진행률 계산은 기존 `ComputeBookProgressUseCase` 재사용.
- DAO는 인터페이스로 정의해 Repository 단위 테스트에서 Room 없이 Fake DAO로 대체 가능하게 함.

## Gradle 설정

- Version Catalog(`gradle/libs.versions.toml`)에 Compose BOM, Room(+KSP), CameraX, ML Kit(barcode-scanning, text-recognition, text-recognition-korean), Retrofit+OkHttp, Coil, Navigation-Compose, Coroutines 추가
- `AndroidManifest.xml`: `CAMERA`, `INTERNET` 권한, `<uses-feature android:name="android.hardware.camera" required="true"/>`
- 카카오 REST API 키: `local.properties`에 `KAKAO_API_KEY=...` 추가 → `app/build.gradle.kts`에서 `buildConfigField`로 주입, `local.properties`는 이미 `.gitignore` 대상이므로 키가 커밋되지 않음을 확인. 사용자가 카카오 디벨로퍼스(https://developers.kakao.com)에서 앱을 등록하고 키를 발급받아야 하는 단계는 구현 완료 후 별도 안내

## 검증 계획

샌드박스에 Android SDK/에뮬레이터가 없으므로:
- **가능**: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`로 순수 로직 검증
  - `AggregateDailyPagesUseCaseTest`(일자 버켓팅/0채움/여러 책 합산), `LogProgressUseCaseTest`(델타/클램프/라운드 경계), `ComputeBookProgressUseCaseTest`(totalPages null 처리), `BookMetadataMapperTest`(카카오 DTO 매핑 + Google Books DTO 매핑 + 폴백 분기), `PageNumberCandidateExtractorTest`(숫자 토큰 필터링/코너 스코어링 로직, 순수 함수로 분리해 단위 테스트), Fake DAO 기반 Repository 테스트
  - `assembleDebug`/`lint`는 SDK 플랫폼 컴포넌트 다운로드가 가능한지에 따라 시도해보되, 안 되면 컴파일+유닛테스트까지가 한계
- **불가능(사용자가 실기기/에뮬레이터에서 직접 확인 필요)**: Compose 화면 렌더링/내비게이션 클릭 흐름(책장 그리드 + 도넛 오버레이 실제 표시 포함), CameraX 프리뷰·권한, 실제 바코드 인식 정확도, 실제 한글/영어 책 페이지 OCR 정확도 및 텍스트 선택 UX, 페이지 번호 자동 인식 정확도(폰트/각도/코너 위치 편차가 커서 실기기 튜닝 필요), Room 실기기 동작, 카카오/Google Books 실제 네트워크 응답, 전체 엔드투엔드 플로우(등록→기록→그래프 갱신→인용구→독후감)

구현 순서는 Room 스키마 → DAO/Repository → domain usecase(전부 유닛테스트로 검증) → ViewModel(Fake Repository로 검증) → Compose UI/CameraX/ML Kit(유닛테스트 불가, 최대한 단순하게 구현) 순으로 진행해, 샌드박스에서 검증 가능한 "핵심 로직"을 먼저 견고히 하고 카메라/OCR 튜닝은 사용자 몫으로 넘긴다.
