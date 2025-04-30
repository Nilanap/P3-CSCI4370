
package edu.uga.csci4370.group12.project3.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        int port = event.getApplicationContext().getEnvironment().getProperty("server.port", Integer.class, 8080);
        String host = "localhost";
        String url = String.format("http://%s:%d", host, port);
        System.out.println("--------------------------------------------------");
        System.out.println("Application is running! Access it at: " + url);
        System.out.println("--------------------------------------------------");
    }
}