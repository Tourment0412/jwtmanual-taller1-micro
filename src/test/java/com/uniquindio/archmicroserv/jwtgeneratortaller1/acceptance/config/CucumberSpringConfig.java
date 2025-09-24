package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ TestContainersConfig.class, RabbitTestConfig.class, AdminSeedConfig.class })
@ActiveProfiles("test")
public class CucumberSpringConfig extends TestContainersConfig {
}


