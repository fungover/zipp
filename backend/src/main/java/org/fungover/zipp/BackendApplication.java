package org.fungover.zipp;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    /**
     * Entry point for the backend application.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic("report-avro", 1, (short) 1);
    }
}
