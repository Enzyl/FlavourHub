package pl.dlusk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MyFinalProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyFinalProjectApplication.class, args);
    }
}
