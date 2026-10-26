# Kotlin / Compose 공부 노트 (Java 개발자 관점)

이 앱을 만들면서 처음 만난 문법과 개념을 **Java와 비교해서** 정리한 노트예요.
각 항목 끝의 `📍`는 이 프로젝트에서 실제로 쓰인 파일이에요.

---

## Part 1. Kotlin 기본 문법

### 1-1. 변수: `val` / `var`
```kotlin
val name = "넷플릭스"   // 재할당 불가 = Java final
var n = 0L             // 재할당 가능
```
- 타입은 대부분 **추론**돼요. 명시하려면 `val price: Int = 0`처럼 써요.
- `0L`은 Long, `100_000`의 `_`는 가독성용 구분자예요.
- 기본은 `val`로 쓰고, 값이 바뀌어야 할 때만 `var`를 써요.

### 1-2. 함수 선언
```kotlin
fun add(a: Int, b: Int): Int { return a + b }
fun add(a: Int, b: Int): Int = a + b          // 본문이 식 하나면 = 로
fun log(msg: String) { }                      // 반환 타입 생략 = Unit (Java void)
```
- 매개변수는 `타입 이름`이 아니라 **`이름: 타입`** 순서예요.
- **기본 인자:** `fun upcomingPaymentDate(today: LocalDate = LocalDate.now())`처럼 쓰면, Java에서 오버로드 두 개로 만들던 것을 하나로 끝내요. 테스트할 때 원하는 값을 넣을 수 있어서 편해요. 📍 `SubscriptionDates.kt`

### 1-3. 클래스와 상속
```kotlin
class AlarmReceiver : BroadcastReceiver() {          // extends와 implements 모두 :
    override fun onReceive(context: Context, intent: Intent) { }
}
class ReminderScheduler(private val context: Context) { }   // 생성자와 필드를 한 번에 선언
```
| Kotlin | Java |
|---|---|
| `: BroadcastReceiver()` | `extends BroadcastReceiver`. `()`는 부모 생성자 호출이에요. |
| `override` **키워드** (필수) | `@Override` 어노테이션 (선택) |
| 기본 `public` | 기본 package-private |
| `private`(최상위 선언) | **파일 안에서만** 보여요 |
| `NotificationChannel(...)` | `new NotificationChannel(...)`. **`new`가 없어요.** |

### 1-4. `companion object`와 `const val`: Java의 static 대신
```kotlin
companion object {
    const val EXTRA_NAME = "extra_name"            // public static final String
    private const val ALARM_ID_OFFSET = 100_000
}
```
- Kotlin에는 `static`이 없어요. 대신 클래스 안의 `companion object`에 넣고 `AlarmReceiver.EXTRA_NAME`으로 접근해요.
- 클래스 **안에서는** 접두어 없이 `EXTRA_NAME`만 써도 돼요.
- 다른 클래스와 공유하는 키(예: Intent extra 키)는 상수로 한 곳에 두면 오타를 막을 수 있어요. 📍 `AlarmReceiver`, `AlarmDismissReceiver`

### 1-5. 최상위 함수
클래스 밖, 파일 최상위에 함수를 둘 수 있어요. Java의 static 유틸 메서드에 해당해요. 📍 `needsExactAlarmPermission()`

### 1-6. 문자열 템플릿
```kotlin
"$name 결제 예정"                     // name + " 결제 예정"
"${price}원"                         // $price원 으로 쓰면 'price원'을 변수 이름으로 읽음
"다음 결제일: ${sub.upcomingPaymentDate()}"   // 식이나 함수 호출은 ${ }
"package:${context.packageName}"
```

### 1-7. 비교 연산자
| Kotlin | Java |
|---|---|
| `a == b` | `a.equals(b)`. **값 비교**이고 null-safe해요. |
| `a === b` | `a == b` (참조 비교) |
| `action != Intent.ACTION_BOOT_COMPLETED` | `!Intent.ACTION_BOOT_COMPLETED.equals(action)`. Kotlin에서는 순서를 신경 쓸 필요가 없어요. |

