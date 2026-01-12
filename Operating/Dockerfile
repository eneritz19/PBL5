# ==========================================
# ETAPA 1: Compilación (Build)
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copiamos el pom.xml y descargamos dependencias (para aprovechar la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copiamos el código fuente
COPY src ./src

# 3. Compilamos y COPIAMOS LAS DEPENDENCIAS a la carpeta target/lib
# El comando 'dependency:copy-dependencies' es la clave aquí porque tu POM no crea un Fat-JAR
RUN mvn clean package dependency:copy-dependencies -DoutputDirectory=target/lib

# ==========================================
# ETAPA 2: Ejecución (Runtime)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Copiamos el JAR de tu aplicación (fíjate en el nombre que pone en tu POM: skinxpert + 1.0-SNAPSHOT)
COPY --from=build /app/target/skinxpert-1.0-SNAPSHOT.jar app.jar

# 2. Copiamos la carpeta 'lib' donde se descargó GSON en la etapa anterior
COPY --from=build /app/target/lib ./lib

# 3. Exponemos el puerto del servidor HTTP
EXPOSE 8082

# 4. COMANDO DE ARRANQUE (Crucial)
# No usamos 'java -jar' porque tu jar está vacío de dependencias.
# Usamos 'java -cp' para incluir tu jar + la carpeta lib (donde está Gson)
ENTRYPOINT ["java", "-cp", "app.jar:lib/*", "edu.mondragon.os.monitors.skinxpert.Main"]