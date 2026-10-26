# 개발 로그: 구독 결제일 알림 앱 (DateAlarm)

Java/XML로 Android를 개발하다 Kotlin + Jetpack Compose를 배우기 위해 만드는 앱이에요.
구독 서비스(넷플릭스, 유튜브 프리미엄 등)의 결제일을 등록해 두면, 결제 전에 알림이나 알람으로 알려 줘요.

---

## 1. 현재 기능

| 기능 | 상태 |
|---|---|
| 구독 추가 (이름, 가격, 월간/연간, 결제 기준일, 며칠 전, 알림 시각, 알림 종류) | ✅ |
| 구독 목록 (다음 결제일 순으로 정렬) | ✅ |
| 일반 알림 (노티) | ✅ |
| 알람 (확인할 때까지 소리 반복, [확인] 버튼으로 끄기) | ✅ |
| 앱 시작 시 권한 요청 (알림 권한, 정확한 알람 권한) | ✅ |
| 재부팅 / 알람 권한 허용 시 알람 다시 예약 | ✅ |
| 다음 결제일 자동 계산, 다음 회차 알림 자동 예약 | ✅ |
| 구독 수정 / 삭제 | ⬜ 다음 작업 |

---

## 2. 프로젝트 구조 (클린 아키텍처 3계층)

```
com.example.datealarm
├── MainActivity.kt                         Compose 진입점, 권한 요청 + NavHost
├── domain/                                 ← 순수 Kotlin. Android에 의존하지 않는 "규칙"
│   ├── model/
│   │   ├── Subscription.kt                 구독 데이터 (data class)
│   │   ├── SubscriptionDates.kt            결제일, 알림 시각 계산 (확장 함수)
│   │   ├── PaymentCycle.kt                 MONTHLY / YEARLY
│   │   └── ReminderType.kt                 NOTIFICATION / ALARM
│   ├── repository/SubscriptionRepository.kt   저장소 인터페이스
│   └── usecase/                            Add, Get 유스케이스
├── data/                                   ← "어디에, 어떻게 저장하나"
│   ├── local/                              Room (Entity, Dao, Database, Converters)
│   ├── repository/SubscriptionRepositoryImpl.kt   Entity ↔ 도메인 모델 변환
│   └── alarm/                              AlarmManager, BroadcastReceiver들
│       ├── ReminderScheduler.kt            다음 알림 시각을 계산해서 AlarmManager에 예약
│       ├── AlarmReceiver.kt                알림 표시 + 다음 회차 예약
│       ├── AlarmDismissReceiver.kt         알람의 [확인] 버튼 처리
│       └── BootReceiver.kt                 재부팅 / 권한 허용 시 전체 재예약
└── presentation/                           ← Compose 화면, ViewModel
    ├── list/                               목록 화면
    ├── addedit/                            추가 화면
    ├── navigation/                         NavHost, 화면 경로
    ├── permission/ReminderPermissionRequester.kt
    └── theme/
```

**계층을 나누는 기준**
- `domain`: "결제일은 기준일에서 n개월 뒤"처럼 **앱의 규칙**이에요. Room이나 AlarmManager를 몰라요.
- `data`: Room, AlarmManager처럼 **저장 방법**에 관한 코드예요.
- `presentation`: 화면이에요. ViewModel이 UseCase를 호출해요.

아직 DI(Hilt) 없이, `SubscriptionNavHost`의 `viewModelFactory { initializer { } }`와 각 Receiver 안에서 직접 객체를 조립해요.

---

## 3. 주요 흐름

### 구독 저장 → 알림 예약
```
AddEditSubscriptionScreen [저장]
  → AddEditSubscriptionViewModel.save()
      → AddSubscriptionUseCase → Repository → Room insert (id 반환)
      → ReminderScheduler.schedule(subscription.copy(id = id))
          → nextReminderAt() 계산 → AlarmManager.setExactAndAllowWhileIdle()
```

