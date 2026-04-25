package com.storeinvoice.storeinvoiceapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer("postgres:latest");
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		return new RabbitMQContainer("rabbitmq:3.13-alpine");
	}

}

