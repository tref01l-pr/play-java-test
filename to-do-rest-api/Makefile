.PHONY: first-run run stop clean rebuild

first-run:
	docker-compose build
	docker-compose up -d
	@timeout 10
	docker exec mongo1 /scripts/rs-init.sh
	@echo "Restarting containers..."
	docker-compose restart
	@echo "Setup complete! The application is now running."

run:
	docker-compose up -d

stop:
	docker-compose down

clean:
	docker-compose down -v

rebuild:
    docker-compose down -v
    docker-compose build
    docker-compose up -d
    @timeout 10
    docker exec mongo1 /scripts/rs-init.sh
    @echo "Restarting containers..."
    docker-compose restart