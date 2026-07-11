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
- 바코드/제목 조회: 온라인 API로 메타데이터 자동완성 사용, 오프라인/실패 시 수동 입력으로 폴백. 독서 기록 데이터 자체는 기기에만 저장(Room DB만 사용, 서버/계정 없음). 메타데이터 API는 **카카오 책 검색 API(제목/저자/출판사/표지/ISBN) + Google Books(페이지수/장르 보조)를 항상 병렬 조회**해 합성(카카오가 실패/결과없음이면 Google Books 결과 전체로 대체) — 상세 실패 처리는 "구현 시 유의사항" 참고
- 첫 화면(대시보드)은 책장처럼 표지를 늘어놓고, 각 표지에 dim 처리 + 원형(도넛) 진행률 오버레이를 씌우는 비주얼
- 진행률 기록은 페이지 번호 직접 입력뿐 아니라, 책 페이지를 촬영해 OCR로 페이지 번호를 자동 인식해 채워주는 방식도 지원
- **빈도 기반 마찰 설계**: 책 등록은 자주 하는 작업이 아니므로 여러 단계 절차(스캔→조회→확인폼)를 유지해도 되지만, 진행 중인 책 3~4권의 진행률 체크와 인용구 등록은 거의 매일 반복되는 작업이므로 화면 전환을 최소화한 원탭 진입 방식으로 설계한다 (아래 "빠른 기록 UX" 참고). 기록 절차가 번거로우면 결국 기록을 포기하게 된다는 것이 설계 원칙.
- **리마인더**: 사용자가 설정한 시각에 하루 한 번, 현재 READING인 책 중 **무작위로 1권**을 골라 진행률을 알림으로 보여줌(예: "『책 제목』 62% 읽는 중"). 알림 탭 시 해당 책의 빠른 기록 시트로 바로 진입해 그 자리에서 진행률을 기록할 수 있음.

## 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| 언어/UI | Kotlin + Jetpack Compose + Material 3 | 단일 Activity, 최신 표준 |
| 폰트 | Pretendard(OFL, `res/font/`에 정적 TTF 4종: Regular/Medium/SemiBold/Bold) | SF Pro는 Apple 독점 폰트라 Android 앱에 라이선스상 사용 불가. Pretendard는 한글 최적화 + 라틴 커버리지 양호해 앱 전체(한/영/숫자) 단일 폰트로 사용, 별도 라틴 대체 폰트 안 둠 |
| 아키텍처 | MVVM: Compose UI → ViewModel → Repository → Room DAO | |
| DI | 수동 DI (`AppContainer`, Hilt 미사용) | 단일 모듈, 리포지토리 5개 내외라 Hilt/KSP 애노테이션 프로세싱 오버헤드가 불필요. 에뮬레이터 없는 샌드박스에서 생성 코드 런타임 검증이 안 되므로 리스크 최소화 |
| 로컬 DB | Room (KSP) | 유일한 데이터 저장소, 클라우드 없음 |
| 비동기 | Kotlin Coroutines + Flow | DAO가 `Flow<List<T>>` 반환, 반응형 UI |
| 카메라 | CameraX | 바코드 스캔 + 페이지 촬영 공용 |
| 바코드 인식 | ML Kit Barcode Scanning (온디바이스), EAN-13 | ISBN-13은 EAN-13 포맷 |
| OCR | ML Kit Text Recognition v2: Latin + Korean 모델 둘 다, 온디바이스 | 클라우드 미사용 요구사항 충족 |
| 메타데이터 조회 | Retrofit + OkHttp. **카카오 책 검색 API**(`GET /v3/search/book`)로 제목/저자/출판사/표지/ISBN을 채우고, **Google Books API**(`q=isbn:{isbn}` 또는 `q=intitle:{query}`, 키 불필요)를 항상 병렬로 같이 호출해 `pageCount`(총 페이지수)·`categories`(장르)만 보조로 채움. 카카오가 실패/결과없음이면 Google Books 결과로 전체를 대체(기존 폴백 유지) | 카카오·네이버 응답엔 페이지수·장르 필드 자체가 없어(비교 검토 결과) "카카오 우선, 실패시만 구글북스"만으로는 페이지수가 거의 항상 수동입력으로 남음. 두 API를 병렬 호출해 필드별로 합성하면 지연시간 증가 없이 자동완성률을 높일 수 있음. 카카오는 REST API 키 필요(카카오 디벨로퍼스에서 무료 발급 후 `local.properties`/`BuildConfig`에 보관, 저장소에 커밋하지 않음) |
| 이미지 로딩 | Coil (표지 썸네일) | |
| 차트 | Compose `Canvas`로 직접 구현하는 막대그래프 (외부 차트 라이브러리 미사용) | 필요한 게 전부 단순 막대그래프 형태(일별 합산 + 통계 화면의 속성별 막대)뿐이라 Vico 등 의존성 추가보다 ~100줄 커스텀 Canvas가 더 가볍고, 데이터 버켓팅/집계 로직은 순수 Kotlin 함수로 분리해 단위 테스트 가능 |
| 내비게이션 | Jetpack Compose Navigation, 단일 Activity | |
| 리마인더 | `AlarmManager`(비정확 알람, `setAndAllowWhileIdle`) + `NotificationManager` + DataStore Preferences(설정 저장) | 클라우드/서버 푸시 없이 기기 로컬 알람만으로 구현 가능. 분 단위 오차는 독서 리마인더 용도에 무해하다고 보고 정확 알람(Android 12+ `SCHEDULE_EXACT_ALARM` 권한 필요)은 피해 권한 요청 절차를 단순화 |
| JSON 직렬화 | kotlinx.serialization | Retrofit 컨버터와 백업 내보내기/가져오기(JSON) 양쪽에 동일 라이브러리 재사용, 별도 JSON 라이브러리 추가 안 함 |
| 백업/복원 | Storage Access Framework(`ACTION_CREATE_DOCUMENT`/`ACTION_OPEN_DOCUMENT`) + kotlinx.serialization JSON | 클라우드 없이도 사용자가 원하는 위치(구글드라이브 문서공급자 포함, SAF가 지원하는 곳 어디든)에 수동 백업 가능. 별도 저장소 권한 불필요(SAF가 스코프드 접근 제공) |
| SDK | minSdk 26, target/compile SDK 35 | ML Kit 한글 인식·CameraX 안정 지원 범위 |

## 비주얼 디자인 원칙 (Apple HIG 스타일 + AI 슬롭 방지)

사용자가 제공한 Apple 디자인 시스템(HIG 마케팅 사이트 기준, 검증된 토큰 문서)을 채택하되, **네이티브 Android 앱이라 웹 전용 요소는 Compose 등가물로 번역**한다. 색/타이포/컴포넌트 규칙은 Apple 문서를 따르고, "AI 슬롭 방지"에 관한 구조적 원칙(카드 중첩 금지, 이모지 금지 등)은 기존대로 유지 — 오히려 Apple 문서의 보이스/상태 가이드가 그 원칙들을 더 구체화해준다.

**Android 번역 시 제약 3가지**:
1. **SF Pro는 Apple 독점 폰트**라 라이선스상 Apple 플랫폼 외 앱에 번들 불가 — 한글은 물론 라틴 문자·숫자까지 앱 전체를 **Pretendard**(OFL 오픈소스, 한글 최적화 + 라틴 커버리지 양호)로 통일. `res/font/`에 정적 TTF(Regular/Medium/SemiBold/Bold, 실제 쓰는 4개 굵기만)를 번들링.
2. 문서의 타이포 스케일은 apple.com **마케팅 웹사이트** 기준(56px 히어로 등)이라, 우리 앱(모바일 유틸리티 앱)엔 그대로 안 쓰고 Compose `Typography` 슬롯에 축소 매핑.
3. `backdrop-filter: blur(20px)` 프로스트 글래스는 minSdk 26에서 일관 지원 안 됨(진짜 배경 블러는 API 31+ RenderEffect 필요) — 상단바는 반투명 단색으로 근사, 블러는 API 31+ 조건부 향상으로 남겨둠.

### 색상 시스템

| 역할 | Light | Dark | 용도 |
|---|---|---|---|
| Primary (Apple Blue) | `#0071e3` | `#0071e3` | 주 버튼, 링크, 진행률 도넛 링, "오늘" 강조, FAB, 포커스 링 — **유일한 유채색 액센트**(Apple 원칙: 한 화면엔 하나의 액센트 컬러만) |
| On Primary | `#ffffff` | `#ffffff` | |
| Background/Canvas | `#f5f5f7` | `#000000` | 화면 배경 — Apple의 binary 라이트그레이/블랙 리듬 |
| Surface(카드/시트) | `#ffffff` | `#000000`(콘텐츠 카드는 대비용 `#1d1d1f`) | |
| On Background/Surface | `#1d1d1f` | `#ffffff` | 본문 텍스트 |
| Secondary text | `rgba(0,0,0,0.8)` | `rgba(255,255,255,0.8)` | 보조 텍스트 |
| Muted/tertiary | `rgba(0,0,0,0.48)` | `rgba(255,255,255,0.48)` | 캡션, 비활성 |
| Link | `#0066cc` | `#2997ff` | 인라인 텍스트 링크 |
| Hairline/구분선 | `rgba(0,0,0,0.10)` | `rgba(255,255,255,0.10)` | 구분선(카드 테두리 대신 사용) |
| **Status Good**(문서에 없어 iOS 시스템 컬러로 보강) | `#34C759` | `#30D158` | **오직** 목표 달성/완독처럼 "성공" 의미가 있을 때만 |
| **Status Critical**(문서에 없어 iOS 시스템 컬러로 보강) | `#FF3B30` | `#FF453A` | 책 삭제/데이터 가져오기 같은 파괴적 확인 다이얼로그의 강조 텍스트 |

이 값들을 `ui/common/theme/Color.kt`에 Compose `ColorScheme`로 직접 매핑하고, `MaterialTheme.colorScheme`의 다이나믹 컬러(기기별 랜덤 색)는 끈다.

### 타이포그래피 (Pretendard)

Apple 문서의 마케팅 스케일(56px 히어로 등)을 모바일 앱 UI에 맞게 Compose `Typography` 슬롯으로 축소 매핑 — SF Pro Display/Text 구분 대신 Pretendard 하나로 전부 처리(굵기로 위계 표현).

