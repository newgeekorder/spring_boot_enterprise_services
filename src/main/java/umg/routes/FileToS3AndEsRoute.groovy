package umg.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.util.GZIPHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory;
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
    private static Logger logger = LoggerFactory.getLogger(FileToS3AndEsRoute.class);

    ElasticHelper eh = new ElasticHelper();
    public static String inputFolder = "file://C:\\Users\\richard\\Downloads\\amcontent_mini";
    String messageType = "FileManifestMessage";
    static String bucket;
    private static String S3_CONFIGURATION;
    private static String ENVIRONMENT = "uat/current/resources/";
    String trackingFolder;
    File tracking;


    FileToS3AndEsRoute() throws Exception {
        eh.connect();

        // load the bucket name
        Properties props = new Properties();
        props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("AwsCredentials.properties"));
        bucket = props.getProperty("cloud.aws.s3.bucket");
        S3_CONFIGURATION =  "aws-s3://" + bucket + "?amazonS3Client=#s3Client";

        // applicaiton properties
        Properties appProps = new Properties();
        appProps.load(ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties"));
        trackingFolder = appProps.getProperty("tracking.folder");
        tracking = new File(trackingFolder + "/run_" + System.currentTimeMillis() + ".txt");

    }


    @Override
    public void configure() throws Exception {
        from(inputFolder +"?flatten=true&recursive=true&move=" + inputFolder + "/processed/").process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                // get the data from the file
                InputStream is = exchange.getIn().getBody(InputStream.class);
                String xmlData = IOUtils.toString(is);

                // create an enterprise message form the file
                EnterpriseMessage em = new EnterpriseMessage();
                em.createEnterpriseMessage(xmlData , messageType);

                // post to S3
                try {
                    is = IOUtils.toInputStream(em.getResult());
                    InputStream isOut = GZIPHelper.compressGzip("UTF-8", is);       // data is compressed
                    exchange.getOut().setBody(isOut);
                    exchange.getOut().setHeader(S3Constants.KEY, ENVIRONMENT + em.getMessageType().toLowerCase() + "/" + em.getObjectId());
                    isOut.close();
                    logger.info("Posting xml to S3 with message " + em.getObjectId())
                } catch (Exception e) {
                    logger.error("Failed to post to s3 for messsage for " + em.getObjectId() + " error file written to ./error folder ");
                    File file = new File("./error/" + em.getObjectId());
                    FileUtils.writeStringToFile(file, em.getResult());
                    return;
                } finally{
                    is.close()
                }
                is.close();
                // post to elastic
                postToElastic(em);
                logger.info("posted message with resouce_id: " + em.getResourceId() + " to elastic at: " + eh.ELASTIC_URL);
            }
        }).to(S3_CONFIGURATION).log("Upload Complete");

    }


    public void postToElastic(EnterpriseMessage em) throws Exception {
        // post to Elastic Search
        EnterpriseMessageJson emj = new EnterpriseMessageJson();
        String xpathMapping = loadXpathRules(messageType);
        String jsonResult = emj.generateJson(em.getResult(), xpathMapping , messageType);
        eh.post(jsonResult,messageType, false );
        tracking.append("Wrote " + jsonResult + " for " + messageType + " \n");
    }


    /**
     * Load the Json xpath rules from the classpath resouces folder
     * @todo move this getting the rules from elastic search
     * @param messageType
     * @return
     * @throws Exception
     */
    public String loadXpathRules(String messageType ) throws Exception {
        InputStream inJson = ClassLoader.getSystemClassLoader().getResourceAsStream("jsonRules/" + messageType +".json");
        if ( inJson == null ){
            log.error("Error opening json xpath rules "  + messageType + ".json");
            throw new Exception();
        } else {
            return IOUtils.toString(inJson);
        }
    }

}