### 알림 시각 도착
```
AlarmManager → AlarmReceiver.onReceive()
  ├─ (goAsync + 코루틴) DB에서 구독 다시 조회 → schedule() → 다음 회차 예약   ← 권한과 상관없이 항상 실행
  ├─ 알림 채널 생성 (노티용, 알람용)
  ├─ 알림 권한 확인 (API 33 미만은 항상 통과)
  ├─ NOTIFICATION → 일반 알림
  └─ ALARM        → 반복 소리 + 지울 수 없는 알림 + [확인] 버튼
                        └─ [확인] → AlarmDismissReceiver → cancel(알림 ID)
```

### 재부팅 / 알람 권한 허용
```
BOOT_COMPLETED 또는 SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
  → BootReceiver → (goAsync + 코루틴) DB 전체 조회 → 하나씩 schedule()
```

### 앱 시작
```
MainActivity → ReminderPermissionRequester
  → (API 33+) 알림 권한 시스템 팝업
  → 응답이 오면 → (API 31+) 정확한 알람 권한이 꺼져 있으면 "설정으로 이동" 다이얼로그
```

---

## 4. 설계 결정

### 결제일은 DB에 "기준일"로 저장하고, 매번 계산한다
DB의 `nextPaymentDate`는 **절대 바꾸지 않는 기준일**이에요. n번째 결제일은 항상 `기준일.plusMonths(n)`으로 계산해요.

알림이 울릴 때마다 DB 날짜에 +1개월을 저장하는 방식은 쓰지 않았어요. 이유는 두 가지예요.
1. **날짜 밀림(드리프트):** `1/31 → 2/28 → 3/28 → …`처럼 한 번 말일 보정이 일어나면 영원히 28일이 돼요. 기준일에서 바로 계산하면 `1/31.plusMonths(2) = 3/31`로 정확해요.
2. **알림은 결제일보다 먼저 울려요.** "7일 전" 알림이 울리는 순간 날짜를 바꾸면, 결제 전인데 목록에 다음 달 날짜가 보여요.

| 함수 | 용도 |
|---|---|
| `paymentDateAt(n)` | n번째 결제일 |
| `upcomingPaymentDate()` | 오늘 이후 첫 결제일 (목록 표시, 정렬용) |
| `nextReminderAt()` | 지금 이후 첫 알림 시각 (예약용, 항상 미래 시각) |

`upcomingPaymentDate`와 `nextReminderAt`을 따로 둔 이유: 오늘이 결제일이고 알림이 "7일 전"이면, 표시할 결제일은 **오늘**이지만 다음 알림은 **다음 달 결제일 7일 전**이에요.

### 다음 회차는 AlarmReceiver가 예약한다
알림이 울린 직후 `schedule()`을 다시 부르면, `nextReminderAt()`이 방금 지난 회차를 건너뛰고 다음 회차를 찾아요. 따로 "+1개월" 코드가 필요 없어요.
Intent 값이 아니라 **DB에서 다시 조회**하는 이유는 두 가지예요. Intent에는 계산에 필요한 값(기준일, 주기 등)이 없고, 그 사이 삭제된 구독은 `null`이 나와서 예약이 자연스럽게 멈추기 때문이에요.
이 예약 코드는 **알림 권한 체크보다 위**에 있어요. 권한이 없어서 `return`하더라도 예약 체인이 끊기지 않게 하려고요.

### 알림 채널을 두 개로 나눴다
노티용(`subscription_reminder_channel`)과 알람용(`subscription_alarm_channel`)을 분리했어요. 채널은 한 번 만들어지면 중요도나 소리를 코드로 바꿀 수 없어서, 성격이 다른 알림은 처음부터 나눠 두는 게 좋아요.

### 알림 ID에 offset을 둔다
노티는 `0 + id`, 알람은 `100_000 + id`예요. 같은 구독의 두 알림이 서로를 덮어쓰지 않게 하려고요.

---

## 5. 알려진 한계 / TODO