### 1-8. 비트 연산: 기호 대신 이름
```kotlin
flags = flags or Notification.FLAG_INSISTENT        // Java: flags | FLAG_INSISTENT
PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
```
`or`, `and`, `xor`, `inv()`, `shl`, `shr`를 써요. ⚠️ 실제로 `of`로 오타를 낸 적이 있어요.

### 1-9. 숫자 변환은 명시적으로
```kotlin
subscriptionId.toInt()          // Java: (int) subscriptionId
reminderDaysBefore.toLong()
```
Kotlin은 Int를 Long으로 바꾸는 **자동 확장 변환도 해 주지 않아요.**

### 1-10. getter/setter → 프로퍼티
```kotlin
intent.action                 // intent.getAction()
context.packageName           // context.getPackageName()
channel.description = "..."   // channel.setDescription("...")
```

### 1-11. 클래스 참조
```kotlin
AlarmReceiver::class.java              // Java: AlarmReceiver.class
context.getSystemService(AlarmManager::class.java)
```
`::class`는 Kotlin 클래스 정보(KClass)이고, Java API에 넘길 때는 `.java`를 붙여요.

### 1-12. `in` 연산자
```kotlin
if (ReminderType.ALARM in types)     // types.contains(ReminderType.ALARM)
```

---

## Part 2. Null 안전성

Kotlin은 타입에 **null 가능 여부**가 포함돼요. `String`은 절대 null이 아니고, `String?`는 null일 수 있어요. Java API에서 오는 값(예: `getStringExtra`)은 null일 수 있으니 `?` 타입으로 다뤄요.

### 2-1. 엘비스 연산자 `?:`: null이면 오른쪽
```kotlin
val name = intent.getStringExtra(EXTRA_NAME) ?: return
```
```java
String name = intent.getStringExtra(EXTRA_NAME);
if (name == null) return;
```
이 줄을 지나면 `name`은 `String`(non-null)으로 확정돼요. 오른쪽에는 값(`?: emptySet()`)이나 `return`, `throw`가 올 수 있어요.

### 2-2. 안전 호출 `?.`: null이면 호출하지 않고 null을 넘김
```kotlin
val types = intent.getStringArrayExtra(EXTRA_REMINDER_TYPES)   // Array<String>?
    ?.map { ReminderType.valueOf(it) }
    ?.toSet()
    ?: emptySet()
```
체인 중간에 null이 나오면 끝까지 null로 흘러가고, 마지막 `?:`에서 기본값으로 바뀌어요.

### 2-3. `?.let { }`
```kotlin
datePickerState.selectedDateMillis?.let { millis -> nextPaymentDate = ... }
```
null이 아닐 때만 블록을 실행해요. Java의 `if (x != null) { ... }`와 같아요. 📍 `AddEditSubscriptionScreen`, `Converters`

---

## Part 3. 람다와 컬렉션

### 3-1. 람다 기본
```kotlin
{ ReminderType.valueOf(it) }        // 매개변수가 하나면 it
{ millis -> ... }                   // 이름을 붙일 수도 있어요
{ _ -> ... }                        // 안 쓰는 매개변수는 _ ("의도적으로 무시")
```

### 3-2. 후행 람다 (trailing lambda)
함수의 **마지막 인자가 람다면 괄호 밖으로** 뺄 수 있어요.
```kotlin
rememberLauncherForActivityResult(contract) { isGranted -> ... }
// 원래 모양: rememberLauncherForActivityResult(contract, { isGranted -> ... })
Button(onClick = { ... }) { Text("저장") }
```