| Compose 슬롯 | 원본(Apple 역할) | size(sp) | weight | lineHeight | letterSpacing | 실제 용도 |
|---|---|---|---|---|---|---|
| displayLarge | Display Hero | 48 | SemiBold(600) | 1.07 | -0.28sp | 대시보드 "오늘 읽은 페이지" 히어로 숫자 |
| headlineMedium | Nav Heading | 28 | SemiBold | 1.2 | -0.3sp | 화면 타이틀(설정/라이브러리/통계) |
| titleLarge | Card Title | 21 | Bold(700) | 1.19 | 0.2sp | 책 상세 헤더(제목) |
| titleMedium | Tile Heading | 17 | Medium(500, Apple 원본 400보다 한 단계 굵게 — Pretendard Regular가 SF Pro Text Regular보다 살짝 얇게 보여 보정) | 1.14 | 0.2sp | 책장 그리드 표지 아래 제목 |
| bodyLarge | Body | 16 | Regular | 1.47 | -0.2sp | 본문 |
| bodyMedium | Body Emphasis | 16 | SemiBold(600) | 1.24 | -0.2sp | 강조 라벨, 최근 기록 값 |
| labelLarge | Button | 16 | Medium | — | 0 | 버튼 텍스트 |
| labelMedium | Caption | 13 | Regular | 1.29 | -0.1sp | 보조 설명, 타임스탬프("3분 전") |
| labelSmall | Micro | 11 | Regular | 1.33 | 0 | 캡션, 배지("?" 등) |

원본 문서의 "Nano"(10px, 법률 고지용)는 우리 앱에 쓸 곳이 없어 제외. 화면마다 이 슬롯 밖의 임의 크기/굵기를 새로 만들지 않는다(기존 "타이포그래피 절제" 원칙 유지, 슬롯만 Apple 값으로 교체).

### 컴포넌트 스타일 (Apple 레시피 적용)

- **버튼**: 마케팅성 주 액션(책 등록 FAB, 온보딩)은 **필(pill) 모양**(`RoundedCornerShape(percent = 50)`, Apple의 980px radius와 동일 효과) + Primary 배경 + 흰 텍스트. 시트/폼 내부의 조밀한 액션(빠른 기록 시트 "저장", 확인 다이얼로그 버튼)은 Apple의 "Commerce Compact" 레시피를 따라 8dp 라운드 + 작은 패딩 — 문서에서도 마케팅 페이지와 결제 플로우가 서로 다른 버튼 지오메트리를 쓰는 것과 동일한 논리(화면 성격에 따라 둘 중 하나를 명확히 선택, 섞지 않음).
- **카드**: 배경색 대비만으로 뜨는 스타일 — **테두리 없음, 그림자 없음(라이트 모드 제외)**, 반경 28dp. 라이트 모드에서만 Apple 카드 그림자(`rgba(0,0,0,0.22) 3px 5px 30px`)를 Compose `Modifier.shadow(elevation)`로 근사 적용(정확한 CSS box-shadow 복제는 아니고 근사치임을 인지). 다크 모드는 그림자 대신 카드가 배경(`#000000`)보다 살짝 밝은 표면색(`#1d1d1f`)으로 대비를 냄 — Apple 문서의 "다크 카드는 그림자가 아니라 표면색 대비로 뜬다" 원칙 그대로.
- **인풋**: 배경 채움(라이트 `#e8e8ed` 상당의 `surfaceVariant`) + 테두리 없음, 포커스 시에만 2dp Primary 테두리.
- **내비게이션(상단바)**: 반투명 배경(라이트 `rgba(250,250,252,0.8)`, 다크는 `rgba(0,0,0,0.8)`) — 진짜 블러는 위 제약 3번 참고, minSdk 26 기본은 단색 반투명, API 31+에서만 `Modifier.graphicsLayer`+`RenderEffect`로 블러 추가하는 조건부 향상.

### 카피 톤 (Apple 보이스 원칙 반영)

기존 "마케팅 어투 금지" 원칙을 Apple의 실제 보이스 가이드로 구체화:
- 짧은 평서문, 마침표로 끝남(느낌표 없음). "책 정보를 찾지 못했어요, 직접 입력해주세요"처럼 **구체적 원인 + 구체적 다음 행동**.
- 과장된 최상급 금지("최고의", "완벽한" 등 없음), 이모지 전면 금지(기존 원칙 유지).
- 에러 메시지는 절대 "문제가 발생했습니다" 같은 뭉뚱그린 표현 안 씀 — 항상 원인이 구체적(오프라인/검색결과없음 등, 이미 설계된 실패 처리 매트릭스가 이 원칙에 부합).

### 상태(States) — 기존 "로딩/대기 상태 UX"에 보강

Apple 문서의 States 표를 우리 설계와 대조해 빠진 부분만 추가:
- **성공(루틴 커밋)**: 빠른 기록 시트 저장처럼 일상적인 액션은 토스트/스낵바 없이 **저장 버튼 자리에 짧게(≈300ms) 체크마크가 스프링으로 나타났다 시트가 닫히는** 조용한 피드백으로 처리(기존 계획엔 "자동 닫힘"만 있었고 이 마이크로 피드백이 빠져있었음). 삭제처럼 되돌릴 수 있어야 하는 액션만 기존대로 스낵바+실행취소 유지(이건 예외적으로 여전히 필요 — Apple 문서엔 없는, 우리 앱 특유의 요구사항).
- 나머지(스켈레톤 지오메트리 고정, 빈 상태에 일러스트 없음, 에러 메시지 구체성)는 기존 "로딩/대기 상태 UX" 섹션이 이미 Apple 문서와 같은 방향이라 변경 없음.

### 모션 (Compose spring 매핑)

Apple 문서의 스프링 물리 기반 모션은 Compose의 기본 애니메이션 철학과 정확히 일치 — Compose `spring()` API로 직접 매핑:
- 표준 전환(시트 열림/닫힘, 화면 전환): `spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)` — Apple 문서가 명시적으로 경계하는 "만화 같은 오버슈트" 없이 자연스럽게 정착.
- 진행률 도넛 링 값 변경, 표지 이미지 crossfade: `tween(300ms)` 또는 위와 동일한 spring — 의미 있는 상태 변화에만 사용(기존 "모션 절제" 원칙 유지).
- **접근성**: 시스템 "애니메이션 사용 안 함" 설정(`Settings.Global.ANIMATOR_DURATION_SCALE == 0`) 감지 시 모든 spring 모션을 즉시 전환/크로스페이드로 대체 — Apple 문서의 Reduce Motion 요구사항과 동일한 취지.

### UI 전반 원칙 (기존 유지)

- **카드 중첩 금지**: 바텀시트/카드 안에 또 카드, 그 안에 또 카드를 겹겹이 쌓지 않는다 — `BookQuickActionSheet`처럼 이미 elevated된 컨테이너 안의 하위 요소는 별도 카드로 감싸지 않고 여백과 hairline 구분선만으로 구획.
- **간격 스케일 고정**: 4dp 배수(4/8/12/16/24/32)만 사용, 화면마다 다른 임의 padding 값 금지.
- **아이콘 일관성**: Material Symbols(outlined) 한 세트만 사용, filled/outlined 혼용 안 함. **UI 크롬(버튼, 타이틀, 빈 상태)에 장식용 이모지 사용 안 함**. 아이콘은 의미 전달용으로만.
- **빈 상태(EmptyState)**: 마스코트 일러스트 없이 텍스트 + 필요하면 아이콘 하나만("아직 읽는 중인 책이 없어요" + 책 등록 버튼).
- **일관된 컴포넌트 재사용**: `BookCoverImage`/`SkeletonBox`/`LoadingOverlay`/`EmptyState`/`AttributeBarChart`처럼 이미 설계된 공용 컴포넌트를 화면마다 새로 만들지 않고 그대로 재사용.
- **접근성(TalkBack) 기본 원칙**: 도넛 진행률 링처럼 시각 정보만으로 전달되는 요소는 `Modifier.semantics { contentDescription = "$title, ${progress}% 진행" }`으로 스크린리더가 값을 읽게 함. 아이콘 전용 버튼(카메라, 연필, 휴지통 등)엔 빠짐없이 `contentDescription` 지정. 터치 타깃은 최소 48dp 유지. 시스템 폰트 확대 설정 시 텍스트가 잘리지 않도록 고정 높이 `Box`에 텍스트를 강제로 욱여넣지 않고 `wrapContentHeight` 기본값 사용.

### 차트 색상 규칙 (`dataviz` 스킬 + Apple 팔레트)

액센트가 Apple Blue(`#0071e3`) 하나뿐이라는 사실 자체가 `dataviz` 스킬의 "카테고리마다 다른 색 금지, 액센트는 하나" 원칙과 정확히 맞아떨어짐 — hex 값만 교체하고 규칙은 그대로 유지:

