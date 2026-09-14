package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Superseded by Testcontainers integration tests")
@SpringBootTest(classes = com.eop.EopApplication.class)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
