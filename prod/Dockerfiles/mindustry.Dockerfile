FROM eclipse-temurin:17

WORKDIR /app

ADD https://github.com/Anuken/Mindustry/releases/download/v146/server-release.jar server-release.jar
ADD https://github.com/xpdustry/kotlin-runtime/releases/download/v3.1.1-k.1.9.22/kotlin-runtime.jar config/mods/kotlin-runtime.jar

ADD https://github.com/kennarddh-mindustry/genesis/releases/download/v3.0.0-beta.26/genesis-core-3.0.0-beta.26.jar config/mods/genesis-core.jar
ADD https://github.com/kennarddh-mindustry/genesis/releases/download/v3.0.0-beta.26/genesis-standard-3.0.0-beta.26.jar config/mods/genesis-standard.jar

ADD https://github.com/kennarddh-mindustry/plague/releases/download/v0.0.17/plague-core-0.0.17.jar config/mods/plague-core.jar

# For ss command. ss command is used for healthcheck
RUN apt-get update && apt-get install -y iproute2

# https://dzone.com/articles/gracefully-shutting-down-java-in-containers
# https://stackoverflow.com/questions/542979/where-dump-is-dumped-using-heapdumponoutofmemoryerror-parameter-for-jboss
ENTRYPOINT exec java -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dumps/ -jar server-release.jar
