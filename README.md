## 기본 환경
- JDK 21
- Docker

## Docker 기반 local 환경 셋팅

### MySQL 컨테이너 생성
로컬 앱은 `application-local.yaml`에 맞춰 MySQL 컨테이너를 만든다.

| Docker 설정      | application-local.yaml 설정               |
|------------------|-------------------------------------------|
| `MYSQL_DATABASE` | JDBC URL의 데이터베이스 이름 (`tikitaka`) |
| `MYSQL_USER`     | `spring.datasource.username`              |
| `MYSQL_PASSWORD` | `spring.datasource.password`              |
| `로컬포트`         | JDBC URL의 로컬 포트                      |

- 호스트(로컬) 포트는 기본 `3306`으로 통일하되, 이미 사용중이라면 비어 있는 포트를 고른다. 
- 컨테이너 안 MySQL 포트는 `3306`으로 고정한다.

```bash
docker run -d \
  --name tikitaka-mysql \
  -p <로컬포트>:3306 \
  -e MYSQL_ROOT_PASSWORD=<spring.datasource.password> \
  -e MYSQL_DATABASE=tikitaka \
  -e MYSQL_USER=<spring.datasource.username> \
  -e MYSQL_PASSWORD=<spring.datasource.password> \
  --restart unless-stopped \
  mysql:8.0
```


### Redis 컨테이너 생성
- Redis는 비밀번호 없이 사용한다.
- 호스트(로컬) 포트는 기본 `6379`으로 통일하되, 이미 사용중이라면 비어 있는 포트를 고른다.
- 컨테이너 안 MySQL 포트는 `6379`으로 고정한다.

```bash
docker run -d \
  --name tikitaka-redis \
  -p <로컬포트>:6379 \
  --restart unless-stopped \
  redis:latest
```

### 컨테이너 관리

```bash
docker ps
docker stop tikitaka-mysql tikitaka-redis
docker start tikitaka-mysql tikitaka-redis
```

## 로컬 실행

`application.yaml`의 기본 프로필은 `local`이다.

- 앱 주소: `http://localhost:8080`
- API 문서: `http://localhost:8080/swagger-ui.html`
- 첫 실행 시 `ddl-auto: update`가 테이블을 만든다.
- 업로드 파일은 프로젝트 루트의 `uploads`에 저장된다. (이 폴더는 Git에 포함하지 않는다.)
