package umg.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("timer://myTimer?period=5000&delay=1000").to("log:se.ithuset.timer?level=INFO");

//        rest("/say").get("/hello").to("direct:hello").get("/bye").consumes("application/json").to("direct:bye").post("/bye").to("mock:update");
//        from("direct:hello").transform().constant("Hello World");
//        from("direct:bye").transform().constant("Bye World");
    }
}


