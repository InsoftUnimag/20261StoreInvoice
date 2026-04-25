package com.storeinvoice.storeinvoiceapi;

import org.springframework.boot.SpringApplication;

public class TestStoreInvoiceApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(StoreInvoiceApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

