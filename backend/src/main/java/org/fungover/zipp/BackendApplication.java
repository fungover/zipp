package org.fungover.zipp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    /**
     * Entry point for the backend application.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    // Prevent instantiation
    private BackendApplication() {
        throw new UnsupportedOperationException();
    }
}
