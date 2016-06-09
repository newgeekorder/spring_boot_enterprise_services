package umg

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.spring.Main
import org.apache.camel.spring.SpringCamelContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.AbstractApplicationContext
import umg.routes.*

/**
 * Entry point for creating micro services using Spring Boot and Apache Camel
 *
 */
@Configuration
@ComponentScan
public class CamelTestApplication {
    private static Logger logger = LoggerFactory.getLogger(CamelTestApplication.class);

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

    CamelContext camelContext;



    @Bean
    public CamelContext createCamelContext() throws Exception {
         camelContext = new SpringCamelContext();

        //Add your routes here.
//        camelContext.addRoutes(testRoute);
//        camelContext.addRoutes(fileToS3AndEsRoute)
//        camelContext.addRoutes(fileTranfer)
//        camelContext.addRoutes(restRoute);
//        camelContext.addRoutes(fileToMuleRoute);
        camelContext.addRoutes(queryMule)
        return camelContext;
    }


    public void callQueryMule(){
        ProducerTemplate producerTemplate =  camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", "67e45b12-6988-5a10-70e3-e05e4334a97b");
    }


    /**
     * Enable running Camel in the background as a standalone application.
     */
    @Bean
    public Main camelMain() throws Exception {
        Main main = new Main();
        main.enableHangupSupport();
        main.setApplicationContext((AbstractApplicationContext) springContext);
        callQueryMule()
        return main;
    }


    public static void main( String[] args ) throws Exception {
        ApplicationContext springContext = SpringApplication.run(CamelTestApplication.class, args);
        springContext.getBean(Main.class).run();
        logger.info("Apache Camel with Spring Boot running.");
        CamelTestApplication cta = new CamelTestApplication()
        cta.callQueryMule()
    }
}
