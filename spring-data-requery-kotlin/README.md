# spring-data-requery-kotlin

Kotlin 언어로 개발할 때 필요한 모듈입니다.

현재 requery 자체에서는 `CompletableFuture` 를 활용하는 비동기 방식의 처리와 `Reactive` 방식을 지원합니다.
이 프로젝트에서는 Kotlin `Coroutines` 를 사용하여, 더욱 간단하고, 구현하기 쉬운 방식을 제공합니다.

spring-data-requery 처음 구현 시에는 모두 Kotlin으로 구현했는데, Java 개발자들의 feedback으로 대부분의 코드를 Java로 다시 구현했습니다.
이런 이유로, 중복된 코드가 상당히 많습니다. 앞으로 이런 중복 코드는 모두 Java 코드만 남겨놓고 Kotlin으로 구현된 코드는 제거할 계획입니다. 