- **`DailyPagesBarChart`(일별 합산 막대)**: 막대는 전부 **Primary(#0071e3) 한 가지 색**만 쓰고, "오늘" 막대만 진하게/나머지는 흐리게(Emphasis). 목표 달성한 날의 막대만 **Status Good**로 전환(성공 의미가 실제 있을 때만, Primary와 절대 혼용 안 함). 목표선은 임계값(threshold) 표시라 점선 유지(장식성 그리드 점선 금지 규칙과는 별개 — 의미 있는 단일 기준선은 관례적으로 점선이 맞음).
- **통계 화면 `AttributeBarChart`(장르/작가/출판사/국가별)**: **절대 카테고리마다 다른 색을 칠하지 않는다** — 막대 길이(=권수)가 이미 전달하는 정보를 색으로 이중 인코딩하면 오히려 정보가 없어짐. 모든 막대를 **Primary 한 가지 색**으로 통일.
- **진행률 도넛 링**: 카테고리 비교용 파이차트가 아니라 단일 값 게이지이므로 파이차트 관련 안티패턴은 해당 없음.
- **공통 마크 규칙**: 얇은 막대 + 막대 사이 여백(테두리로 구분 안 함), 축/그리드는 hairline로 은은하게, 필요한 값만 선택적으로 직접 라벨링.
- **스켈레톤**: "로딩/대기 상태 UX" 섹션대로 **최초 컴포지션 1회만**, 이후 리페치 시엔 스켈레톤으로 되돌아가지 않고 이전 값 유지한 채 자연스럽게 전환.

## 데이터 모델 (Room)

**마이그레이션 원칙**: `BooklogsDatabase`는 버전 1부터 시작해, 이후 스키마를 바꿀 때마다(컬럼 추가/삭제, 테이블 추가 등) 반드시 정식 `Migration(from, to)` 객체를 작성해 누적한다. **`fallbackToDestructiveMigration()`은 사용하지 않음** — 로컬 전용 앱이라 마이그레이션 실패나 스키마 불일치가 곧 사용자의 독서 기록 영구 손실로 이어지므로, 서버가 있는 앱과 달리 "그냥 지우고 다시 만들기"가 절대 안전하지 않음. 버전을 올릴 때마다 Room의 `MigrationTestHelper` 기반 마이그레이션 테스트를 작성하는 걸 원칙으로 하되, 이건 계측 테스트(instrumented test)라 이 샌드박스에선 실행 불가 — 사용자가 실기기/에뮬레이터 확보 후 검증. 지금 계획 문서상 스키마가 여러 차례 바뀌었지만, 실제 구현은 최종 형태를 스키마 버전 1로 잡고 시작하면 되므로 지금까지의 설계 변경 이력 자체를 마이그레이션으로 재현할 필요는 없음.

- **BookEntity** (`books`): id, isbn?(인덱스 — 중복 등록 감지용 조회에 사용), title, author?, publisher?, coverImageUrl?, totalPages?, status(READING/PAUSED/FINISHED/DROPPED/PLANNED), **format**(PHYSICAL/EBOOK, 기본 PHYSICAL), **genre?**, **country?**, createdAt
  - country는 어떤 API도 제공하지 않는 정보라 항상 수동 입력. genre는 카카오엔 필드 자체가 없지만, **Google Books를 항상 보조로 병렬 호출**하므로 그 응답에 `categories`가 있으면 자동완성됨(실패해도 무방, 사용자가 직접 채우거나 비워둘 수 있음). 둘 다 비워두면 통계 화면에서 "미상"으로 집계.
  - READING: 대시보드 책장에 노출, 진행 중
  - PAUSED: 일시중지 — 나중에 재개할 생각 있음. 같은 라운드를 유지한 채 책장에서만 빠짐
  - FINISHED: 완독
  - **DROPPED**: 중단 — 재개할 생각 없음. FINISHED와 마찬가지로 "라운드 종료"이지만 종료 사유가 다름(아래 참고)
  - PLANNED: 아직 시작 전
  - **format**: 전자책은 페이지 대신 %로 진행률을 표시하는 매체 특성 반영. 단, `ReadingLogEntity.currentPage`는 형식에 상관없이 항상 페이지 기준으로 저장(아래 "빠른 기록 UX" 참고) — 델타 계산/일별 합산/진행률 도넛 등 핵심 로직은 종이책·전자책 구분 없이 동일하게 동작하고, 입력창과 이력 표시만 format에 따라 %/페이지로 갈라짐
- **ReadingRoundEntity** (`reading_rounds`): id, bookId(FK), roundNumber, startedAt, finishedAt?, **endReason?**(COMPLETED/DROPPED, 라운드가 끝났을 때만 값이 들어가고 진행/일시중지 중엔 null) — 재독마다 새 라운드 생성. 책 하나에 독후감이 여러 개 쌓이는 요구사항을 라운드 단위로 깔끔하게 표현(라운드별 진행 이력 + 독후감 그룹핑). 재독 시작 시 새 라운드를 만들면 페이지 델타 계산도 라운드 내에서만 비교하므로 "재독으로 페이지가 1로 돌아갈 때 음수 델타" 같은 엣지케이스가 자연히 해결됨.
  - **완독과 중단은 같은 메커니즘**(라운드 종료, `finishedAt` 기록)이고 `endReason`만 다름. 일시중지(PAUSED)는 라운드를 종료하지 않고 그대로 열어둔 채 책 상태만 바꾸는 것이라 서로 구분됨.
  - FINISHED/DROPPED 상태에서 다시 읽기 시작하면(재독/재도전) 기존 라운드는 그대로 두고 `roundNumber`가 1 증가한 **새 라운드**를 생성 — 1차 시도의 진행 이력·독후감은 보존되고 2차 시도가 새로 쌓임. PAUSED에서 재개하면 라운드를 새로 만들지 않고 열려있던 라운드를 그대로 이어감.
- **ReadingLogEntity** (`reading_logs`): id, bookId(FK), readingRoundId(FK), currentPage, logDateEpochDay(타임존 안정적인 일자 버켓용), loggedAt — bookId, logDateEpochDay에 인덱스. **델타는 컬럼으로 저장하지 않는다.** 각 로그는 "이 시점에 몇 페이지였다"는 스냅샷일 뿐이고, 페이지 증가량(델타)은 항상 같은 라운드의 로그들을 `loggedAt` 순으로 정렬해 인접한 두 로그의 차이로 그때그때 계산한다(clamped ≥0). 이렇게 델타를 파생값으로만 다루면 과거 로그를 수정/삭제해도 그 행 하나만 갱신/삭제하면 끝이고, 이웃 로그의 저장된 델타를 다시 써넣는 연쇄 작업이 필요 없다 — **어떤 시점의 로그든 자유롭게 수정/삭제 가능**. 개인 독서기록 규모(수백~수천 행)에서 매번 정렬+차이계산하는 비용은 무시할 수준.
- **QuoteEntity** (`quotes`): id, bookId(FK), text, pageNumber?, **pageNumberEnd?**(두 페이지 이상에 걸친 인용구일 때만 값이 들어감, 단일 페이지 인용구는 null — 상세에서 "185p" 또는 "185-186p"로 표시), createdAt
- **ReviewEntity** (`reviews`): id, bookId(FK), readingRoundId(FK, **non-null**), content, rating?, createdAt — 독후감은 항상 특정 라운드에 연결된다는 설계(화면 흐름 #6)와 일치시키기 위해 nullable로 두지 않음. 작성 화면은 항상 어떤 라운드에 대한 독후감인지 알고 진입하므로(책 상세의 특정 라운드에서 "독후감 작성") 이 FK가 비어있을 상황 자체가 없음.

모든 FK는 `onDelete = CASCADE`. 사진 원본은 디스크에 영구 저장하지 않음(OCR 처리는 메모리상에서, 표지는 Coil 캐시로 충분) — 저장공간 최소화, STORAGE 권한 불필요.

## 패키지 구조

```
com.dyk1323.booklogs/
  data/
    local/{BooklogsDatabase, dao/*, entity/*}
    remote/{KakaoBooksApi, GoogleBooksApi, dto/*, BookMetadataMapper — 순수 매핑 전담: 카카오/Google Books DTO 각각을 domain 필드로 변환하고, `merge(kakaoDto?, googleDto?)`로 두 DTO를 하나의 BookMetadata로 합성(어떤 DTO를 넘길지는 Repository가 결정, Mapper는 주어진 값을 합치기만 함)}
    repository/{Book,ReadingLog,Quote,Review,BookMetadata}Repository(+Impl) — `BookRepository`에 `findByIsbn(isbn): BookEntity?` 추가(중복 등록 감지용)
    settings/ (AppSettingsDataStore — DataStore Preferences, Room이 아님. reminderEnabled: Boolean, reminderHour/Minute: Int, **dailyGoalPages: Int?**)
    backup/ (BackupExporter, BackupImporter, dto/BackupEnvelope — schemaVersion 포함 JSON 스키마. Room 5개 테이블 전체를 하나의 JSON으로 직렬화/역직렬화. 사진/이미지 바이너리는 애초에 디스크에 저장 안 하므로 백업 대상에서 자연히 제외)
  domain/
    model/ (Book, ReadingLog, Quote, Review, ReadingRound — Room 엔티티와 분리된 순수 모델)
    usecase/
      ComputeLogDeltasUseCase.kt      ← 핵심 파생 로직, 순수 함수: 한 라운드의 로그 목록(loggedAt순)을 받아 인접 쌍의 차이를 delta로 변환(음수 클램프). AggregateDailyPagesUseCase와 라운드 진행 이력 표시(책 상세) 양쪽에서 재사용
      AggregateDailyPagesUseCase.kt   ← 핵심 요구사항(#1), 순수 함수, 최우선 단위테스트 대상. 전체 로그를 라운드별로 묶어 ComputeLogDeltasUseCase로 델타를 얻은 뒤 각 델타를 "뒤쪽 로그"의 logDateEpochDay에 귀속시켜 날짜별 합산(요청 기간 밖의 이전 로그도 델타 계산엔 포함해야 경계의 첫 델타가 정확함)
      LogProgressUseCase.kt           ← 새 로그 저장(단순 insert, currentPage+timestamp만 기록, 델타 계산 없음)
      EditLogUseCase.kt               ← 임의 로그의 currentPage 수정(단순 update, 이웃 로그 재계산 불필요 — 델타가 저장되지 않으므로)
      DeleteLogUseCase.kt             ← 임의 로그 삭제(단순 delete, 이웃 로그 재계산 불필요)
      ComputeBookProgressUseCase.kt   ← 진행률 %. 두 가지 "값 없음" 케이스를 구분: `totalPages`가 null이면 퍼센트 자체를 계산 불가(도넛 대신 "?" 배지), `totalPages`는 있는데 아직 로그가 0건이면 `currentPage`를 0으로 간주해 0%로 계산(도넛은 정상 표시, 그냥 시작 전 상태)
      ConvertPagePercentUseCase.kt    ← 순수 함수, 양방향 변환: `percentToPage(percent, totalPages)`(EBOOK 입력 저장 시, round 처리)와 `pageToPercent(currentPage, totalPages)`(EBOOK 이력 표시 시). totalPages가 null이면 변환 불가 예외/Result로 처리 → 호출부(빠른 기록 시트)가 먼저 총 페이지 입력을 유도
      ChangeBookStatusUseCase.kt      ← 상태 전이 처리(READING↔PAUSED는 라운드 유지, →FINISHED/DROPPED는 현재 라운드 종료+endReason 기록, FINISHED/DROPPED→READING은 새 라운드 생성). 전이 종류별 라운드 부수효과를 한 곳에 모아 화면(책 상세)에서는 단순 호출만 하도록 함
      PickReminderBookUseCase.kt      ← 순수 함수: READING 상태 책 목록을 받아 무작위로 1권 선택(빈 목록이면 null 반환 → 리시버가 알림을 건너뜀)
      AggregateBooksByAttributeUseCase.kt ← 통계 화면용 순수 함수: 책 목록 + 키 추출 함수(genre/author/publisher/country)를 받아 그룹별 권수를 세고 내림차순 정렬, 상위 N개 외엔 "기타"로 묶음. null/빈 값은 "미상"으로 그룹핑. 장르/작가/출판사/국가 4개 차트가 모두 이 함수 하나를 재사용(키 추출 함수만 다름)
      DeleteBookUseCase.kt            ← 책 삭제(Room `onDelete=CASCADE`로 라운드/로그/인용구/독후감 전부 함께 삭제됨). 파괴적 작업이라 호출 전 확인은 UI(책 상세) 책임
  ui/
    dashboard/ (DashboardScreen, DashboardViewModel, TodayPagesHero, DailyPagesBarChart, BookShelfGrid, BookCoverProgressRing, BookQuickActionSheet, QuickProgressEntryViewModel)
    registration/ (BookRegistrationScreen, BarcodeScanScreen, BarcodeAnalyzer, TitleSearchScreen, BookConfirmFormScreen, BookRegistrationViewModel)
    bookdetail/ (BookDetailScreen, BookDetailViewModel, ReadingRoundSection, QuoteListSection, ReviewListSection, BookStatusActions, DeleteBookConfirmDialog — "진행률 기록"/"인용구 추가" 버튼은 dashboard의 BookQuickActionSheet/QuoteCaptureScreen을 그대로 재사용)
    library/ (LibraryScreen, LibraryViewModel — 상태별 필터가 가능한 전체 책 목록. PAUSED/DROPPED/FINISHED/PLANNED 책은 대시보드 책장(READING 전용)에 안 나오므로 이 화면이 유일한 접근 경로)
    stats/ (StatsScreen, StatsViewModel, AttributeBarChart — 장르별/작가별/출판사별/국가별 4개 섹션이 모두 이 컴포저블 하나를 재사용)
    quote/ (QuoteCaptureScreen, TextRecognitionAnalyzer, QuoteTextSelectionScreen, QuoteCaptureViewModel — bookId 파라미터로 대시보드/책상세 양쪽에서 직접 진입 가능. `QuoteCaptureViewModel`은 캡처된 사진별 인식 결과를 `List<CapturedPageOcrResult>`로 누적 보관해 여러 페이지에 걸친 인용구를 지원)
    review/ (ReviewEditorScreen, ReviewEditorViewModel)
    settings/ (SettingsScreen, SettingsViewModel — 리마인더 on/off 토글 + TimePicker, 일일 목표, **데이터 내보내기/가져오기**)
    common/{theme, components/(SkeletonBox, LoadingOverlay, EmptyState, BookCoverImage)}
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
  - 시트를 열면 **진행률 입력이 바로 최상단에 노출**된다: 숫자 입력창에 마지막으로 기록한 값이 **전체 선택된 상태**로 프리필되고 숫자 키패드가 자동으로 뜬다. 사용자는 지금 보고 있는 값을 그대로 타이핑해서 덮어쓰기만 하면 됨 — 마지막 기록과의 차이를 암산할 필요가 없다(이전 버전의 `+5/+10/+20/+50` 델타 칩은 "지금 보는 페이지"가 아니라 "마지막 기록 대비 얼마나 더 읽었는지"를 계산하게 만들어 오히려 번거로워서 폐기). 델타는 저장되지 않고 필요할 때 파생되므로 사용자는 신경 쓸 필요 없음.
    - **책 형식(`format`)에 따라 입력 단위가 다름**: PHYSICAL(종이책)이면 "현재 페이지"(절대 페이지 번호) 입력, EBOOK(전자책)이면 "현재 몇 %"(0~100 정수) 입력 — 전자책은 대개 페이지가 아니라 %로 진행률이 표시되기 때문. 저장 시 EBOOK은 내부적으로 `currentPage = round(퍼센트 / 100 × totalPages)`로 환산해 `ReadingLogEntity`에 페이지로 저장하므로, 이후 델타/합산/도넛 로직은 종이책과 완전히 동일하게 동작(입력창만 다름).
    - PHYSICAL 책은 입력창 바로 옆에 카메라 아이콘 버튼을 동급 옵션으로 배치: 탭하면 카메라가 열리고 페이지 사진을 찍어 OCR로 추출한 숫자를 같은 입력창에 채워준 뒤 확인/저장 흐름으로 복귀. 타이핑도 촬영도 둘 다 "암산 없이 보이는 숫자 그대로"라는 점에서 동등한 주 경로이므로, 촬영을 보조 링크로 숨기지 않고 나란히 노출한다. **EBOOK은 이 카메라 옵션을 숨김**(전자책 화면 캡처로 % OCR을 시도할 이유가 없어 스코프에서 제외).
    - 입력창 아래에 **"최근 기록: 245p · 3분 전"**(PHYSICAL) 또는 **"최근 기록: 62% · 3분 전"**(EBOOK, `currentPage/totalPages`를 %로 재환산해 표시)처럼 그 책의 가장 최근 로그를 작게 보여주고, 옆에 연필(수정)·휴지통(삭제) 아이콘을 둔다. 방금 낸 오타를 새 로그를 또 쌓지 않고 바로 잡기 위한 용도(가장 흔한 실수 케이스라 대시보드에서 화면 전환 없이 처리).
      - 연필 탭 → 입력창이 그 로그 값(PHYSICAL은 `currentPage`, EBOOK은 그 페이지를 다시 %로 환산한 값)으로 채워지고 "저장" 버튼이 "수정 저장"으로 바뀜 → 저장 시 새 로그를 추가하지 않고 기존 로그의 `currentPage`만 갱신(`EditLogUseCase`, 델타가 저장되지 않으므로 이웃 로그를 건드릴 필요 없음).
      - 휴지통 탭 → 확인 다이얼로그 없이 즉시 삭제하고 하단에 "기록 삭제됨 · 실행취소" 스낵바를 몇 초간 노출(`DeleteLogUseCase`) — 빠른 기록이라는 원칙에 맞게 모달 확인 대신 실행취소로 되돌릴 수 있게 함.
      - **과거 날짜의 로그는 이 시트가 아니라 책 상세의 진행 이력 목록에서 수정/삭제**(아래 4번 참고) — 대시보드 시트는 "방금 기록한 것 즉시 정정"용으로 최근 1건만 인라인 노출하고, 여러 날짜에 걸친 로그를 훑어보며 고치는 건 빈도가 낮은 딥다이브 작업이라 책 상세로 위치시킴. 스키마상 제약은 없으므로(델타 비저장) 언제든 어떤 로그든 같은 `EditLogUseCase`/`DeleteLogUseCase`로 처리.
  - 시트 내 "인용구 추가" 버튼: 탭하면 책 상세를 거치지 않고 곧바로 카메라(인용구 캡처)로 진입 — **대시보드 → 카메라 1홉**. 인용구 저장 후에는 "계속 촬영" / "완료"를 선택하게 해, 한 번에 여러 인용구를 찍을 때마다 대시보드로 돌아왔다 다시 들어가지 않아도 되게 한다.
  - 시트 내 "책 상세보기" 버튼: 진행 이력·독후감처럼 가끔 보는 딥다이브용 화면으로 이동하는 세 번째(가장 낮은 빈도) 옵션.
- `totalPages`가 없는 책은 도넛 대신 "?" 배지를 표시하고, 시트를 열면 총 페이지 수 입력을 먼저 유도.

## 로딩/대기 상태 UX

**원칙**: 로컬 DB(Room `Flow`) 읽기는 대부분 즉시 응답하므로 별도 로딩 처리가 크게 중요하지 않지만, **최초 컴포지션 시 빈 화면이 한 프레임이라도 깜빡이지 않도록 항상 스켈레톤을 먼저 그리고** 첫 `Flow` emission이 오면 실제 콘텐츠 또는 빈 상태(EmptyState)로 교체한다. 반면 **네트워크(카카오/Google Books)나 온디바이스 연산(ML Kit OCR·바코드, CameraX 초기화)이 관여하는 지점은 반드시 명시적 로딩 표시**가 있어야 한다 — 결과 모양(목록/여러 필드)을 미리 아는 곳은 스켈레톤, 버튼 탭 같은 단발 액션은 스피너.

- **`SkeletonBox`**(공용 컴포저블): 회색 사각형에 무한 반복 알파 애니메이션(`rememberInfiniteTransition`)을 입힌 shimmer placeholder. 외부 라이브러리(예: accompanist-placeholder) 없이 ~20줄로 직접 구현 — 차트와 동일하게 "필요한 건 딱 이거 하나"라 의존성 추가 안 함.
- **`LoadingOverlay`**(공용 컴포저블): 반투명 검정 배경 + 중앙 `CircularProgressIndicator` + 안내 텍스트. 전체 화면/카메라 프리뷰 위에 덮어써서 입력을 막아야 하는 블로킹 대기(OCR 처리 등)에 사용.
- 화면별 적용:
  - **대시보드**: `TodayPagesHero`/`DailyPagesBarChart`/`BookShelfGrid` 모두 최초 컴포지션엔 `SkeletonBox`(막대그래프는 회색 막대 더미, 책장은 회색 카드 3~4개)로 시작 → 첫 `Flow` emission 시 실제 데이터 또는 "아직 읽는 중인 책이 없어요" EmptyState로 교체.
  - **바코드 스캔**: `PreviewView` 초기화가 지연되면 카메라 프리뷰 위에 짧게 `LoadingOverlay`("카메라 준비 중"). 바코드 디코드 성공 후 카카오+Google Books 조회가 도는 동안(최악 5초) 확인 폼으로 넘어가기 전 `LoadingOverlay`("책 정보를 찾고 있어요") 노출 — 타임아웃/실패 시 "구현 시 유의사항"의 메타데이터 실패 처리 분기에 정의된 메시지로 전환.
  - **제목 검색**: 검색 제출 시 결과 리스트 자리에 카드 5~6개 형태의 `SkeletonBox` 리스트를 먼저 그림 → 카카오(필요 시 Google Books 폴백) 응답 도착 시 실제 결과 또는 "검색 결과가 없어요"로 교체.
  - **책 확인/수정 폼**: 카카오 필드는 검색 결과 선택 즉시 채워져 폼이 바로 열리고, `totalPages`/`genre` 입력칸만 Google Books 보조 조회가 끝날 때까지 그 칸 자리에 작은 `SkeletonBox`를 표시(폼 전체를 막지 않음 — 사용자가 다른 필드를 먼저 수정해도 무방) → 응답 도착 시 값 채움 또는 빈 칸 유지.
  - **인용구 캡처**: 촬영 버튼 탭 직후 이미지 캡처+Latin/Korean 두 인식기 처리 동안 `LoadingOverlay`("텍스트 인식 중…")로 화면을 덮고 촬영 버튼 비활성화(중복 촬영 방지) → 완료 시 텍스트 선택 화면으로 전환. "다음 페이지 이어서 촬영"도 매 촬영마다 동일하게 적용.
  - **책 상세**: 진행 이력/인용구/독후감 각 섹션이 독립적으로 최초엔 `SkeletonBox` 리스트 → 각자의 `Flow` emission이 오는 대로 개별 교체(로컬 DB라 사실상 동시에 채워지지만 섹션 단위로 독립 처리해 구조를 단순하게 유지).
  - **라이브러리 / 통계**: 최초 컴포지션 시 `SkeletonBox`(라이브러리는 리스트, 통계는 막대) → `Flow` emission 후 실제 콘텐츠. 필터/검색은 이미 메모리에 있는 리스트를 즉시 거르는 동기 연산이라 별도 로딩 불필요.
  - **표지 이미지**: 대시보드 책장·검색 결과·라이브러리·책 상세 등 표지가 나오는 모든 곳에서 Coil `AsyncImage`에 `placeholder`(회색 박스 또는 기본 책 아이콘) + `crossfade(true)`를 공통 설정 — 개별 화면마다 다르게 처리하지 않고 공용 `BookCoverImage` 컴포저블 하나로 통일.
  - **빠른 기록 시트 저장 / 상태 변경 버튼**: 로컬 Room insert/update라 보통 즉시 끝나지만, 중복 탭 방지를 위해 버튼을 짧게 비활성화하고 라벨을 스피너로 교체(예: "저장" → 작은 `CircularProgressIndicator`) 후 자동 닫힘.

## 화면 흐름

1. **대시보드(홈, 시작 화면) — "책장" 뷰**: 상단바(`TopAppBar`)엔 좌측에 타이틀, 우측에 **라이브러리 아이콘 + 통계 아이콘 + 설정 아이콘**을 직접 노출(햄버거+드로어 아님 — 아이콘 3개까지는 바로 누르는 게 탭 수가 적음, 더 늘어나면 그때 드로어로 전환 검토). 그 아래 본문은 위에서부터
   - **오늘 읽은 페이지** 큰 숫자로 강조 표시("오늘 128p"). 걷기 그래프의 "오늘 걸음 수"와 같은 위상. 일일 목표(`dailyGoalPages`)가 설정돼 있으면 "128 / 150p"처럼 목표를 옆에 같이 표시하고, 목표 달성 시 숫자를 강조색(성공 컬러)으로 전환.
   - **최근 7일 합산 페이지 막대그래프**(걷기 그래프 스타일): 오늘 막대는 강조색, 나머지는 흐린 색. 위 "오늘 읽은 페이지"와 이 그래프는 같은 데이터 소스(`AggregateDailyPagesUseCase`가 만드는 일자별 합산 배열) — 배열의 마지막(오늘) 항목을 숫자로 뽑아 보여주고 배열 전체를 막대로 그리는 것뿐, 별도 계산 불필요. 일일 목표가 설정돼 있으면 목표값 높이에 **점선 기준선**을 그려 넣어 날짜별로 목표 달성 여부를 한눈에 비교(목표를 넘긴 날의 막대는 성공 컬러로 표시).
   - **책장 그리드**: 진행 중인(`BookEntity.status == READING`) 책들을 표지로 배치 — 각 표지 이미지에 dim 오버레이(반투명 검정)를 씌우고, 그 위에 원형(도넛) 진행률 링을 겹쳐 그림(진행률 = currentPage/totalPages, `ComputeBookProgressUseCase` 재사용). **도넛 중앙에 퍼센트 텍스트("62%")를 함께 표시**해 시각적 링만으로 정확한 값을 가늠하기 어려운 문제를 보완 — 정확한 페이지 수(185/320p)는 표지 탭 시 열리는 빠른 기록 시트/책 상세에서 확인.
   - 표지 탭 → 위 "빠른 기록 UX"의 바텀시트가 열림(화면 전환 없음). 별도 FAB로 책 등록 화면 진입.
2. **책 등록**: 스캔/제목검색/수동입력 선택 → 스캔은 CameraX+ML Kit 바코드 인식 → ISBN으로 **카카오(메인) + Google Books(페이지수·장르 보조)를 병렬 조회**해 하나로 합성 → 확인/수정 폼(둘 다 실패 시 빈 폼+수동입력 안내) → 저장. 제목 검색도 동일하게 카카오로 검색해 결과 목록을 보여주고, 사용자가 하나를 고르면 그때 Google Books를 페이지수/장르용으로 추가 조회. **확인/수정 폼에 "종이책/전자책" 토글**을 추가(기본값 종이책, 메타데이터 API로는 형식을 알 수 없어 항상 수동 선택) — 전자책을 선택하면 이후 그 책의 빠른 기록 시트가 %입력 모드로 동작. **장르/국가 입력 필드**도 폼에 추가 — 장르는 위 병렬 조회로 자동완성되는 경우가 많고, 국가는 항상 수동, 둘 다 선택 입력이라 비워도 등록 가능(통계에서 "미상"으로 집계). **"바로 읽기 시작" / "읽을 예정으로 등록" 선택**도 폼에 포함(기본값은 "바로 읽기 시작"=READING) — READING으로 저장하면 즉시 첫 라운드 자동 생성, PLANNED로 저장하면 라운드 없이 등록만 되고 대시보드 책장엔 안 보임(라이브러리에서만 조회 가능, 나중에 책 상세에서 "읽기 시작"으로 첫 라운드 생성).
   - **중복 등록 감지**: ISBN을 알고 있는 시점(바코드 스캔은 항상, 제목 검색은 대부분)에 `BookRepository.findByIsbn`으로 이미 등록된 책인지 확인. 있으면 저장을 막지 않되 확인 폼 상단에 "이미 등록된 책이에요 · 『제목』" 안내 + "그 책으로 이동"(기존 책 상세로 이동, 재독이면 거기서 "다시 읽기 시작") / "그래도 새로 등록"(다른 사본을 별도로 추적하고 싶은 드문 경우) 선택지 제공. ISBN이 없는 수동 입력은 이 검사를 건너뜀(제목/저자 퍼지 매칭은 오탐 위험이 커서 범위에서 제외).
   - 저빈도 작업이므로 기존의 다단계 절차를 그대로 유지.
3. **진행률 빠른 기록 시트**: 위 "빠른 기록 UX" 참고. 대시보드 표지 탭으로 진입하는 것이 기본 경로이며, 책 상세 화면에서도 동일한 시트를 재사용해 진입 가능(어떤 경로든 "책 선택" 단계가 별도로 필요 없음 — 이미 어떤 책인지 알고 진입하므로). PHYSICAL 책의 사진 인식 모드는 인용구 캡처와 동일한 카메라+OCR 파이프라인을 재사용, 코너의 독립된 숫자 토큰을 페이지 후보로 추출해 프리필하되 항상 사용자 확인 후 저장. EBOOK 책은 % 직접 입력만 지원.
4. **책 상세**: 메타데이터, 진행률, 라운드별 진행 이력/독후감, 인용구 목록. 딥다이브용 화면이며 빠른 기록 시트에서 "상세보기"로 진입하거나, 검색/목록에서 직접 진입. **진행 이력 목록의 각 로그 행은 탭하면 인라인으로 펼쳐져 수정/삭제 가능**(대시보드 빠른 기록 시트와 동일한 `EditLogUseCase`/`DeleteLogUseCase` 재사용) — 과거 특정 날짜의 기록을 고치는 것은 이 화면이 담당. 이력의 각 행은 책 `format`에 따라 "245p" 또는 "62%"로 표시(저장된 값은 항상 페이지, 표시만 환산).
   - **상태 변경 액션**(`BookStatusActions`, `ChangeBookStatusUseCase` 호출): READING 중엔 "다 읽음" / "중단" / "일시중지" 세 버튼을 노출. "일시중지"는 라운드를 유지한 채 상태만 바꾸고, "다 읽음"/"중단"은 현재 라운드를 종료(각각 `endReason = COMPLETED`/`DROPPED`)하고 책 상태를 FINISHED/DROPPED로 바꿈. FINISHED/DROPPED/PAUSED 상태에선 "다시 읽기 시작" 버튼 하나로 재개(PAUSED는 같은 라운드 이어감, FINISHED/DROPPED는 새 라운드 시작). **PLANNED 상태에선 "읽기 시작" 버튼**(라운드가 아직 하나도 없는 책이라 첫 라운드를 생성 — FINISHED/DROPPED→READING과 동일한 메커니즘, 라벨만 다름). 등록/상태변경처럼 저빈도 작업이라 책 상세에 위치, 확인 다이얼로그 없이 즉시 적용 후 스낵바로 되돌리기 제공.
   - **책 삭제**(`DeleteBookConfirmDialog`, `DeleteBookUseCase` 호출): 화면 하단에 시각적으로 눈에 덜 띄게(에러 컬러 텍스트 버튼) 배치. 탭하면 "『책 제목』을 삭제하면 진행 기록·인용구·독후감이 모두 함께 사라집니다"를 알리는 **블로킹 확인 다이얼로그**를 띄움 — 다른 곳의 실행취소 스낵바 패턴과 달리, cascade로 지워지는 데이터 양이 크므로 삭제 전에 확실히 막아섬. 대시보드 빠른 기록 시트에서는 접근 불가(책 상세에서만).
   - → 독후감작성 등 저빈도 작업으로 이동
5. **인용구 캡처**: 대시보드 빠른 기록 시트 또는 책 상세에서 진입(둘 다 책 컨텍스트를 이미 알고 있어 책 선택 단계 없음). 단발 촬영(연속 프레임 아님, 정확도 우선) → Latin+Korean 인식기 동시 실행 → 인식된 줄(line) 단위 리스트를 체크박스로 선택("전체 선택" 토글도 제공, 문단 전체를 인용하는 경우가 많음).
   - **여러 페이지에 걸친 인용구**: 텍스트 선택 화면에 "다음 페이지 이어서 촬영" 버튼을 둔다. 탭하면 카메라가 다시 열려 다음 페이지를 촬영 → OCR 실행 → 인식된 줄이 "2페이지" 그룹으로 같은 선택 목록에 이어서 추가됨(1페이지 선택 내용은 유지). 페이지 수 제한 없이 반복 가능. 최종적으로 **선택된 줄을 촬영 순서대로 합쳐 하나의 편집 가능한 텍스트필드**에 표시 — 인용구는 항상 1건으로 저장됨(페이지별로 쪼개지지 않음).
   - 페이지번호는 첫 촬영에서 인식/입력한 값을 `pageNumber`에, 두 번째 이상 촬영이 있었다면 마지막 촬영의 값을 `pageNumberEnd`에 넣어 "185-186p"처럼 범위로 표시(한 페이지만 촬영했다면 `pageNumberEnd`는 null). 각 촬영마다 진행률 기록과 동일한 페이지 번호 자동 인식(코너 숫자 후보 추출)을 재사용해 프리필하되 항상 확인 후 저장.
   - 저장 후 "계속 촬영"(같은 책으로 카메라 재진입해 **별개의 새 인용구** 캡처 시작) / "완료"(호출한 곳으로 복귀) 선택 — 이건 "다음 페이지 이어서 촬영"과 달리 저장 이후에만 등장하는, 완전히 다른 인용구를 잇달아 찍기 위한 것.
6. **독후감 작성**: 특정 라운드에 연결된 텍스트+평점(선택) 작성/저장 — 중단한(DROPPED) 라운드에도 독후감(왜 중단했는지 등)을 남길 수 있음, 제약 없음
7. **라이브러리(전체 책 목록)**: 대시보드 상단바에서 진입, 상태별(읽는 중/일시중지/완독/중단/읽을 예정) 필터와 검색 제공. PAUSED·DROPPED·FINISHED·PLANNED 책은 대시보드 책장에는 안 보이므로 이 화면이 유일한 접근 경로. 목록 아이템 탭 → 책 상세.
8. **통계**: 대시보드 상단바에서 진입. 상단에 "지금까지 읽은 책 32권"(PLANNED 제외 전체 카운트) 요약, 그 아래 **장르별 / 작가별 / 출판사별 / 국가별** 4개 섹션이 각각 가로 막대 리스트로 표시(책 권수 기준, `AggregateBooksByAttributeUseCase` 재사용). 상위 N개 외엔 "기타"로 묶고, genre/country가 비어있는 책은 "미상"으로 집계. 막대 탭 시 그 그룹에 속한 책 목록으로 필터링된 라이브러리 화면으로 이동은 **선택 사항**(있으면 좋지만 필수는 아님) — 구현한다면 `LibraryViewModel`의 필터가 지금의 status 필터만으로는 부족하므로 genre/author/publisher/country 필터도 추가해야 함.
9. **설정**: 대시보드 상단바에서 진입.
   - 리마인더 on/off 토글 + 시각 선택(`TimePicker`, 하루 1회). 저장 즉시 `ReminderScheduler`가 알람을 재등록/취소. 알림 자체는 매일 정해진 시각에 현재 READING인 책 중 **무작위로 1권**을 골라 "『책 제목』 62% 읽는 중" 형태로 표시하고, 알림 탭 시 그 책의 빠른 기록 시트가 바로 열려 그 자리에서 진행률을 기록할 수 있음(리마인더가 곧 빠른 기록 진입점이 되도록 설계). READING인 책이 하나도 없는 날은 알림을 건너뛰고 다음날 알람만 재등록.
   - **일일 목표 페이지 수**(숫자 입력, 비워두면 목표 없음/그래프에 기준선 미표시). 저장 즉시 대시보드의 히어로 숫자·막대그래프에 반영(둘 다 DataStore를 구독하는 Flow라 별도 갱신 로직 불필요).
   - **데이터 내보내기**: 탭하면 SAF `ACTION_CREATE_DOCUMENT`(기본 파일명 `booklogs_backup_YYYY-MM-DD.json`)로 저장 위치를 사용자가 직접 고름(로컬 저장소든 구글드라이브 등 문서공급자든 SAF가 지원하는 곳 어디든) → 전체 데이터를 JSON으로 내보냄. 클라우드 없이도 사용자가 원하는 곳에 수동 백업 가능하다는 게 핵심.
   - **데이터 가져오기**: SAF `ACTION_OPEN_DOCUMENT`로 이전에 내보낸 JSON 파일 선택 → "가져오기를 하면 현재 앱의 모든 데이터가 가져온 파일 내용으로 대체됩니다"를 알리는 확인 다이얼로그(파괴적 작업이라 삭제와 동일하게 블로킹 확인) → 승인 시 기존 Room 데이터 전체 삭제 후 JSON 내용으로 재삽입. 새 기기로 옮기거나 데이터 손실에서 복구하는 용도이므로 병합이 아니라 전체 대체가 맞는 동작.

## 구현 시 유의사항

- **바코드 분석기**: `ImageAnalysis.Analyzer`로 EAN-13만 필터링, `addOnCompleteListener`에서 반드시 `imageProxy.close()`. 첫 성공 인식 후 `clearAnalyzer()`로 중복 트리거 방지. ISBN 유효성(978/979로 시작하는 13자리)까지 확인 후 조회 호출.
- **OCR**: 실시간 오버레이 대신 정지 이미지 캡처 후 처리(흔들림 있는 실시간 인식보다 안정적). 이미지 내 탭-선택 오버레이 대신 인식된 줄을 리스트 형태로 선택하게 해 구현 난이도를 낮춤(기기 없이 검증 불가한 부분이므로 최대한 단순한 UX로 설계).
- **일별 합산 그래프**: `ComputeLogDeltasUseCase`(라운드별 로그를 `loggedAt`순 정렬 후 인접 쌍 차이 계산, 음수 클램프)로 델타를 파생시킨 뒤, 각 델타를 그 쌍의 뒤쪽 로그의 `logDateEpochDay`에 귀속시켜 날짜별로 합산하는 순수 Kotlin 함수(`AggregateDailyPagesUseCase`)로 분리 — 이 두 함수가 이번 프로젝트의 핵심 로직이자 최우선 단위 테스트 대상. 요청한 기간(예: 최근 14일) 밖의 로그도 델타 계산 입력에는 포함해야 기간 경계의 첫 델타가 정확하게 나옴에 유의.
- **메타데이터 연동(카카오 메인 + Google Books 병렬 보조) — 실패 처리 상세**: 각 API 호출은 개별적으로 `ApiLookupResult<T>`(`Success(data)` / `NotFound`(정상 응답, 결과 0건) / `NetworkError`(타임아웃·오프라인·5xx))를 반환. `BookMetadataRepositoryImpl.lookup(isbn 또는 query)`가 코루틴 `async`로 카카오·Google Books를 **동시에** 호출한 뒤(각 5초 타임아웃, 병렬이라 전체 최악 대기시간도 5초 — 순차였다면 최대 10초) 두 결과를 보고 **어떤 조합으로 합칠지 분기만 결정**하고, 실제 필드 합성은 `BookMetadataMapper.merge(kakaoDto?, googleDto?)`에 위임(Repository=오케스트레이션, Mapper=순수 매핑, 역할 분리):
  - 카카오 `Success` + Google Books `Success` → `merge(kakaoDto, googleDto)` 호출 — 카카오의 title/author/publisher/coverImageUrl/isbn을 채택하고 Google Books의 `pageCount`→`totalPages`, `categories`→`genre`만 덧씌움(정상 케이스).
  - 카카오 `Success` + Google Books `NotFound`/`NetworkError` → 카카오 필드만 채택, `totalPages`/`genre`는 그냥 빈 값으로 둠(에러 표시 없이 기존 "총 페이지 수 입력 유도" 흐름으로 자연 연결). 확인 폼에 "정보 다시 불러오기" 버튼을 둬서 Google Books 쪽만 재시도 가능.
  - 카카오 `NotFound`/`NetworkError` + Google Books `Success` → **Google Books 결과 전체로 대체**(표지·저자 등도 Google Books 값 사용) — 기존 폴백.
  - 둘 다 `NotFound`, 또는 하나는 `NotFound` 하나는 `NetworkError` → 최소 한쪽은 정상 응답을 받은 것이므로 `MetadataLookupResult.NotFound`로 분류, "책 정보를 찾지 못했어요 · 직접 입력해주세요" 안내 + 빈 확인 폼(바코드 스캔이었다면 ISBN만 프리필).
  - 둘 다 `NetworkError` → `MetadataLookupResult.NetworkError`로 분류, "인터넷 연결을 확인해주세요" 안내 + 빈 확인 폼 + **다시 시도 버튼**(오프라인이어도 등록 자체는 계속 가능 — 수동 입력 후 저장).
  - **제목 검색 목록 단계**는 매 검색마다 두 API를 병렬 호출하면 호출 수가 과해지므로 **카카오 단독 검색**이 우선이고, 카카오 결과가 0건(`NotFound`)일 때만 동일 검색어로 Google Books 검색을 재시도해 목록을 채움. 사용자가 목록에서 항목을 하나 고른 시점에야 그 책에 대해 Google Books를 페이지수/장르용으로 추가 조회(위 5가지 분기와 동일하게 처리).
  - 카카오 REST API 키는 `local.properties`에 저장 후 `BuildConfig` 필드로 노출(레포에 커밋되지 않도록 `.gitignore` 확인), 사용자가 카카오 디벨로퍼스에서 직접 발급받아야 함을 README/설정 안내에 명시.
- **페이지 번호 사진 인식**: 인용구용 `TextRecognitionAnalyzer`를 그대로 재사용. 인식된 `Line` 중 (a) 순수 숫자로만 구성되고 (b) 자릿수가 1~4자리이며 (c) 이미지 상하단 코너 영역의 바운딩 박스에 위치하는 것을 페이지 번호 후보로 스코어링해 가장 그럴듯한 값을 입력창에 프리필. 후보가 여러 개면 가장 코너에 가까운 것을 우선하되 항상 사용자 확인/수정 단계를 거쳐 자동 저장하지 않음.
- **도넛 진행률 오버레이**: `DashboardScreen`의 책장 그리드 아이템은 `Box`로 표지(Coil `AsyncImage`) + 반투명 검정 `Box`(dim) + `Canvas`로 그리는 도넛(진행률 arc, `drawArc(startAngle=-90, sweepAngle=360*progress)`) + 도넛 중앙에 `Text("${(progress*100).roundToInt()}%")`를 겹쳐 그림. 진행률 계산은 기존 `ComputeBookProgressUseCase` 재사용. `totalPages`가 없으면 도넛/퍼센트 대신 "?" 배지로 대체(이미 앞서 정의됨).
- **오늘 읽은 페이지 히어로 숫자 + 주간 막대그래프**: `DashboardViewModel`이 `AggregateDailyPagesUseCase`의 결과(`List<DayPageTotal>`, 최근 7일)를 한 번만 계산해 `DailyPagesBarChart`(전체 배열)와 `TodayPagesHero`(마지막 원소, `Text`로 큰 숫자) 두 컴포저블에 그대로 전달 — 별도 쿼리/계산 중복 없음.
- **일일 목표선**: `AppSettingsDataStore.dailyGoalPages`를 `DashboardViewModel`이 `AggregateDailyPagesUseCase` 결과와 `combine`해 구독. `dailyGoalPages`가 null이 아니면 `DailyPagesBarChart`가 목표값 위치에 `drawLine`으로 점선을 긋고, 각 막대는 `dayTotal >= goal` 여부로 성공/일반 컬러를 분기(간단한 불리언 비교라 별도 usecase 없이 컴포저블에서 직접 처리). `TodayPagesHero`도 같은 비교로 "128 / 150p" 텍스트와 색상을 결정.
- **빠른 기록 시트(`BookQuickActionSheet`)**: `ModalBottomSheet`로 구현, 열릴 때 해당 책의 마지막 `ReadingLog.currentPage`를 조회해 입력창에 프리필(아직 로그가 한 건도 없으면 0/빈 값으로 프리필 — 오늘 처음 기록하는 상황)하고 `TextFieldValue`의 selection을 전체 범위로 설정해 즉시 타이핑하면 덮어써지도록 함(`LocalFocusRequester`로 자동 포커스 + 키패드 자동 표시). PHYSICAL 책은 프리필 값이 곧 `currentPage`이고, EBOOK 책은 `ConvertPagePercentUseCase.pageToPercent`로 환산한 %를 프리필. 저장 시 PHYSICAL은 `LogProgressUseCase`에 입력값을 그대로, EBOOK은 `percentToPage`로 환산한 값을 전달 — 둘 다 결과적으로 `ReadingLogEntity.currentPage`에 페이지가 저장되는 단순 insert(델타 계산 없음). EBOOK인데 `totalPages`가 아직 없으면 %입력 대신 총 페이지 수(대략치) 입력을 먼저 요구(기존 "?" 배지 흐름 재사용). 시트는 화면 전환이 아니라 대시보드 위에 오버레이되므로 저장 후 자동 닫힘 + 진행률 링 애니메이션 갱신.
- **인용구 "계속 촬영" 흐름**: `QuoteCaptureViewModel`이 저장 성공 후 `SnackbarResult`/다이얼로그로 "계속 촬영" 선택 시 동일 화면(같은 bookId)에서 카메라를 재시작하고, "완료" 선택 시 호출한 곳(대시보드 또는 책 상세)으로 pop. 내비게이션 스택에 화면을 새로 쌓지 않고 같은 컴포저블 내에서 상태만 리셋.
- **여러 페이지 인용구("다음 페이지 이어서 촬영")**: `QuoteCaptureViewModel`이 `capturedPages: List<CapturedPageOcrResult>`(각 원소 = 한 번의 촬영에서 나온 인식 줄 목록 + 사용자가 선택한 줄 인덱스 + 그 페이지의 인식/입력된 페이지번호)를 순서대로 보관. "다음 페이지 이어서 촬영" 탭 시 카메라를 다시 열고 촬영 결과를 리스트에 append, 텍스트 선택 화면은 그룹별로 구분해 보여주되 최종 텍스트필드는 순수 함수 `joinSelectedQuoteLines(capturedPages): String`(선택된 줄을 촬영 순서대로 join, Android 의존성 없어 단위테스트 가능)의 결과를 표시. 저장 시 `QuoteEntity.pageNumber = capturedPages.first().pageNumber`, `pageNumberEnd = if (capturedPages.size > 1) capturedPages.last().pageNumber else null`. "계속 촬영"(저장 후 새 인용구)과는 별개 기능이며 저장 전에만 등장.
- **로그 수정/삭제(임의 시점 가능)**: `ReadingLogDao`에 `getLatest(roundId): Flow<ReadingLogEntity?>`, `getAllForRound(roundId): Flow<List<ReadingLogEntity>>`, `update(log)`, `deleteById(id)` 추가. `EditLogUseCase`/`DeleteLogUseCase`는 대상 로그의 `roundId`만 확인하고 바로 update/delete — 델타가 저장되지 않으므로 이웃 로그를 손댈 필요가 전혀 없다. 대시보드 시트는 `getLatest`로 최근 1건만 인라인 노출하고, 책 상세의 진행 이력은 `getAllForRound`로 전체 목록을 보여주며 각 행에 동일한 수정/삭제 진입점을 둔다. 두 화면 모두 Flow 구독이라 수정/삭제 즉시 진행률 링·그래프·이력 목록이 자동 갱신됨. 삭제 시 스낵바의 "실행취소"는 삭제된 엔티티를 ViewModel이 메모리에 잠깐 들고 있다가 재삽입하는 방식으로 구현(별도 soft-delete 컬럼 불필요).
- **책 상태 전이(`ChangeBookStatusUseCase`)**: 전이 종류에 따라 분기.
  - READING → PAUSED: `BookEntity.status`만 갱신, 라운드 불변.
  - READING → FINISHED/DROPPED: 현재 열린 라운드(`finishedAt == null`)를 찾아 `finishedAt = now`, `endReason = COMPLETED`/`DROPPED`로 갱신 + `BookEntity.status` 동기화.
  - PAUSED → READING: `BookEntity.status`만 갱신, 라운드 불변(이미 열려있음).
  - FINISHED/DROPPED → READING: `roundNumber = 이전 최대값 + 1`인 새 `ReadingRoundEntity` 삽입(startedAt=now, finishedAt=null) + `BookEntity.status = READING`.
  - **PLANNED → READING**: 라운드가 아직 하나도 없는 책이므로 `roundNumber = 1`인 첫 `ReadingRoundEntity` 삽입 + `BookEntity.status = READING` — 등록 시 "바로 읽기 시작"을 선택했을 때와 완전히 동일한 헬퍼(`createFirstRound` 또는 동일 코드 경로)를 재사용, FINISHED/DROPPED→READING과도 "새 라운드 만들기"라는 점에서 근본적으로 같은 동작.
  - 각 케이스가 독립적인 분기라 파악이 쉽고, 트랜잭션 하나(`@Transaction`)로 묶어 라운드/책 상태가 항상 같이 갱신되도록 함(하나만 갱신되고 나머지가 실패하는 상태 방지).
- **라이브러리 화면 쿼리**: `BookDao.observeAll(): Flow<List<BookEntity>>`를 가져와 ViewModel에서 status로 필터링(데이터 양이 적어 SQL WHERE 없이 메모리 필터로 충분). 검색은 title/author `contains` 매칭.
- **통계 화면**: `StatsViewModel`이 `BookDao.observeAll()`을 status != PLANNED로 필터링한 뒤 `AggregateBooksByAttributeUseCase`를 genre/author/publisher/country 4번 호출(키 추출 함수만 다르게 전달)해 4개 섹션 데이터를 만듦 — 별도 쿼리/usecase 중복 없이 하나의 순수 함수 재사용. 장르 자동완성은 `BookMetadataMapper`가 Google Books 응답의 `volumeInfo.categories`(List<String>) 중 첫 값을 확인/수정 폼의 장르 필드에 프리필(카카오·Google Books 병렬 조회이므로 카카오가 성공해도 매번 시도됨) — 항상 사용자가 확인/수정 가능.
- **중복 등록 감지**: `BookRegistrationViewModel`이 ISBN을 확보한 시점(바코드 디코드 직후, 또는 제목검색 결과 선택 직후)에 `BookRepository.findByIsbn(isbn)`을 호출 — 결과가 있으면 확인 폼에 안내 배너를 노출하고 "그 책으로 이동"/"그래도 새로 등록" 두 액션을 제공, 저장 자체를 막지는 않음(정말 별도 사본을 추적하고 싶은 경우를 배려).
- **책 삭제**: `DeleteBookUseCase`는 `BookDao.deleteById(bookId)` 단순 호출(FK `onDelete=CASCADE`가 라운드/로그/인용구/독후감을 전부 정리). UI 쪽 `DeleteBookConfirmDialog`가 실제 안전장치 — usecase 자체엔 확인 로직을 넣지 않고(순수하게 삭제만 수행), 확인 여부는 항상 호출부(책 상세 화면) 책임으로 분리해 usecase를 단순하게 유지.
- **백업/복원**: `BackupExporter`가 5개 테이블(Book/ReadingRound/ReadingLog/Quote/Review)을 각각 `List<XxxDto>`로 변환 후 `BackupEnvelope(schemaVersion = 1, exportedAt, books, rounds, logs, quotes, reviews)`로 감싸 kotlinx.serialization으로 직렬화, SAF `ACTION_CREATE_DOCUMENT`로 받은 `Uri`에 스트림으로 씀. `BackupImporter`는 반대로 역직렬화 후 **Room 트랜잭션 하나**로 기존 데이터 전량 삭제 + 새 데이터 삽입(부분 실패 시 롤백되어 데이터가 어중간하게 섞이는 상태 방지). Room 자동생성 PK(`id`)는 내보낼 때 그대로 포함하되 가져오기 시 그 값 그대로 재삽입해 FK 관계(bookId 등)가 깨지지 않게 함(가져오기는 "새 기기에 그대로 복원"이 목적이라 ID 재발급 불필요). `schemaVersion`이 현재 앱의 지원 버전보다 낮으면 가져오기 전에 JSON 단계에서 매핑해 올리는 마이그레이션 함수를 추가할 자리를 마련해둠(현재는 버전 1 하나뿐이라 매핑 없음).
- **리마인더 알람/알림**: `ReminderScheduler.schedule(hour, minute)`이 `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, pendingIntent)`로 다음 발생 시각 하나만 예약(반복 알람 대신 매번 재예약 — Doze 하에서 `setRepeating` 오차 누적을 피하기 위함). `ReminderReceiver.onReceive`가 (1) `BookDao`에서 READING 목록 조회 → `PickReminderBookUseCase`로 무작위 1권 선택(없으면 알림 생략) → (2) 알림 표시 시 `PendingIntent`의 딥링크 extra로 `bookId`를 실어 `MainActivity` → `Destinations.Dashboard(openQuickSheetFor = bookId)`로 진입하도록 구성 → (3) 다음날 같은 시각으로 알람 재예약. 기기 재부팅 시 `AlarmManager` 알람이 사라지므로 `BootReceiver`가 `RECEIVE_BOOT_COMPLETED`를 받아 저장된 설정으로 재등록. Android 13(API 33)+에서는 알림 표시 전 런타임 `POST_NOTIFICATIONS` 권한 요청 필요(설정 화면에서 리마인더를 켤 때 요청).
- DAO는 인터페이스로 정의해 Repository 단위 테스트에서 Room 없이 Fake DAO로 대체 가능하게 함.

## Gradle 설정

- Version Catalog(`gradle/libs.versions.toml`)에 Compose BOM, Room(+KSP), CameraX, ML Kit(barcode-scanning, text-recognition, text-recognition-korean), Retrofit+OkHttp, Coil, Navigation-Compose, Coroutines, DataStore Preferences, **kotlinx.serialization**(+ Retrofit용 컨버터) 추가
- **Pretendard 폰트 번들링** (완료): npm 레지스트리(`pretendard@1.3.9` 패키지, `dl.google.com`과 달리 이 샌드박스에서 접근 가능했음)에서 정적 TTF(Regular/Medium/SemiBold/Bold 4종)를 받아 `app/src/main/res/font/pretendard_*.ttf`로 배치 완료. `ui/common/theme/Type.kt`에서 `FontFamily`로 구성해 Compose `Typography`의 모든 슬롯에 지정. OFL 라이선스 고지는 `licenses/PRETENDARD_LICENSE.txt`로 커밋됨. 별도 Gradle 의존성 불필요(리소스 번들링).
- 백업/복원(SAF `ACTION_CREATE_DOCUMENT`/`ACTION_OPEN_DOCUMENT`)은 별도 매니페스트 권한이나 의존성 불필요 — `Intent`만으로 동작, `READ/WRITE_EXTERNAL_STORAGE` 등 저장소 권한 일체 불필요(스코프드 SAF 접근이라 안전).
- `AndroidManifest.xml`: `CAMERA`, `INTERNET`, **`POST_NOTIFICATIONS`(API 33+ 런타임 권한), `RECEIVE_BOOT_COMPLETED`** 권한, `<uses-feature android:name="android.hardware.camera" required="true"/>`, `ReminderReceiver`/`BootReceiver`를 `<receiver>`로 등록
- 카카오 REST API 키: `local.properties`에 `KAKAO_API_KEY=...` 추가 → `app/build.gradle.kts`에서 `buildConfigField`로 주입, `local.properties`는 이미 `.gitignore` 대상이므로 키가 커밋되지 않음을 확인. 사용자가 카카오 디벨로퍼스(https://developers.kakao.com)에서 앱을 등록하고 키를 발급받아야 하는 단계는 구현 완료 후 별도 안내

## 구현 현황

- **책 등록(화면 흐름 #2) 구현 완료**: 진입 선택(스캔/제목검색/직접입력) → 바코드 스캔(CameraX+ML Kit EAN-13, ISBN 유효성 검사) 또는 제목 검색(카카오 우선, 0건일 때만 Google Books 폴백) → 확인/수정 폼(중복 등록 배너, 종이책/전자책 토글, 바로 읽기 시작 스위치) → `RegisterBookUseCase` 저장. 5-분기 메타데이터 실패 처리 매트릭스(`resolveBookMetadata`)와 병합 로직(`mergeBookMetadata`)은 :domain에 순수 함수로 구현해 전수 단위테스트 완료. Compose Navigation으로 대시보드 FAB → 등록 플로우 4화면을 연결(단일 `BookRegistrationViewModel` 인스턴스를 플로우 전체가 공유, 진입 시 `reset()`).
  - 간소화한 부분: 계획 문서의 "확인 폼이 즉시 열리고 totalPages/genre 칸만 개별 스켈레톤"이 아니라, 폼 화면 전체에 `LoadingOverlay`를 잠깐(병렬 호출이라 최악 5초) 띄운 뒤 완성된 폼을 보여주는 방식으로 구현(필드별 스켈레톤보다 구현이 단순하고 UX 차이는 미미). "카카오만 성공했을 때 Google Books만 재시도"도 전체 재조회(`lookupByIsbn` 재호출)로 단순화.
  - 미구현(다음 작업): 진행률 빠른 기록 시트, 책 상세, 인용구 캡처, 독후감, 라이브러리, 통계, 설정, 리마인더 실동작, 대시보드 책장 그리드(도넛 오버레이)/7일 막대그래프, 런처 아이콘.

## 검증 계획

샌드박스에 Android SDK/에뮬레이터가 없으므로:
- **가능**: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`로 순수 로직 검증
  - `ComputeLogDeltasUseCaseTest`(핵심: 정렬/인접쌍 차이/클램프, 그리고 **중간 로그 수정·삭제 후 이웃 델타가 재저장 없이 올바르게 다시 계산되는지** — 이번 스키마 변경의 핵심 검증 대상), `AggregateDailyPagesUseCaseTest`(일자 버켓팅/0채움/여러 책 합산/기간 경계 밖 로그 포함 여부, **목표값과 비교해 성공 여부 플래그가 맞는지**), `LogProgressUseCaseTest`(단순 insert), `EditLogUseCaseTest`/`DeleteLogUseCaseTest`(임의 시점 로그 수정/삭제, 이웃 로그 미변경 확인), `ComputeBookProgressUseCaseTest`(totalPages null 처리 + **로그 0건일 때 0%로 계산되는지, 두 케이스가 서로 구분되는지**), `ChangeBookStatusUseCaseTest`(READING↔PAUSED는 라운드 불변, →FINISHED/DROPPED는 라운드 종료+endReason 정확성, FINISHED/DROPPED→READING은 roundNumber 증가한 새 라운드 생성, PAUSED→READING은 라운드 재사용, **PLANNED→READING은 roundNumber=1인 첫 라운드 생성**), `PickReminderBookUseCaseTest`(빈 목록→null, 여러 권 중 무작위 선택이 목록 범위 내에서만 나오는지), `BookMetadataMapperTest`(카카오 DTO 매핑 + Google Books DTO 매핑 + **두 응답 병합 시 pageCount/categories만 Google Books에서 채택되는지** + 카카오 실패 시 Google Books 전체 대체 폴백 분기), `PageNumberCandidateExtractorTest`(숫자 토큰 필터링/코너 스코어링 로직, 순수 함수로 분리해 단위 테스트), `JoinSelectedQuoteLinesTest`(여러 페이지의 선택된 줄을 촬영 순서대로 합치는 순수 함수, 페이지 1건/2건 이상일 때 `pageNumberEnd` null 여부), `ConvertPagePercentUseCaseTest`(양방향 변환 반올림 정확성, totalPages null일 때 변환 불가 처리, 0%/100% 경계값), `AggregateBooksByAttributeUseCaseTest`(그룹별 카운트/내림차순 정렬/상위 N+"기타" 묶음/null·빈값 "미상" 처리), `BookMetadataRepositoryImplTest`(Fake `KakaoBooksApi`/`GoogleBooksApi`로 **Success/NotFound/NetworkError 3×3 조합**을 각각 주입해 병합 결과·대체 폴백·`NotFound`/`NetworkError` 최종 분류가 위 표대로 나오는지 전수 검증 — 이번에 정리한 실패 처리 매트릭스의 핵심 테스트), `BackupExportImportRoundTripTest`(Fake 데이터를 `BackupEnvelope`로 직렬화 후 역직렬화했을 때 5개 테이블 전부 원본과 동일한지, FK 관계(bookId 등)가 유지되는지 — 실제 파일 I/O·SAF는 Android 의존적이라 제외하고 순수 매핑/직렬화 로직만 검증), `FindByIsbnDuplicateCheckTest`(Fake DAO에 동일 ISBN 책이 있을 때/없을 때 분기), Fake DAO 기반 Repository 테스트
  - `assembleDebug`/`lint`는 SDK 플랫폼 컴포넌트 다운로드가 가능한지에 따라 시도해보되, 안 되면 컴파일+유닛테스트까지가 한계
- **불가능(사용자가 실기기/에뮬레이터에서 직접 확인 필요)**: Compose 화면 렌더링/내비게이션 클릭 흐름(책장 그리드 + 도넛 오버레이 실제 표시 포함), CameraX 프리뷰·권한, 실제 바코드 인식 정확도, 실제 한글/영어 책 페이지 OCR 정확도 및 텍스트 선택 UX, 페이지 번호 자동 인식 정확도(폰트/각도/코너 위치 편차가 커서 실기기 튜닝 필요), Room 실기기 동작(마이그레이션 테스트 포함), 카카오/Google Books 실제 네트워크 응답, **AlarmManager 알람 발화·Doze 하 지연 정도·재부팅 후 재등록·알림 표시/딥링크 동작**, **SAF 파일 선택/쓰기·실제 백업 파일로 새 기기 복원 시나리오**, TalkBack 실제 낭독 확인, 전체 엔드투엔드 플로우(등록→기록→그래프 갱신→인용구→독후감→리마인더→백업)

구현 순서는 Room 스키마 → DAO/Repository → domain usecase(전부 유닛테스트로 검증) → ViewModel(Fake Repository로 검증) → Compose UI/CameraX/ML Kit(유닛테스트 불가, 최대한 단순하게 구현) 순으로 진행해, 샌드박스에서 검증 가능한 "핵심 로직"을 먼저 견고히 하고 카메라/OCR 튜닝은 사용자 몫으로 넘긴다.
