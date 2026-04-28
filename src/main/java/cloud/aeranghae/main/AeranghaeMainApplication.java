package cloud.aeranghae.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AeranghaeMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AeranghaeMainApplication.class, args);
    }

}
