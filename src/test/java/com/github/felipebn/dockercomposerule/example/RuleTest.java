package com.github.felipebn.dockercomposerule.example;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.joda.time.Duration;
import org.junit.Assert;
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
        //This needs to be defined according to the target environment
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
    public void testContainers() throws InterruptedException, UnknownHostException, IOException {
        DockerPort neo4jOnePort = docker.containers().container("neo4j_one").port(7687);
        DockerPort neo4jTwoPort = docker.containers().container("neo4j_two").port(7687);
        Assert.assertNotEquals(neo4jOnePort.getExternalPort(), neo4jTwoPort.getExternalPort());

        assertAbleToConnect(neo4jOnePort);
        assertAbleToConnect(neo4jTwoPort);
    }

    private void assertAbleToConnect(DockerPort port) throws IOException {
        SocketAddress target = new InetSocketAddress("localhost", port.getExternalPort());
        try(Socket s = new Socket()){
            s.connect(target, 1000);
            if(! s.isConnected() ) {
                Assert.fail("Not able to connect to " + target);
            }
        }
    }
}
