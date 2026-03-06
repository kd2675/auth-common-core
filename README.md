# auth-common-core

인증 공통 라이브러리입니다. 사용자 컨텍스트, 인증 DTO, 예외, OpenFeign 기반 사용자 조회 클라이언트를 제공합니다.

## 역할
- 로그인/토큰 관련 DTO 제공
- `UserContext` 및 argument resolver 제공
- 인증 예외/상수 제공
- 다른 서비스에서 `auth-back-server`를 조회할 Feign client 제공

## 주요 패키지
- `auth.common.core.client`
- `auth.common.core.constant`
- `auth.common.core.context`
- `auth.common.core.dto`
- `auth.common.core.exception`

## 대표 파일
- `src/main/java/auth/common/core/client/UserServiceClient.java`
- `src/main/java/auth/common/core/context/UserContext.java`
- `src/main/java/auth/common/core/context/UserContextArgumentResolver.java`
- `src/main/java/auth/common/core/dto/`
- `src/main/java/auth/common/core/exception/`

## 빌드
```bash
./gradlew :auth-common-core:compileJava
./gradlew :auth-common-core:test
```

## 참고
- 라이브러리 모듈이라 `bootRun` 대상이 아닙니다.
- 현재 코드 기준 주요 사용처는 `auth-back-server`, `zeroq-back-service`, `muse-back-service`, `semo-back-service`입니다.
- 계약 변경 시 하위 서비스 전반에 영향이 있으므로 DTO/예외 호환성을 우선합니다.