- [ ] **수정 / 삭제 기능 없음.** 삭제할 때는 예약된 알람도 `AlarmManager.cancel()`로 같이 취소해야 해요.
- [ ] **저장 직후 `popBackStack()`.** 화면이 닫히면 `viewModelScope`가 취소되니까, 이론상 저장이나 예약이 끝나기 전에 코루틴이 취소될 수 있어요.
- [ ] **입력 검증 없음.** 이름이 비어 있거나 가격이 숫자가 아니어도 그냥 저장돼요 (가격은 0원).
- [ ] **`fallbackToDestructiveMigration(true)`.** DB 버전을 올리면 기존 데이터가 지워져요. 출시 전에 Migration을 작성해야 해요.
- [ ] `AppDatabase`의 싱글톤 변수 이름 오타 (`INSTANT` → `INSTANCE`), `getInstance()`에 `synchronized`가 없음
- [ ] 화면을 회전하면 권한 요청 `LaunchedEffect`가 다시 실행돼요 (이미 허용된 권한은 팝업이 뜨지 않아서 실제 문제는 적어요).
- [ ] `PendingIntent` requestCode로 `subscription.id.toInt()`를 쓰고 있어요. id가 Int 범위를 넘으면 겹칠 수 있어요 (현실적으로는 문제없음).

---

## 6. 테스트 방법 메모

| 확인할 것 | 방법 |
|---|---|
| 알림이 바로 뜨는지 | `ReminderScheduler`의 `// 테스트용 30초뒤` 줄을 사용하세요. **테스트가 끝나면 꼭 원래대로 되돌리세요.** |
| 예약된 알람 목록 | `adb shell dumpsys alarm \| grep datealarm` |
| 재부팅 후 복구 | 구독 저장 → 위 명령으로 확인 → `adb reboot` → 앱을 열지 말고 다시 확인 |
| 권한 흐름 처음부터 | 앱 삭제 → 재설치 (권한은 한 번 허용되면 다시 묻지 않아요) |
| 드리프트 | 결제 기준일 1/31, 월간 → 목록에 이번 달 말일이 보이면 정상 |

> `adb shell am broadcast -a android.intent.action.BOOT_COMPLETED`는 최신 Android에서 보호된 브로드캐스트라 권한 거부로 막혀요. 실제로 재부팅해서 확인하세요.

---

## 7. 작업 기록

| 커밋 | 내용 |
|---|---|
| `Initail commit` | Android Studio Compose 템플릿 |
| `Add domain layer, list/add-edit screens, and navigation` | 도메인 모델, 목록/추가 화면, Navigation |
| (이번 커밋) | Room DB, 알림/알람 예약, 권한 요청, BootReceiver, 결제일 자동 계산 |

### 이번 작업에서 겪은 실수들 (다시 하지 않기 위해)
- **이름 오타로 인한 컴파일 에러:** `createNotificationChannel` vs `createNotificationChannels`, `EXTRA_REMINDER_TYPE` vs `EXTRA_REMINDER_TYPES`, `upcomingPaymentData` vs `upcomingPaymentDate`. 이름을 바꿀 때는 **Shift+F6(Rename)**을 쓰면 호출하는 곳까지 같이 바뀌어요.
- **`flags of ...`:** 비트 OR는 `|`가 아니라 `or`예요.
- **함수 본문 전체가 주석:** 이름만 고치고 `/* */`를 안 풀면 컴파일은 되지만 채널이 생성되지 않아서, 알림이 **에러 없이** 사라졌어요.
- **변수를 만들고 사용하지 않음:** `dismissPendingIntent`를 만들고 `.addAction()`을 안 붙였어요. 회색 "unused" 경고를 무시하지 말기.
- **자동 import가 엉뚱한 클래스를 고름:** Compose 코드에 `android.app.AlertDialog`, `android.R.attr.text`가 import됐어요. Compose 코드에서 `android.app.*`이나 `android.R.*` import가 보이면 의심하기.
- **Manifest 문법 오류:** `<action android:name=""`처럼 태그가 안 닫혔어요. Kotlin 컴파일은 통과하지만 **전체 빌드(`⌘F9`)**에서 실패해요. Manifest를 고친 뒤에는 전체 빌드를 돌리기.
- **도메인 로직을 `data` 패키지에 둠:** 계산 로직은 Android와 무관한 "규칙"이니까 `domain`에 둬야 해요. 파일 이동은 드래그(Refactor → Move)로 하면 package와 import가 자동으로 바뀌어요.
