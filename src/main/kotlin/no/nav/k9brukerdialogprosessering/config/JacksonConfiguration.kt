package no.nav.k9brukerdialogprosessering.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.k9.søknad.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {

    @Bean
    @Autowired
    fun objectMapper(kotlinModule: KotlinModule): ObjectMapper {
        return JsonUtils.getObjectMapper()
            .registerModule(kotlinModule)
    }

    @Bean
    fun kotlinModule(): KotlinModule {
        return KotlinModule.Builder().build()
    }
}
