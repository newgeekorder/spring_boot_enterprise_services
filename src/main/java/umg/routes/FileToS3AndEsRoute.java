package umg.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.util.GZIPHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import sun.nio.ch.IOUtil;
import umg.AWSCredentialsProvider;
import umg.helper.ElasticHelper;
import umg.model.EnterpriseMessageJson;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;


@Component
public class FileToS3AndEsRoute extends RouteBuilder {
    ElasticHelper eh = new ElasticHelper();
    String inputFolder = "file:///media/richard/08378273-c010-466a-b3fb-34e00cd85e93/workspace/workspace_springboot/camelboot/TestData/";
    String messageType = "FileManifestMessage";
    static String bucket;
    private static String S3_CONFIGURATION;


    FileToS3AndEsRoute() throws Exception {
        eh.connect();

        // load the bucket name
        Properties props = new Properties();
        props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("AwsCredentials.properties"));
        bucket = props.getProperty("cloud.aws.s3.bucket");
        S3_CONFIGURATION =  "aws-s3://" + bucket + "?amazonS3Client=#s3Client";
    }


    @Override
    public void configure() throws Exception {
        from(inputFolder).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                InputStream is = exchange.getIn().getBody(InputStream.class);
                List<String> xmlData = IOUtils.readLines(is);
                String cleanData = new String();
                // skip first line
                for (int i = 1; i < xmlData.size(); i++) {
                    cleanData += xmlData.get(i);
                }

                EnterpriseMessage em = new EnterpriseMessage();
                em.createEnterpriseMessage(cleanData, "FileManifestMessage");

                // post to S3
                try {
                    is = IOUtils.toInputStream(em.getResult());
                    InputStream isOut = GZIPHelper.compressGzip("UTF-8", is);       // data is compressed
                    exchange.getOut().setBody(isOut);
                    exchange.getOut().setHeader(S3Constants.KEY, em.getMessageType().toLowerCase() + "/" + em.getObjectId());
                    log.info("Posting xml to S3 with message " + em.getObjectId());
                } catch (Exception e) {
                    log.error("Failed to post to s3 for messsage for " + em.getObjectId() + " error file written to ./error folder ");
                    File file = new File("./error/" + em.getObjectId());
                    FileUtils.writeStringToFile(file, em.getResult());
                    return;
                }

                // post to elastic
                postToElastic(em);
                log.info("posted message with resouce_id: " + em.getResourceId() + " to elastic at: " + eh.ELASTIC_URL);
            }
        }).to(S3_CONFIGURATION).log("Upload Complete");

    }


    public void postToElastic(EnterpriseMessage em) throws Exception {
        // post to Elastic Search
        EnterpriseMessageJson emj = new EnterpriseMessageJson();
        String xpathMapping = loadXpathRules(messageType);
        String jsonResult = emj.generateJson(em.getResult(), xpathMapping , messageType);
        eh.post(jsonResult,messageType, false );
    }


    /**
     * Load the Json xpath rules from the classpath resouces folder
     * @todo move this getting the rules from elastic search
     * @param messageType
     * @return
     * @throws Exception
     */
    public String loadXpathRules(String messageType ) throws Exception {
        InputStream inJson = ClassLoader.getSystemClassLoader().getResourceAsStream(messageType +".json");
        if ( inJson == null ){
            log.error("Error opening json xpath rules "  + messageType + ".json");
            throw new Exception();
        } else {
            return IOUtils.toString(inJson);
        }
    }

}
