package com.storeinvoice.store_invoice_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class StoreInvoiceApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