### 3-3. 레이블 리턴 `return@launch`
```kotlin
CoroutineScope(Dispatchers.IO).launch {
    val sub = repository.getById(id) ?: return@launch   // 이 람다만 끝냄
    ...
}
```
람다 안에서 그냥 `return`을 쓰면 **바깥 함수**를 리턴하려는 뜻이 돼서 에러가 나요. `return@함수이름`은 "**이 람다만** 끝내"라는 뜻이에요. Java 람다 안의 `return`과 같아요. `try/finally` 안이라면 `finally`는 실행돼요. 📍 `AlarmReceiver`

### 3-4. 컬렉션 함수
| Kotlin | Java |
|---|---|
| `listOf(a, b)` | `List.of(a, b)` (불변) |
| `setOf(x)`, `emptySet()` | `Set.of(x)`, `Collections.emptySet()` |
| `list.map { }` | `stream().map(...).collect(toList())` |
| `list.toSet()` | `new HashSet<>(list)` |
| `list.forEach { }` | `for (x : list) { }` |
| `list.sortedBy { it.date }` | `stream().sorted(comparing(x -> x.date))...`. 원본은 그대로 두고 새 리스트를 반환해요. |
| `types.joinToString(",") { it.name }` | `stream().map(...).collect(joining(","))` |
| `set + x`, `set - x` | 원소를 추가하거나 뺀 **새** Set (불변) |
| `array.toTypedArray()` | `list.toArray(new String[0])` |

---

## Part 4. 스코프 함수와 확장 함수

### 4-1. `apply { }`
```kotlin
val intent = Intent(context, AlarmDismissReceiver::class.java).apply {
    putExtra(EXTRA_NOTIFICATION_ID, notificationId)    // 블록 안의 this = Intent
}
// 블록이 끝나면 객체 자신을 반환해요
```
**객체를 만들면서 설정할 때** 써요. Java에서 `new` → setter 여러 줄로 쓰던 것을 한 덩어리로 묶어요.
```kotlin
.build().apply { flags = flags or Notification.FLAG_INSISTENT }   // 빌더가 없는 설정을 덧붙일 때
```

### 4-2. `also { }`
```kotlin
Room.databaseBuilder(...).build().also { INSTANT = it }
```
값을 그대로 반환하면서 **부수 작업**(여기서는 싱글톤 변수에 저장)을 해요. 블록 안에서는 `it`이에요. 📍 `AppDatabase`

### 4-3. 확장 함수: 클래스를 고치지 않고 메서드 추가
```kotlin
fun Subscription.upcomingPaymentDate(today: LocalDate = LocalDate.now()): LocalDate {
    var n = 0L
    while (paymentDateAt(n).isBefore(today)) n++     // this.paymentDateAt
    return paymentDateAt(n)
}

subscription.upcomingPaymentDate()   // 원래 있던 메서드처럼 호출
```
- 컴파일하면 사실 Java의 `static LocalDate upcomingPaymentDate(Subscription s, LocalDate today)`예요.
- data class는 데이터만 담고, 로직은 별도 파일로 분리할 때 좋아요.
- **확장 함수도 import해야** 쓸 수 있어요 (`import ...domain.model.upcomingPaymentDate`).
- 📍 `SubscriptionDates.kt`, `SubscriptionRepositoryImpl`의 `toDomain()`, `toEntity()`

---

## Part 5. `when`, data class, enum, sealed class

### 5-1. `when`: 값을 돌려주는 switch
```kotlin
fun Subscription.paymentDateAt(n: Long): LocalDate = when (cycle) {
    PaymentCycle.MONTHLY -> nextPaymentDate.plusMonths(n)
    PaymentCycle.YEARLY -> nextPaymentDate.plusYears(n)
}
```
- `when`은 **식**이라 결과를 바로 반환하거나 변수에 담을 수 있어요.
- enum의 모든 경우를 다루면 `else`가 필요 없어요. **나중에 enum 값을 추가하면 컴파일 에러로 알려 줘요.**
- 조건만 쓰는 형태도 있어요: `when { dynamicColor && ... -> ...; darkTheme -> ...; else -> ... }` 📍 `Theme.kt`

