package sonaged.collecte.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // purge planifiée du soft-delete (DeletionPurgeScheduler)
public class SonagedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonagedApplication.class, args);
	}


}
