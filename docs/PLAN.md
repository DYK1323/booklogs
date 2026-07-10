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
- **빈도 기반 마찰 설계**: 책 등록은 자주 하는 작업이 아니므로 여러 단계 절차(스캔→조회→확인폼)를 유지해도 되지만, 진행 중인 책 3~4권의 진행률 체크와 인용구 등록은 거의 매일 반복되는 작업이므로 화면 전환을 최소화한 원탭 진입 방식으로 설계한다 (아래 "빠른 기록 UX" 참고). 기록 절차가 번거로우면 결국 기록을 포기하게 된다는 것이 설계 원칙.
- **리마인더**: 사용자가 설정한 시각에 하루 한 번, 현재 READING인 책 중 **무작위로 1권**을 골라 진행률을 알림으로 보여줌(예: "『책 제목』 62% 읽는 중"). 알림 탭 시 해당 책의 빠른 기록 시트로 바로 진입해 그 자리에서 진행률을 기록할 수 있음.

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
| 리마인더 | `AlarmManager`(비정확 알람, `setAndAllowWhileIdle`) + `NotificationManager` + DataStore Preferences(설정 저장) | 클라우드/서버 푸시 없이 기기 로컬 알람만으로 구현 가능. 분 단위 오차는 독서 리마인더 용도에 무해하다고 보고 정확 알람(Android 12+ `SCHEDULE_EXACT_ALARM` 권한 필요)은 피해 권한 요청 절차를 단순화 |
| SDK | minSdk 26, target/compile SDK 35 | ML Kit 한글 인식·CameraX 안정 지원 범위 |

## 데이터 모델 (Room)

- **BookEntity** (`books`): id, isbn?, title, author?, publisher?, coverImageUrl?, totalPages?, status(READING/PAUSED/FINISHED/DROPPED/PLANNED), createdAt
  - READING: 대시보드 책장에 노출, 진행 중
  - PAUSED: 일시중지 — 나중에 재개할 생각 있음. 같은 라운드를 유지한 채 책장에서만 빠짐
  - FINISHED: 완독
  - **DROPPED**: 중단 — 재개할 생각 없음. FINISHED와 마찬가지로 "라운드 종료"이지만 종료 사유가 다름(아래 참고)
  - PLANNED: 아직 시작 전
- **ReadingRoundEntity** (`reading_rounds`): id, bookId(FK), roundNumber, startedAt, finishedAt?, **endReason?**(COMPLETED/DROPPED, 라운드가 끝났을 때만 값이 들어가고 진행/일시중지 중엔 null) — 재독마다 새 라운드 생성. 책 하나에 독후감이 여러 개 쌓이는 요구사항을 라운드 단위로 깔끔하게 표현(라운드별 진행 이력 + 독후감 그룹핑). 재독 시작 시 새 라운드를 만들면 페이지 델타 계산도 라운드 내에서만 비교하므로 "재독으로 페이지가 1로 돌아갈 때 음수 델타" 같은 엣지케이스가 자연히 해결됨.
  - **완독과 중단은 같은 메커니즘**(라운드 종료, `finishedAt` 기록)이고 `endReason`만 다름. 일시중지(PAUSED)는 라운드를 종료하지 않고 그대로 열어둔 채 책 상태만 바꾸는 것이라 서로 구분됨.
  - FINISHED/DROPPED 상태에서 다시 읽기 시작하면(재독/재도전) 기존 라운드는 그대로 두고 `roundNumber`가 1 증가한 **새 라운드**를 생성 — 1차 시도의 진행 이력·독후감은 보존되고 2차 시도가 새로 쌓임. PAUSED에서 재개하면 라운드를 새로 만들지 않고 열려있던 라운드를 그대로 이어감.