### 5-2. data class
```kotlin
data class Subscription(val id: Long, val name: String, val reminderDaysBefore: Int = 1, ...)
```
`equals`, `hashCode`, `toString`, `copy`가 자동으로 만들어져요. Java의 record, 또는 Lombok `@Data`와 비슷해요.
```kotlin
subscription.copy(id = id)     // 일부만 바꾼 새 객체 (불변 객체 다루기)
```

### 5-3. 이름 붙은 인자
```kotlin
Subscription(id = 0L, name = name, price = price.toIntOrNull() ?: 0, ...)
```
순서와 상관없이 이름으로 넘겨요. 인자가 많을 때 읽기 쉬워요.

### 5-4. sealed class + data object
```kotlin
sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object AddEdit : Screen("add_edit")
}
```
**하위 타입이 정해진** 클래스예요. `object`는 싱글톤이에요. 📍 `Screen.kt`

---

## Part 6. 코루틴

### 6-1. 핵심: 스코프 / 디스패처 / suspend
| 개념 | 의미 | Java 비유 |
|---|---|---|
| **CoroutineScope** | 코루틴의 **수명**. 스코프가 취소되면 안의 코루틴도 전부 취소돼요. | 화면이 닫힐 때 `executor.shutdownNow()`를 자동으로 호출해 주는 Executor |
| **Dispatcher** | **어느 스레드에서** 실행할지 | Executor 종류 |
| **suspend 함수** | 기다리는 동안 **스레드를 반납**하는 함수 | (대응하는 개념 없음) |

**코루틴 ≠ 스레드예요.** 여러 코루틴이 소수의 스레드를 나눠 쓰고, `suspend`에서 기다리는 동안에는 스레드를 놓아줘요. 그래서 코루틴 1만 개는 가볍지만 스레드 1만 개는 무거워요.

| Dispatcher | 용도 | Java 비유 |
|---|---|---|
| `Dispatchers.Main` | UI | `runOnUiThread` |
| `Dispatchers.IO` | DB, 네트워크 | `newCachedThreadPool()` |
| `Dispatchers.Default` | CPU 계산 | `newFixedThreadPool(코어 수)` |

| 스코프 | 취소 시점 |
|---|---|
| `viewModelScope` | ViewModel `onCleared()` (화면이 백스택에서 빠질 때) |
| `lifecycleScope` | Activity/Fragment destroy |
| `CoroutineScope(Dispatchers.IO)` (직접 생성) | **아무도 취소하지 않아요.** 블록이 끝나거나 프로세스가 죽을 때까지 살아 있어요. |

### 6-2. 콜백 대신 순차 코드
```kotlin
// Java: getAll(new Callback() { onResult(list) {...}; onError(e) {...} })
val list = repository.getAll().first()   // 여기서 suspend → 결과가 오면 다음 줄
list.forEach { scheduler.schedule(it) }
```
에러도 콜백이 아니라 일반 `try/catch`로 잡아요.

| 하고 싶은 것 | 방법 |
|---|---|
| 실행만 | `launch { }` → `Job` |
| 결과 받기 | `async { }` → `Deferred<T>` → `await()` (Future와 비슷하지만 스레드를 블록하지 않아요) |
| 중간에 스레드 바꾸기 | `withContext(Dispatchers.Main) { }` |
| 콜백 API를 suspend로 감싸기 | `suspendCancellableCoroutine` |

### 6-3. BroadcastReceiver에서 코루틴 쓰기: `goAsync()`
```kotlin
val pendingResult = goAsync()
CoroutineScope(Dispatchers.IO).launch {
    try {
        // DB 작업
    } finally {
        pendingResult.finish()     // 예외가 나도 반드시 호출
    }
}
```
- `onReceive()`는 **메인 스레드**에서 돌고, **리턴하면 프로세스가 죽을 수 있어요.**
- Room은 메인 스레드 조회를 막아요 → 백그라운드에서 해야 해요 → `onReceive`가 먼저 리턴해 버려요.
- `goAsync()`는 시스템에게 "`finish()` 부를 때까지 기다려 줘(최대 약 10초)"라고 알려요.
- 직접 만든 스코프는 아무도 관리하지 않으니, `goAsync`와 **한 쌍으로** 써요.
- 📍 `BootReceiver`, `AlarmReceiver`

