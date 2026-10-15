FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package \
	&& java -Djarmode=tools -jar target/spring-petclinic-4.0.0-SNAPSHOT.jar extract --layers --destination target/extracted

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
	&& apt-get install --yes --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/* \
	&& groupadd --system petclinic \
	&& useradd --system --gid petclinic --home-dir /app petclinic
WORKDIR /app
COPY --from=build --chown=petclinic:petclinic /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=petclinic:petclinic /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=petclinic:petclinic /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=petclinic:petclinic /workspace/target/extracted/application/ ./

USER petclinic
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
	CMD curl --fail --silent http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
