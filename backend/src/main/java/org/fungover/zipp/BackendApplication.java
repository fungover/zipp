package org.fungover.zipp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class BackendApplication {

    private BackendApplication() {
        throw new UnsupportedOperationException
                ("This is a utility class and cannot be instantiated");
    }

    /**
     * Entry point for the backend application.
     *
     * @param args the command line arguments
     */

    public static void main(final String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
