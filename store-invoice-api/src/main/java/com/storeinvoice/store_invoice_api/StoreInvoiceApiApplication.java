package com.storeinvoice.store_invoice_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external")
@ComponentScan(basePackages = {
    "com.storeinvoice.store_invoice_api",
    "com.storeinvoice.infrastructure",
    "com.storeinvoice.application",
    "com.storeinvoice.domain"
})
public class StoreInvoiceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreInvoiceApiApplication.class, args);
	}

}
