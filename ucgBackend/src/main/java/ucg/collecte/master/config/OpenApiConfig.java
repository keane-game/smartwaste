package ucg.collecte.master.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {

    @Bean
    public OpenAPI applicationOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("UCG Collecte ")
                        .description("")
                        .version("V1.0")
                )
                .externalDocs(new ExternalDocumentation()
                        .description(""));


    }
}