### 6-4. ViewModel에서
```kotlin
fun save(subscription: Subscription) {
    viewModelScope.launch {
        val id = addSubscription(subscription)              // suspend
        reminderScheduler.schedule(subscription.copy(id = id))
    }
}
```
`addSubscription(...)`처럼 객체를 함수처럼 호출할 수 있는 건 UseCase에 `operator fun invoke`가 있기 때문이에요.

---

## Part 7. Flow

### 7-1. Flow란
**값을 여러 번 흘려보내는 비동기 스트림**이에요. Room DAO가 `Flow<List<...>>`를 돌려주면, **DB가 바뀔 때마다** 새 리스트가 흘러와요. Java로 비유하면 LiveData나 RxJava Observable이에요.

### 7-2. 주요 연산
```kotlin
getSubscriptions()                                          // Flow<List<Subscription>>
    .map { list -> list.sortedBy { it.upcomingPaymentDate() } }   // 흘러오는 값마다 변환
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```
| 연산 | 의미 |
|---|---|
| `.map { }` | 값마다 변환해요. ⚠️ Flow의 map과 List의 map은 **다른 함수**예요. 위 코드에서는 바깥이 Flow, 안쪽 `list`가 List예요. |
| `.first()` | **첫 값 하나만** 받고 끝내요 (suspend). 1회 조회가 필요할 때 써요. `collect`하면 끝나지 않아요. `import kotlinx.coroutines.flow.first`가 필요해요. |
| `.stateIn(...)` | Flow를 **현재 값을 가진** `StateFlow`로 바꿔요. 화면이 구독할 수 있어요. |
| `WhileSubscribed(5000)` | 화면이 안 보인 지 5초가 지나면 DB 관찰을 멈춰요. 회전처럼 잠깐 사라졌다 나타나는 경우에는 끊기지 않아요. |

Compose에서는 `viewModel.subscriptions.collectAsState()`로 받아서 화면에 그려요.

---

## Part 8. Jetpack Compose

### 8-1. 선언형 UI의 핵심
- `@Composable` 함수는 화면을 **그리는 함수**예요. 상태가 바뀌면 **다시 실행(recomposition)**돼요.
- XML + `findViewById` + `setText`처럼 **명령**하는 대신, "**이 상태면 화면은 이렇다**"고 **선언**해요.

### 8-2. 상태: `remember` + `mutableStateOf` + `by`
```kotlin
var showDialog by remember { mutableStateOf(false) }
```
| 부분 | 역할 |
|---|---|
| `mutableStateOf(false)` | 값이 바뀌면 **다시 그리게 하는** 상자 |
| `remember { }` | 다시 그려질 때 초기화되지 않고 **값을 기억** |
| `by` | 위임. `.value` 없이 일반 변수처럼 읽고 써요. (`getValue`, `setValue` import 필요) |

Java XML에 비유하면 "값을 바꾸고 `notifyDataSetChanged()`를 직접 호출"하던 걸 자동으로 해 주는 거예요.

### 8-3. 다이얼로그도 선언형
```kotlin
if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("...") },       // 슬롯: 문자열이 아니라 "그릴 Composable"을 받아요
        confirmButton = { TextButton(onClick = { ... }) { Text("확인") } },
    )
}
```
`show()`나 `dismiss()`가 없어요. 상태가 `true`면 나타나고 `false`면 사라져요. 📍 `ReminderPermissionRequester`, `AddEditSubscriptionScreen`(DatePicker)

