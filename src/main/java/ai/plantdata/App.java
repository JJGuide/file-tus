package ai.plantdata;

import ai.plantdata.controller.AppProperties;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin
@EnableScheduling
public class App implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);



    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("=======================================");
        LOG.info("=======================================");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }
    
    @Bean
    public TusFileUploadService tusFileUploadService(AppProperties appProperties) {

        return new TusFileUploadService().withUploadURI("/api/upload");
    }

}
