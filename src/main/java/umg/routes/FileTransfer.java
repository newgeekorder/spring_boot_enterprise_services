package umg.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileTransfer  extends RouteBuilder {
    String dataOut;
    String dataIn;
    static int i = 0;

    @Override
    public void configure() throws Exception {
        long startTime = System.nanoTime();

        from("file://C:\\DataOut").log(".").to("file://C:\\DataIn");
        long endTime = System.nanoTime();
        System.out.println("Processed  " + i + "  files in " + (endTime - startTime)/1000  + " milli-seconds ");

    }
}
