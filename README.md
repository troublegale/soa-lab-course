### Setup
```
git clone https://github.com/troublegale/soa-lab-course
cd soal-lab-course/spring
chmod +x mvnw
./mvnw clean install
cd ../jax-rs
chmod +x gradlew
./gradlew clean war
cd ..
docker compose up -d --build
```
Frontend available at https://localhost:8448