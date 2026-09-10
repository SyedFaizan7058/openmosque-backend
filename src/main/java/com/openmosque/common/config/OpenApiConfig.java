package com.openmosque.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        // Prevent Swagger UI from generating query parameter inputs for resolved authentication principals
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(com.openmosque.modules.user.entity.User.class);
        SpringDocUtils.getConfig().addAnnotationsToIgnore(com.openmosque.security.annotation.CurrentUser.class);
    }

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openMosqueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpenMosque REST API")
                        .description("API Documentation for OpenMosque community platform and prayer times engine")
                        .version("v1.0.0")
                        .contact(new Contact().name("OpenMosque Team").email("support@openmosque.org"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide Firebase ID Token or dev mock token (e.g., 'mock-admin-1', 'mock-moderator-1', 'mock-user-1') as Bearer token.")));
    }
}
