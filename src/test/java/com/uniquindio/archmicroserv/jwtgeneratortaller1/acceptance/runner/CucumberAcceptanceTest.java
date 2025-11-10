package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Suite de pruebas de aceptación con Cucumber para el servicio de autenticación JWT.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, summary, html:target/cucumber-report.html")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "")
public class CucumberAcceptanceTest {
}

