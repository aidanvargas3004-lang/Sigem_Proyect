package com.transur.sigem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/** Prueba de arranque contra PostgreSQL: se ejecuta solo si se define DB_URL. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class SigemApplicationTests {

	@Test
	void contextLoads() {
	}

}
