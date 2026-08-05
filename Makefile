.PHONY: all test build run docker-up docker-down clean

all: test build

test:
	@echo "🧪 Запуск тестов..."
	./run_tests.sh

build:
	@echo "🏗️  Сборка проекта..."
	mvn clean package -DskipTests

run:
	@echo "🚀 Запуск приложения..."
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-up:
	@echo "🐳 Запуск Docker контейнеров..."
	docker-compose up -d

docker-down:
	@echo "🐳 Остановка Docker контейнеров..."
	docker-compose down

clean:
	@echo "🧹 Очистка проекта..."
	mvn clean
	rm -rf logs/
	docker-compose down -v

coverage:
	@echo "📊 Показать покрытие тестами..."
	open target/site/jacoco/index.html 2>/dev/null || \
	echo "Отчет: file://$(PWD)/target/site/jacoco/index.html"

help:
	@echo "Доступные команды:"
	@echo "  make test     - Запустить все тесты"
	@echo "  make build    - Собрать проект"
	@echo "  make run      - Запустить приложение локально"
	@echo "  make docker-up - Запустить в Docker"
	@echo "  make clean    - Очистить проект"
	@echo "  make coverage - Показать отчет о покрытии"