### 8-4. Side Effect: `LaunchedEffect`
```kotlin
LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
```
Composable 본문은 여러 번 실행되니까, 거기서 바로 팝업을 띄우면 **계속 뜨게** 돼요. `LaunchedEffect(key)`는 **화면에 처음 나타날 때 한 번**(그리고 key가 바뀔 때) 실행해요. `Unit`은 바뀌지 않는 key라서 "딱 한 번"이 돼요. Java의 `onCreate`에서 한 번 호출하는 것과 비슷해요.

### 8-5. 권한 요청: `rememberLauncherForActivityResult`
```kotlin
val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }
launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
```
Java Activity의 `registerForActivityResult(new RequestPermission(), granted -> ...)`를 Compose 버전으로 쓴 거예요.

### 8-6. Context 얻기
```kotlin
val context = LocalContext.current
```
Composable은 Activity가 아니라 함수라서 `this`가 없어요.

### 8-7. 레이아웃 기본
| Compose | XML |
|---|---|
| `Column` / `Row` | 세로 / 가로 `LinearLayout` |
| `LazyColumn { items(list) { } }` | `RecyclerView` + Adapter |
| `Scaffold(floatingActionButton = { })` | CoordinatorLayout + FAB |
| `Modifier.padding(16.dp).fillMaxWidth()` | `layout_*`, `padding` 속성 |

### 8-8. 화면을 그리지 않는 Composable
`ReminderPermissionRequester()`는 UI 없이 **동작만** 해요(권한 요청). Compose 기능(`LaunchedEffect`, 런처)은 Composable 안에서만 쓸 수 있어서 이런 형태가 돼요.

### 8-9. Navigation + ViewModel 생성
```kotlin
composable(Screen.List.route) {
    val viewModel: SubscriptionListViewModel = viewModel(
        factory = viewModelFactory { initializer { SubscriptionListViewModel(...) } }
    )
}
```
DI 없이 ViewModel에 생성자 인자를 넘기는 방법이에요. 📍 `SubscriptionNavHost`

---

## Part 9. Room (이전 작업)

| 구성 | 역할 |
|---|---|
| `@Entity` data class | 테이블 |
| `@Dao` interface | 쿼리. `suspend fun`은 1회 실행, `Flow` 반환은 변경을 관찰해요. |
| `@Database` abstract class | DB 인스턴스 (싱글톤) |
| `@TypeConverter` | `LocalDate` ↔ Long, `Set<ReminderType>` ↔ CSV 문자열처럼 Room이 모르는 타입을 변환해요. |

- 어노테이션 처리기는 KAPT 대신 **KSP**(`ksp(libs.androidx.room.compiler)`)를 써요.
- **Entity와 도메인 모델을 분리**하고, Repository에서 `toDomain()`, `toEntity()`로 변환해요. 도메인이 Room에 의존하지 않게 하려고요.

---

## Part 10. Android 플랫폼 (알림 / 알람)

### 10-1. 알림 채널 (API 26+)
- **채널이 없는 상태로 `notify()`를 하면 예외 없이 알림이 버려져요.** logcat에 `No Channel found`만 남아요.
- 같은 ID로 다시 생성하면 아무 일도 일어나지 않아요(멱등). 그래서 매번 만들어도 괜찮아요.
- 한 번 만들어진 채널의 중요도나 소리는 **코드로 바꿀 수 없어요.** 바꾸려면 앱을 다시 설치해야 해요.
- minSdk가 24면 `if (SDK_INT < O) return`으로 버전을 체크해야 해요.

### 10-2. 권한
| 권한 | API | 요청 방식 |
|---|---|---|
| `POST_NOTIFICATIONS` | 33+ | 런타임 권한, 시스템 팝업 |
| `SCHEDULE_EXACT_ALARM` | 31+ | **팝업이 없어요.** `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`으로 설정 화면에 보내요. Android 14+에서는 새로 설치하면 기본값이 꺼짐이에요. |
| `RECEIVE_BOOT_COMPLETED` | - | Manifest 선언만 하면 돼요. |

