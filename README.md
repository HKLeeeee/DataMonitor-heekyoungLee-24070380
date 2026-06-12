# DataMonitor — S-Semi 데이터 모니터링 도구 (PoC 3)

반도체 시료 생산주문관리 시스템(SampleOrderSystem)의 PoC 3.
JSON 파일에 저장된 시료·주문 데이터를 **콘솔에서 실시간 조회**하는 독립 실행 관리자 도구.

---

## 데이터 소스 연결

### 기본 경로
실행 디렉토리 기준 `data/` 폴더의 두 파일을 읽는다.

```
data/
  samples.json   ← 시료 목록
  orders.json    ← 주문 목록
```

### 경로 변경 (CLI 인자)
```bash
# 기본 (data/)
./gradlew run

# 경로 지정
./gradlew run --args="path/to/your/datadir"
```

### JSON 포맷

**samples.json**
```json
[
  {
    "id": "S-001",
    "name": "SiC 파워기판-6인치",
    "avgProductionTime": "0.8",
    "yield": "0.92",
    "stock": "50"
  }
]
```

**orders.json**
```json
[
  {
    "orderId": "ORD-20260612-0001",
    "sampleId": "S-001",
    "customerName": "삼성전자",
    "quantity": "20",
    "status": "RESERVED"
  }
]
```

주문 status 허용값: `RESERVED` / `REJECTED` / `PRODUCING` / `CONFIRMED` / `RELEASE`

---

## 빌드 & 실행

```bash
# 테스트
./gradlew test

# 빌드
./gradlew build

# 실행 (데이터 경로 기본값: data/)
./gradlew run

# 경로 직접 지정
./gradlew run --args="data"
```

---

## 명령 목록

| 명령 | 설명 |
|------|------|
| `1`  | 시료 현황 — 전체 시료 목록 + 재고(ea) 테이블 출력 |
| `2`  | 주문 현황 — 상태별 건수(RESERVED/PRODUCING/CONFIRMED/RELEASE), REJECTED 제외 |
| `3`  | 재고 상태 — 시료별 재고 + 여유/부족/고갈 판정 |
| `4`  | 전체 요약 — 등록 시료 수, 총 재고, 전체 주문 수, 상태별 건수 |
| `R`  | 새로고침 — JSON 파일을 다시 읽어 최신 데이터 반영 |
| `0`  | 종료 |

### 재고 상태 판정 기준
- **고갈**: 재고 == 0
- **부족**: 재고 < CONFIRMED + PRODUCING 주문의 총 수량
- **여유**: 그 외

---

## 화면 예시

```
======================================================================
  S-Semi 데이터 모니터링 도구 시작
  데이터 경로: data
======================================================================

======================================================================
  S-Semi 데이터 모니터링 도구
======================================================================
  [1] 시료 현황   - 전체 시료 목록 + 재고
  [2] 주문 현황   - 상태별 건수 (REJECTED 제외)
  [3] 재고 상태   - 시료별 재고 + 여유/부족/고갈
  [4] 전체 요약   - 등록 시료 수 / 총 재고 / 주문 수 / 상태별 건수
  [R] 새로고침    - 데이터 소스 재로드
  [0] 종료
----------------------------------------------------------------------
  명령 입력: 1

======================================================================
  시료 ID    시료 이름                          생산시간   수율   재고
----------------------------------------------------------------------
  S-001      SiC 파워기판-6인치                   0.8분   92%   50ea
  S-002      GaN-on-Si 에피웨이퍼                 1.2분   85%    0ea
  S-003      Si 포토레지스트-8인치                0.5분   95%   10ea
----------------------------------------------------------------------
  총 3개 시료 등록

  명령 입력: 2

======================================================================
  [주문 현황] 상태별 건수 (REJECTED 제외)
----------------------------------------------------------------------
  RESERVED     :     1건
  PRODUCING    :     1건
  CONFIRMED    :     2건
  RELEASE      :     1건
----------------------------------------------------------------------
  합계          :     5건

  명령 입력: R

  [새로고침] 데이터 소스를 다시 읽었습니다.
    samples: /absolute/path/data/samples.json
    orders : /absolute/path/data/orders.json
```

---

## 실시간성 시연 시나리오

1. 도구 실행 후 `[4] 전체 요약`으로 현재 상태 확인
2. 다른 터미널에서 `data/orders.json`을 편집하여 주문 추가 또는 상태 변경
3. 도구에서 `R` (새로고침) 입력
4. `[2] 주문 현황` 또는 `[4] 전체 요약`으로 변경 내용이 반영된 것 확인

---

## 프로젝트 구조

```
src/
  main/java/com/ssemi/
    MonitorApp.java            ← 진입점 (dataDir 인자 처리)
    model/
      Sample.java              ← 시료 도메인 모델
      Order.java               ← 주문 도메인 모델
      OrderStatus.java         ← 주문 상태 열거형
      StockStatus.java         ← 재고 상태 열거형 (여유/부족/고갈)
    repository/
      JsonReader.java          ← 순수 Java JSON 파서
      DataRepository.java      ← 파일 기반 데이터 저장소
    service/
      MonitorService.java      ← 집계·조회 비즈니스 로직
    view/
      ConsoleView.java         ← 콘솔 출력 포매터
    controller/
      MonitorController.java   ← 메뉴 루프 제어
  test/java/com/ssemi/service/
    MonitorServiceTest.java    ← 단위 테스트 (13개)
  test/resources/data/
    samples.json               ← 테스트용 시료 데이터
    orders.json                ← 테스트용 주문 데이터
data/
  samples.json                 ← 운영용 시료 데이터
  orders.json                  ← 운영용 주문 데이터
```
