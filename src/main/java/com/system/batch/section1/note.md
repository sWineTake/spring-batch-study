# Spring Batch - @JobScope & @StepScope 빈 생명주기

## 전체 흐름 개요

다이어그램은 `@JobScope`와 `@StepScope` 빈이 **언제 생성되고 소멸되는지**를 나란히 보여줍니다.

---

## 공통 초기 흐름

```
Application Start
       ↓
Proxy for @JobScope & @StepScope Beans
```

- 앱 시작 시점에 실제 빈을 바로 만들지 않고, **프록시 객체**만 먼저 등록합니다.
- 실제 빈 초기화는 각 Scope가 시작될 때까지 **지연(Lazy)** 됩니다.

---

## @JobScope 생명주기

| 단계 | 설명 |
|------|------|
| `JobExecution start` | Job이 실행되는 시점 |
| `@JobScope Bean Initialized` | 빈이 실제로 생성됨 |
| `JobParameters Injected` | `@Value("#{jobParameters[...]}")` 값이 주입됨 |
| `JobExecution End` | Job 실행 종료 |
| `@JobScope Bean Destroyed` | 빈이 소멸됨 |

> **하나의 Job 실행 동안 빈이 유지됩니다.**

---

## @StepScope 생명주기

| 단계 | 설명 |
|------|------|
| `StepExecution start` | Step이 실행되는 시점 |
| `@StepScope Bean Initialized` | 빈이 실제로 생성됨 |
| `JobParameters Injected` | `@Value("#{jobParameters[...]}")` 값이 주입됨 |
| `StepExecution End` | Step 실행 종료 |
| `@StepScope Bean Destroyed` | 빈이 소멸됨 |

> **하나의 Step 실행 동안 빈이 유지됩니다.**

---

## 핵심 포인트 정리

### 왜 프록시를 쓰는가?

- Spring 컨테이너는 시작 시 모든 빈을 등록해야 하는데, `@JobScope` / `@StepScope` 빈은 **그 시점에 JobParameters가 없음**
- 그래서 프록시로 자리만 잡아두고, 실제 Scope가 활성화될 때 진짜 빈을 생성

---

### @JobScope vs @StepScope 비교

| | @JobScope | @StepScope |
|---|---|---|
| 생존 범위 | Job 실행 단위 | Step 실행 단위 |
| 주로 사용 | `JobExecutionListener`, `Step` | `ItemReader`, `ItemWriter`, `ItemProcessor` |
| 접근 가능 컨텍스트 | `jobParameters`, `jobExecutionContext` | 위 + `stepExecutionContext` |

---

### 실제 사용 예시

```java
// @JobScope 예시 - Job 실행 시점에 파라미터 주입
@Bean
@JobScope
public Step myStep(@Value("#{jobParameters['date']}") String date) {
    // Job 실행 시점에 date 값이 주입됨
}

// @StepScope 예시 - Step 실행 시점에 파라미터 주입
@Bean
@StepScope
public ItemReader<MyEntity> myReader(@Value("#{jobParameters['date']}") String date) {
    // Step 실행 시점에 date 값이 주입됨
}
```

---

## 요약

> 두 Scope 모두 **Late Binding(지연 바인딩)** 을 통해 실행 시점의 동적 파라미터를 빈에 주입할 수 있게 해주는 것이 핵심입니다.