- **ReadingLogEntity** (`reading_logs`): id, bookId(FK), readingRoundId(FK), currentPage, logDateEpochDay(타임존 안정적인 일자 버켓용), loggedAt — bookId, logDateEpochDay에 인덱스. **델타는 컬럼으로 저장하지 않는다.** 각 로그는 "이 시점에 몇 페이지였다"는 스냅샷일 뿐이고, 페이지 증가량(델타)은 항상 같은 라운드의 로그들을 `loggedAt` 순으로 정렬해 인접한 두 로그의 차이로 그때그때 계산한다(clamped ≥0). 이렇게 델타를 파생값으로만 다루면 과거 로그를 수정/삭제해도 그 행 하나만 갱신/삭제하면 끝이고, 이웃 로그의 저장된 델타를 다시 써넣는 연쇄 작업이 필요 없다 — **어떤 시점의 로그든 자유롭게 수정/삭제 가능**. 개인 독서기록 규모(수백~수천 행)에서 매번 정렬+차이계산하는 비용은 무시할 수준.
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
    settings/ (ReminderSettingsDataStore — DataStore Preferences, Room이 아님. reminderEnabled: Boolean, reminderHour/Minute: Int)
  domain/
    model/ (Book, ReadingLog, Quote, Review, ReadingRound — Room 엔티티와 분리된 순수 모델)
    usecase/
      ComputeLogDeltasUseCase.kt      ← 핵심 파생 로직, 순수 함수: 한 라운드의 로그 목록(loggedAt순)을 받아 인접 쌍의 차이를 delta로 변환(음수 클램프). AggregateDailyPagesUseCase와 라운드 진행 이력 표시(책 상세) 양쪽에서 재사용
      AggregateDailyPagesUseCase.kt   ← 핵심 요구사항(#1), 순수 함수, 최우선 단위테스트 대상. 전체 로그를 라운드별로 묶어 ComputeLogDeltasUseCase로 델타를 얻은 뒤 각 델타를 "뒤쪽 로그"의 logDateEpochDay에 귀속시켜 날짜별 합산(요청 기간 밖의 이전 로그도 델타 계산엔 포함해야 경계의 첫 델타가 정확함)
      LogProgressUseCase.kt           ← 새 로그 저장(단순 insert, currentPage+timestamp만 기록, 델타 계산 없음)
      EditLogUseCase.kt               ← 임의 로그의 currentPage 수정(단순 update, 이웃 로그 재계산 불필요 — 델타가 저장되지 않으므로)
      DeleteLogUseCase.kt             ← 임의 로그 삭제(단순 delete, 이웃 로그 재계산 불필요)
      ComputeBookProgressUseCase.kt   ← 진행률 %
      ChangeBookStatusUseCase.kt      ← 상태 전이 처리(READING↔PAUSED는 라운드 유지, →FINISHED/DROPPED는 현재 라운드 종료+endReason 기록, FINISHED/DROPPED→READING은 새 라운드 생성). 전이 종류별 라운드 부수효과를 한 곳에 모아 화면(책 상세)에서는 단순 호출만 하도록 함
      PickReminderBookUseCase.kt      ← 순수 함수: READING 상태 책 목록을 받아 무작위로 1권 선택(빈 목록이면 null 반환 → 리시버가 알림을 건너뜀)
  ui/
    dashboard/ (DashboardScreen, DashboardViewModel, TodayPagesHero, DailyPagesBarChart, BookShelfGrid, BookCoverProgressRing, BookQuickActionSheet, QuickProgressEntryViewModel)
    registration/ (BookRegistrationScreen, BarcodeScanScreen, BarcodeAnalyzer, TitleSearchScreen, BookConfirmFormScreen, BookRegistrationViewModel)
    bookdetail/ (BookDetailScreen, BookDetailViewModel, ReadingRoundSection, QuoteListSection, ReviewListSection, BookStatusActions — "진행률 기록"/"인용구 추가" 버튼은 dashboard의 BookQuickActionSheet/QuoteCaptureScreen을 그대로 재사용)
    library/ (LibraryScreen, LibraryViewModel — 상태별 필터가 가능한 전체 책 목록. PAUSED/DROPPED/FINISHED/PLANNED 책은 대시보드 책장(READING 전용)에 안 나오므로 이 화면이 유일한 접근 경로)
    quote/ (QuoteCaptureScreen, TextRecognitionAnalyzer, QuoteTextSelectionScreen, QuoteCaptureViewModel — bookId 파라미터로 대시보드/책상세 양쪽에서 직접 진입 가능)
    review/ (ReviewEditorScreen, ReviewEditorViewModel)
    settings/ (SettingsScreen, SettingsViewModel — 리마인더 on/off 토글 + TimePicker)
    common/{theme, components}
    navigation/{BooklogsNavHost, Destinations}
  notification/
    ReminderScheduler.kt              ← AlarmManager 등록/취소 래퍼
    ReminderReceiver.kt               ← BroadcastReceiver, 알람 시각에 실행: 책 조회→PickReminderBookUseCase→알림 표시→다음날 알람 재등록
    BootReceiver.kt                   ← RECEIVE_BOOT_COMPLETED 수신 시 저장된 설정 기준으로 알람 재등록(재부팅 시 AlarmManager 알람이 사라지므로)
    ReminderNotificationBuilder.kt    ← 알림 콘텐츠/딥링크(PendingIntent → 대시보드 특정 bookId로 BookQuickActionSheet 자동 오픈) 구성
  di/AppContainer.kt
  MainActivity.kt, BooklogsApplication.kt
```

## 빠른 기록 UX (핵심 설계 원칙)

책 등록은 저빈도 작업이라 여러 단계를 거쳐도 되지만, **진행률 체크와 인용구 등록은 거의 매일 반복**되므로 화면 전환 자체를 없애는 방향으로 설계한다.

- **책장 표지 탭 → 화면 전환 없이 바텀시트**가 즉시 열림. 이 시트가 진행률 체크·인용구 추가·상세보기 세 가지 진입점을 모두 담당하며, "책 상세" 화면을 거치지 않는다.
  - 시트를 열면 **진행률 입력이 바로 최상단에 노출**된다: 숫자 입력창에 마지막으로 기록한 페이지 번호가 **전체 선택된 상태**로 프리필되고 숫자 키패드가 자동으로 뜬다. 사용자는 지금 책에서 보고 있는 페이지 번호를 그대로 타이핑해서 덮어쓰기만 하면 됨 — 마지막 기록과의 차이를 암산할 필요가 없다(이전 버전의 `+5/+10/+20/+50` 델타 칩은 "지금 보는 페이지"가 아니라 "마지막 기록 대비 얼마나 더 읽었는지"를 계산하게 만들어 오히려 번거로워서 폐기). 델타는 저장되지 않고 필요할 때 파생되므로 사용자는 신경 쓸 필요 없음.
    - 입력창 바로 옆에 카메라 아이콘 버튼을 동급 옵션으로 배치: 탭하면 카메라가 열리고 페이지 사진을 찍어 OCR로 추출한 숫자를 같은 입력창에 채워준 뒤 확인/저장 흐름으로 복귀. 타이핑도 촬영도 둘 다 "암산 없이 보이는 숫자 그대로"라는 점에서 동등한 주 경로이므로, 촬영을 보조 링크로 숨기지 않고 나란히 노출한다. 사용자는 그때그때 더 편한 쪽을 선택.
    - 입력창 아래에 **"최근 기록: 245p · 3분 전"**처럼 그 책의 가장 최근 로그를 작게 보여주고, 옆에 연필(수정)·휴지통(삭제) 아이콘을 둔다. 방금 낸 오타를 새 로그를 또 쌓지 않고 바로 잡기 위한 용도(가장 흔한 실수 케이스라 대시보드에서 화면 전환 없이 처리).
      - 연필 탭 → 입력창이 그 로그의 `currentPage`로 채워지고 "저장" 버튼이 "수정 저장"으로 바뀜 → 저장 시 새 로그를 추가하지 않고 기존 로그의 `currentPage`만 갱신(`EditLogUseCase`, 델타가 저장되지 않으므로 이웃 로그를 건드릴 필요 없음).
      - 휴지통 탭 → 확인 다이얼로그 없이 즉시 삭제하고 하단에 "기록 삭제됨 · 실행취소" 스낵바를 몇 초간 노출(`DeleteLogUseCase`) — 빠른 기록이라는 원칙에 맞게 모달 확인 대신 실행취소로 되돌릴 수 있게 함.
      - **과거 날짜의 로그는 이 시트가 아니라 책 상세의 진행 이력 목록에서 수정/삭제**(아래 4번 참고) — 대시보드 시트는 "방금 기록한 것 즉시 정정"용으로 최근 1건만 인라인 노출하고, 여러 날짜에 걸친 로그를 훑어보며 고치는 건 빈도가 낮은 딥다이브 작업이라 책 상세로 위치시킴. 스키마상 제약은 없으므로(델타 비저장) 언제든 어떤 로그든 같은 `EditLogUseCase`/`DeleteLogUseCase`로 처리.
  - 시트 내 "인용구 추가" 버튼: 탭하면 책 상세를 거치지 않고 곧바로 카메라(인용구 캡처)로 진입 — **대시보드 → 카메라 1홉**. 인용구 저장 후에는 "계속 촬영" / "완료"를 선택하게 해, 한 번에 여러 인용구를 찍을 때마다 대시보드로 돌아왔다 다시 들어가지 않아도 되게 한다.
  - 시트 내 "책 상세보기" 버튼: 진행 이력·독후감처럼 가끔 보는 딥다이브용 화면으로 이동하는 세 번째(가장 낮은 빈도) 옵션.
- `totalPages`가 없는 책은 도넛 대신 "?" 배지를 표시하고, 시트를 열면 총 페이지 수 입력을 먼저 유도.

## 화면 흐름

1. **대시보드(홈, 시작 화면) — "책장" 뷰**: 위에서부터
   - **오늘 읽은 페이지** 큰 숫자로 강조 표시("오늘 128p"). 걷기 그래프의 "오늘 걸음 수"와 같은 위상.
   - **최근 7일 합산 페이지 막대그래프**(걷기 그래프 스타일): 오늘 막대는 강조색, 나머지는 흐린 색. 위 "오늘 읽은 페이지"와 이 그래프는 같은 데이터 소스(`AggregateDailyPagesUseCase`가 만드는 일자별 합산 배열) — 배열의 마지막(오늘) 항목을 숫자로 뽑아 보여주고 배열 전체를 막대로 그리는 것뿐, 별도 계산 불필요.
   - **책장 그리드**: 진행 중인(`BookEntity.status == READING`) 책들을 표지로 배치 — 각 표지 이미지에 dim 오버레이(반투명 검정)를 씌우고, 그 위에 원형(도넛) 진행률 링을 겹쳐 그림(진행률 = currentPage/totalPages, `ComputeBookProgressUseCase` 재사용). **도넛 중앙에 퍼센트 텍스트("62%")를 함께 표시**해 시각적 링만으로 정확한 값을 가늠하기 어려운 문제를 보완 — 정확한 페이지 수(185/320p)는 표지 탭 시 열리는 빠른 기록 시트/책 상세에서 확인.
   - 표지 탭 → 위 "빠른 기록 UX"의 바텀시트가 열림(화면 전환 없음). 별도 FAB로 책 등록 화면 진입, 상단바 아이콘으로 라이브러리(전체 목록) 화면 진입.
2. **책 등록**: 스캔/제목검색/수동입력 선택 → 스캔은 CameraX+ML Kit 바코드 인식 → ISBN으로 카카오 책 검색 API 조회(실패 시 Google Books 폴백) → 확인/수정 폼(둘 다 실패 시 빈 폼+수동입력 안내) → 저장(상태 READING이면 첫 라운드 자동 생성). 제목 검색도 동일하게 카카오 우선 조회. 저빈도 작업이므로 기존의 다단계 절차를 그대로 유지.
3. **진행률 빠른 기록 시트**: 위 "빠른 기록 UX" 참고. 대시보드 표지 탭으로 진입하는 것이 기본 경로이며, 책 상세 화면에서도 동일한 시트를 재사용해 진입 가능(어떤 경로든 "책 선택" 단계가 별도로 필요 없음 — 이미 어떤 책인지 알고 진입하므로). 사진 인식 모드는 인용구 캡처와 동일한 카메라+OCR 파이프라인을 재사용, 코너의 독립된 숫자 토큰을 페이지 후보로 추출해 프리필하되 항상 사용자 확인 후 저장.
4. **책 상세**: 메타데이터, 진행률, 라운드별 진행 이력/독후감, 인용구 목록. 딥다이브용 화면이며 빠른 기록 시트에서 "상세보기"로 진입하거나, 검색/목록에서 직접 진입. **진행 이력 목록의 각 로그 행은 탭하면 인라인으로 펼쳐져 수정/삭제 가능**(대시보드 빠른 기록 시트와 동일한 `EditLogUseCase`/`DeleteLogUseCase` 재사용) — 과거 특정 날짜의 기록을 고치는 것은 이 화면이 담당.
   - **상태 변경 액션**(`BookStatusActions`, `ChangeBookStatusUseCase` 호출): READING 중엔 "다 읽음" / "중단" / "일시중지" 세 버튼을 노출. "일시중지"는 라운드를 유지한 채 상태만 바꾸고, "다 읽음"/"중단"은 현재 라운드를 종료(각각 `endReason = COMPLETED`/`DROPPED`)하고 책 상태를 FINISHED/DROPPED로 바꿈. FINISHED/DROPPED/PAUSED 상태에선 "다시 읽기 시작" 버튼 하나로 재개(PAUSED는 같은 라운드 이어감, FINISHED/DROPPED는 새 라운드 시작). 등록/상태변경처럼 저빈도 작업이라 책 상세에 위치, 확인 다이얼로그 없이 즉시 적용 후 스낵바로 되돌리기 제공.
   - → 독후감작성 등 저빈도 작업으로 이동
5. **인용구 캡처**: 대시보드 빠른 기록 시트 또는 책 상세에서 진입(둘 다 책 컨텍스트를 이미 알고 있어 책 선택 단계 없음). 단발 촬영(연속 프레임 아님, 정확도 우선) → Latin+Korean 인식기 동시 실행 → 인식된 줄(line) 단위 리스트를 체크박스로 선택("전체 선택" 토글도 제공, 문단 전체를 인용하는 경우가 많음) → 선택된 줄을 순서대로 합쳐 편집 가능한 텍스트필드에 표시 → 페이지번호(선택) 입력 후 저장 → "계속 촬영"(같은 책으로 카메라 재진입) / "완료"(호출한 곳으로 복귀) 선택.
6. **독후감 작성**: 특정 라운드에 연결된 텍스트+평점(선택) 작성/저장 — 중단한(DROPPED) 라운드에도 독후감(왜 중단했는지 등)을 남길 수 있음, 제약 없음
7. **라이브러리(전체 책 목록)**: 대시보드 상단바에서 진입, 상태별(읽는 중/일시중지/완독/중단/읽을 예정) 필터와 검색 제공. PAUSED·DROPPED·FINISHED·PLANNED 책은 대시보드 책장에는 안 보이므로 이 화면이 유일한 접근 경로. 목록 아이템 탭 → 책 상세.
8. **설정(리마인더)**: 대시보드 상단바에서 진입. 리마인더 on/off 토글 + 시각 선택(`TimePicker`, 하루 1회). 저장 즉시 `ReminderScheduler`가 알람을 재등록/취소. 알림 자체는 매일 정해진 시각에 현재 READING인 책 중 **무작위로 1권**을 골라 "『책 제목』 62% 읽는 중" 형태로 표시하고, 알림 탭 시 그 책의 빠른 기록 시트가 바로 열려 그 자리에서 진행률을 기록할 수 있음(리마인더가 곧 빠른 기록 진입점이 되도록 설계). READING인 책이 하나도 없는 날은 알림을 건너뛰고 다음날 알람만 재등록.

## 구현 시 유의사항

- **바코드 분석기**: `ImageAnalysis.Analyzer`로 EAN-13만 필터링, `addOnCompleteListener`에서 반드시 `imageProxy.close()`. 첫 성공 인식 후 `clearAnalyzer()`로 중복 트리거 방지. ISBN 유효성(978/979로 시작하는 13자리)까지 확인 후 조회 호출.
- **OCR**: 실시간 오버레이 대신 정지 이미지 캡처 후 처리(흔들림 있는 실시간 인식보다 안정적). 이미지 내 탭-선택 오버레이 대신 인식된 줄을 리스트 형태로 선택하게 해 구현 난이도를 낮춤(기기 없이 검증 불가한 부분이므로 최대한 단순한 UX로 설계).
- **일별 합산 그래프**: `ComputeLogDeltasUseCase`(라운드별 로그를 `loggedAt`순 정렬 후 인접 쌍 차이 계산, 음수 클램프)로 델타를 파생시킨 뒤, 각 델타를 그 쌍의 뒤쪽 로그의 `logDateEpochDay`에 귀속시켜 날짜별로 합산하는 순수 Kotlin 함수(`AggregateDailyPagesUseCase`)로 분리 — 이 두 함수가 이번 프로젝트의 핵심 로직이자 최우선 단위 테스트 대상. 요청한 기간(예: 최근 14일) 밖의 로그도 델타 계산 입력에는 포함해야 기간 경계의 첫 델타가 정확하게 나옴에 유의.
- **메타데이터 연동(카카오 우선 + Google Books 보조)**: `BookMetadataRepositoryImpl`이 카카오 책 검색 API를 먼저 호출하고, 결과 없음/에러/오프라인이면 Google Books를 호출, 그마저 실패하면 수동입력 폼으로 폴백(`MetadataLookupResult.Success/NotFound/NetworkError`로 구분). 두 API 모두 5초 타임아웃. 카카오 REST API 키는 `local.properties`에 저장 후 `BuildConfig` 필드로 노출(레포에 커밋되지 않도록 `.gitignore` 확인), 사용자가 카카오 디벨로퍼스에서 직접 발급받아야 함을 README/설정 안내에 명시.
- **페이지 번호 사진 인식**: 인용구용 `TextRecognitionAnalyzer`를 그대로 재사용. 인식된 `Line` 중 (a) 순수 숫자로만 구성되고 (b) 자릿수가 1~4자리이며 (c) 이미지 상하단 코너 영역의 바운딩 박스에 위치하는 것을 페이지 번호 후보로 스코어링해 가장 그럴듯한 값을 입력창에 프리필. 후보가 여러 개면 가장 코너에 가까운 것을 우선하되 항상 사용자 확인/수정 단계를 거쳐 자동 저장하지 않음.
- **도넛 진행률 오버레이**: `DashboardScreen`의 책장 그리드 아이템은 `Box`로 표지(Coil `AsyncImage`) + 반투명 검정 `Box`(dim) + `Canvas`로 그리는 도넛(진행률 arc, `drawArc(startAngle=-90, sweepAngle=360*progress)`) + 도넛 중앙에 `Text("${(progress*100).roundToInt()}%")`를 겹쳐 그림. 진행률 계산은 기존 `ComputeBookProgressUseCase` 재사용. `totalPages`가 없으면 도넛/퍼센트 대신 "?" 배지로 대체(이미 앞서 정의됨).
- **오늘 읽은 페이지 히어로 숫자 + 주간 막대그래프**: `DashboardViewModel`이 `AggregateDailyPagesUseCase`의 결과(`List<DayPageTotal>`, 최근 7일)를 한 번만 계산해 `DailyPagesBarChart`(전체 배열)와 `TodayPagesHero`(마지막 원소, `Text`로 큰 숫자) 두 컴포저블에 그대로 전달 — 별도 쿼리/계산 중복 없음.
- **빠른 기록 시트(`BookQuickActionSheet`)**: `ModalBottomSheet`로 구현, 열릴 때 해당 책의 마지막 `ReadingLog.currentPage`를 조회해 입력창에 프리필하고 `TextFieldValue`의 selection을 전체 범위로 설정해 즉시 타이핑하면 덮어써지도록 함(`LocalFocusRequester`로 자동 포커스 + 키패드 자동 표시). 저장 시 `LogProgressUseCase`가 단순 insert(델타 계산 없음). 시트는 화면 전환이 아니라 대시보드 위에 오버레이되므로 저장 후 자동 닫힘 + 진행률 링 애니메이션 갱신.
- **인용구 "계속 촬영" 흐름**: `QuoteCaptureViewModel`이 저장 성공 후 `SnackbarResult`/다이얼로그로 "계속 촬영" 선택 시 동일 화면(같은 bookId)에서 카메라를 재시작하고, "완료" 선택 시 호출한 곳(대시보드 또는 책 상세)으로 pop. 내비게이션 스택에 화면을 새로 쌓지 않고 같은 컴포저블 내에서 상태만 리셋.
- **로그 수정/삭제(임의 시점 가능)**: `ReadingLogDao`에 `getLatest(roundId): Flow<ReadingLogEntity?>`, `getAllForRound(roundId): Flow<List<ReadingLogEntity>>`, `update(log)`, `deleteById(id)` 추가. `EditLogUseCase`/`DeleteLogUseCase`는 대상 로그의 `roundId`만 확인하고 바로 update/delete — 델타가 저장되지 않으므로 이웃 로그를 손댈 필요가 전혀 없다. 대시보드 시트는 `getLatest`로 최근 1건만 인라인 노출하고, 책 상세의 진행 이력은 `getAllForRound`로 전체 목록을 보여주며 각 행에 동일한 수정/삭제 진입점을 둔다. 두 화면 모두 Flow 구독이라 수정/삭제 즉시 진행률 링·그래프·이력 목록이 자동 갱신됨. 삭제 시 스낵바의 "실행취소"는 삭제된 엔티티를 ViewModel이 메모리에 잠깐 들고 있다가 재삽입하는 방식으로 구현(별도 soft-delete 컬럼 불필요).
- **책 상태 전이(`ChangeBookStatusUseCase`)**: 전이 종류에 따라 분기.
  - READING → PAUSED: `BookEntity.status`만 갱신, 라운드 불변.
  - READING → FINISHED/DROPPED: 현재 열린 라운드(`finishedAt == null`)를 찾아 `finishedAt = now`, `endReason = COMPLETED`/`DROPPED`로 갱신 + `BookEntity.status` 동기화.
  - PAUSED → READING: `BookEntity.status`만 갱신, 라운드 불변(이미 열려있음).
  - FINISHED/DROPPED → READING: `roundNumber = 이전 최대값 + 1`인 새 `ReadingRoundEntity` 삽입(startedAt=now, finishedAt=null) + `BookEntity.status = READING`. 첫 등록 시 라운드 생성 로직과 동일한 헬퍼 재사용.
  - 각 케이스가 독립적인 분기라 파악이 쉽고, 트랜잭션 하나(`@Transaction`)로 묶어 라운드/책 상태가 항상 같이 갱신되도록 함(하나만 갱신되고 나머지가 실패하는 상태 방지).
- **라이브러리 화면 쿼리**: `BookDao.observeAll(): Flow<List<BookEntity>>`를 가져와 ViewModel에서 status로 필터링(데이터 양이 적어 SQL WHERE 없이 메모리 필터로 충분). 검색은 title/author `contains` 매칭.
- **리마인더 알람/알림**: `ReminderScheduler.schedule(hour, minute)`이 `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, pendingIntent)`로 다음 발생 시각 하나만 예약(반복 알람 대신 매번 재예약 — Doze 하에서 `setRepeating` 오차 누적을 피하기 위함). `ReminderReceiver.onReceive`가 (1) `BookDao`에서 READING 목록 조회 → `PickReminderBookUseCase`로 무작위 1권 선택(없으면 알림 생략) → (2) 알림 표시 시 `PendingIntent`의 딥링크 extra로 `bookId`를 실어 `MainActivity` → `Destinations.Dashboard(openQuickSheetFor = bookId)`로 진입하도록 구성 → (3) 다음날 같은 시각으로 알람 재예약. 기기 재부팅 시 `AlarmManager` 알람이 사라지므로 `BootReceiver`가 `RECEIVE_BOOT_COMPLETED`를 받아 저장된 설정으로 재등록. Android 13(API 33)+에서는 알림 표시 전 런타임 `POST_NOTIFICATIONS` 권한 요청 필요(설정 화면에서 리마인더를 켤 때 요청).
- DAO는 인터페이스로 정의해 Repository 단위 테스트에서 Room 없이 Fake DAO로 대체 가능하게 함.

## Gradle 설정

- Version Catalog(`gradle/libs.versions.toml`)에 Compose BOM, Room(+KSP), CameraX, ML Kit(barcode-scanning, text-recognition, text-recognition-korean), Retrofit+OkHttp, Coil, Navigation-Compose, Coroutines, **DataStore Preferences** 추가
- `AndroidManifest.xml`: `CAMERA`, `INTERNET`, **`POST_NOTIFICATIONS`(API 33+ 런타임 권한), `RECEIVE_BOOT_COMPLETED`** 권한, `<uses-feature android:name="android.hardware.camera" required="true"/>`, `ReminderReceiver`/`BootReceiver`를 `<receiver>`로 등록
- 카카오 REST API 키: `local.properties`에 `KAKAO_API_KEY=...` 추가 → `app/build.gradle.kts`에서 `buildConfigField`로 주입, `local.properties`는 이미 `.gitignore` 대상이므로 키가 커밋되지 않음을 확인. 사용자가 카카오 디벨로퍼스(https://developers.kakao.com)에서 앱을 등록하고 키를 발급받아야 하는 단계는 구현 완료 후 별도 안내

## 검증 계획

샌드박스에 Android SDK/에뮬레이터가 없으므로:
- **가능**: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`로 순수 로직 검증
  - `ComputeLogDeltasUseCaseTest`(핵심: 정렬/인접쌍 차이/클램프, 그리고 **중간 로그 수정·삭제 후 이웃 델타가 재저장 없이 올바르게 다시 계산되는지** — 이번 스키마 변경의 핵심 검증 대상), `AggregateDailyPagesUseCaseTest`(일자 버켓팅/0채움/여러 책 합산/기간 경계 밖 로그 포함 여부), `LogProgressUseCaseTest`(단순 insert), `EditLogUseCaseTest`/`DeleteLogUseCaseTest`(임의 시점 로그 수정/삭제, 이웃 로그 미변경 확인), `ComputeBookProgressUseCaseTest`(totalPages null 처리), `ChangeBookStatusUseCaseTest`(READING↔PAUSED는 라운드 불변, →FINISHED/DROPPED는 라운드 종료+endReason 정확성, FINISHED/DROPPED→READING은 roundNumber 증가한 새 라운드 생성, PAUSED→READING은 라운드 재사용), `PickReminderBookUseCaseTest`(빈 목록→null, 여러 권 중 무작위 선택이 목록 범위 내에서만 나오는지), `BookMetadataMapperTest`(카카오 DTO 매핑 + Google Books DTO 매핑 + 폴백 분기), `PageNumberCandidateExtractorTest`(숫자 토큰 필터링/코너 스코어링 로직, 순수 함수로 분리해 단위 테스트), Fake DAO 기반 Repository 테스트
  - `assembleDebug`/`lint`는 SDK 플랫폼 컴포넌트 다운로드가 가능한지에 따라 시도해보되, 안 되면 컴파일+유닛테스트까지가 한계
- **불가능(사용자가 실기기/에뮬레이터에서 직접 확인 필요)**: Compose 화면 렌더링/내비게이션 클릭 흐름(책장 그리드 + 도넛 오버레이 실제 표시 포함), CameraX 프리뷰·권한, 실제 바코드 인식 정확도, 실제 한글/영어 책 페이지 OCR 정확도 및 텍스트 선택 UX, 페이지 번호 자동 인식 정확도(폰트/각도/코너 위치 편차가 커서 실기기 튜닝 필요), Room 실기기 동작, 카카오/Google Books 실제 네트워크 응답, **AlarmManager 알람 발화·Doze 하 지연 정도·재부팅 후 재등록·알림 표시/딥링크 동작**, 전체 엔드투엔드 플로우(등록→기록→그래프 갱신→인용구→독후감→리마인더)

구현 순서는 Room 스키마 → DAO/Repository → domain usecase(전부 유닛테스트로 검증) → ViewModel(Fake Repository로 검증) → Compose UI/CameraX/ML Kit(유닛테스트 불가, 최대한 단순하게 구현) 순으로 진행해, 샌드박스에서 검증 가능한 "핵심 로직"을 먼저 견고히 하고 카메라/OCR 튜닝은 사용자 몫으로 넘긴다.