⚠️ **API 33 미만에서 `checkSelfPermission(POST_NOTIFICATIONS)`은 항상 DENIED예요.** 그 권한이 존재하지 않기 때문이에요. 그래서 버전 체크가 필요해요.
```kotlin
val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED
```

### 10-3. AlarmManager
- `setExactAndAllowWhileIdle(RTC_WAKEUP, millis, pendingIntent)`: 도즈 모드에서도 정확한 시각에 실행돼요.
- **이미 지난 시각으로 예약하면 즉시 실행돼요.** 그래서 `nextReminderAt()`은 항상 미래 시각을 돌려줘요.
- **재부팅하면 예약이 전부 사라져요** → BootReceiver로 다시 예약해요.
- `canScheduleExactAlarms()`가 false면 예약할 수 없어요.

### 10-4. PendingIntent
"**나중에 다른 프로세스(시스템)가 내 앱 대신 실행해 줄 Intent**"예요. AlarmManager나 알림 버튼처럼 시스템이 실행하는 곳에 넘겨요.
- **requestCode**를 구독마다 다르게 줘야 서로 덮어쓰지 않아요.
- Android 12+에서는 `FLAG_IMMUTABLE`이나 `FLAG_MUTABLE`을 **반드시** 지정해야 해요. 없으면 크래시가 나요.

### 10-5. 알림 옵션
| 옵션 | 효과 |
|---|---|
| `setAutoCancel(true)` | 누르면 사라져요 |
| `setOngoing(true)` | 스와이프로 지울 수 없어요 → **끄는 방법(버튼)을 반드시 제공해야 해요** |
| `setCategory(CATEGORY_ALARM)` | 시스템에 알람이라고 알려요 |
| `FLAG_INSISTENT` | 알림창을 열 때까지 소리를 반복해요 |
| `.addAction(0, "확인", pendingIntent)` | 버튼을 추가해요. **`.build()` 전에** 호출해야 해요. |
| `NotificationManagerCompat.cancel(id)` | `notify(id)`로 띄운 알림을 지워요 (ongoing도 가능) |

### 10-6. BroadcastReceiver `exported`
| 리시버 | exported | 이유 |
|---|---|---|
| `AlarmReceiver`, `AlarmDismissReceiver` | `false` | 우리 앱의 PendingIntent로만 호출돼요 |
| `BootReceiver` | `true` | **시스템** 브로드캐스트를 받아요. 보호된 브로드캐스트라 다른 앱이 가짜로 보낼 수 없고, 코드에서 action을 한 번 더 확인해요. |

**Manifest에 등록하지 않은 리시버는 에러 없이 호출되지 않아요.**

### 10-7. 날짜 계산 함정
`LocalDate.plusMonths()`를 **누적**하면 말일 보정 때문에 날짜가 밀려요(`1/31 → 2/28 → 3/28`). **기준일에서 n을 곱해서** 계산해야 해요(`기준일.plusMonths(n)`).

---

## Part 11. Android Studio 팁

| 단축키 / 기능 | 용도 |
|---|---|
| **Shift+F6** (Rename) | 이름을 바꾸면 호출하는 곳까지 같이 바뀌어요. 오타 수정은 이걸로 하세요. |
| **⌃⌥O** (Optimize Imports) | 안 쓰는 import를 정리해요. **현재 열린 탭**에만 적용돼요. |
| **⌘F9** (Make Project) | 전체 빌드예요. **Manifest 오류**는 Kotlin 컴파일만으로는 안 잡혀요. |
| 파일 드래그 (Refactor → Move) | package 선언과 import를 자동으로 바꿔 줘요. |
| Alt+Enter 자동 import | ⚠️ 이름이 같은 클래스가 있으면 엉뚱한 것을 고를 수 있어요. Compose 코드에 `android.app.*`이나 `android.R.*`이 보이면 의심하세요. |
| 회색 코드 | 사용되지 않는 변수나 import예요. 무시하지 말고 왜 안 쓰이는지 확인하세요. |
