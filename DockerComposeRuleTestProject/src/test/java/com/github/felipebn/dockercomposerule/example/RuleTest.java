package com.github.felipebn.dockercomposerule.example;
import org.joda.time.Duration;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import com.palantir.docker.compose.logging.FileLogCollector;

public class RuleTest {

    @ClassRule
    public static final EnvironmentVariables environmentVariables  = new EnvironmentVariables() {{
        set("DOCKER_COMPOSE_LOCATION", "C:/Program Files/Docker/Docker/resources/bin/docker-compose.exe");
        set("DOCKER_LOCATION", "C:/Program Files/Docker/Docker/resources/bin/docker.exe");
    }};


    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
        .file("src/test/java/docker-compose.yml")
        .logCollector(FileLogCollector.fromPath("src/test/java/logs"))
        .waitingForService("neo4j_one", HealthChecks.toRespond2xxOverHttp(7474, (port) -> "http://localhost:"+port.getExternalPort()+"/browser/"), Duration.standardMinutes(10))
        .waitingForService("neo4j_two", HealthChecks.toRespond2xxOverHttp(7474, (port) -> "http://localhost:"+port.getExternalPort()+"/browser/"), Duration.standardMinutes(10))
        .pullOnStartup(false)
        .build();


    @Test
    public void testContainers() throws InterruptedException {
        DockerPort neo4jOnePort = docker.containers().container("neo4j_one").port(7687);
        DockerPort neo4jTwoPort = docker.containers().container("neo4j_two").port(7687);
        System.out.println("Neo4j One port: " + neo4jOnePort.getExternalPort());
        System.out.println("Neo4j Two port: " + neo4jTwoPort.getExternalPort());
    }
}
