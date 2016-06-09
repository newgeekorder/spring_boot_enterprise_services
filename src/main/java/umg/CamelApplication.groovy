package umg;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.Main;
import org.apache.camel.spring.SpringCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext
import umg.routes.FileToMuleRoute
import umg.routes.FileToS3AndEsRoute
import umg.routes.FileTransfer
import umg.routes.QueryMule;
import umg.routes.RestRoute
import umg.routes.TestRoute;



/**
 * Entry point for creating micro services using Spring Boot and Apache Camel
 *
 */
@Configuration
@ComponentScan
public class CamelApplication {
    private static Logger logger = LoggerFactory.getLogger(CamelApplication.class);

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private TestRoute testRoute;

    @Autowired
    private RestRoute restRoute;

    @Autowired
    private FileTransfer fileTranfer;

    @Autowired
    private FileToS3AndEsRoute fileToS3AndEsRoute;

    @Autowired
    private FileToMuleRoute fileToMuleRoute;

    @Autowired
    private QueryMule queryMule;

    public static void main( String[] args ) throws Exception {
        ApplicationContext springContext = SpringApplication.run(CamelApplication.class, args);
        springContext.getBean(Main.class).run();
        logger.info("Apache Camel with Spring Boot running.");
    }

    @Bean
    public CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = new SpringCamelContext();

        //Add your routes here.
//        camelContext.addRoutes(testRoute);
//        camelContext.addRoutes(fileToS3AndEsRoute)
//        camelContext.addRoutes(fileTranfer)
//        camelContext.addRoutes(restRoute);
        camelContext.addRoutes(queryMule);
        camelContext.addRoutes(fileToMuleRoute);
        return camelContext;
    }

    /**
     * Enable running Camel in the background as a standalone application.
     */
    @Bean
    public Main camelMain() throws Exception {
        Main main = new Main();
        main.enableHangupSupport();
        main.setApplicationContext((AbstractApplicationContext) springContext);
        return main;
    }
}
