package umg.routes

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class S3Retrive extends RouteBuilder{
    private static String S3_CONFIGURATION;
    String bucket

    S3Retrive(){
        // load the bucket name
        Properties props = new Properties();
        props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("AwsCredentials.properties"));
        bucket = props.getProperty("cloud.aws.s3.bucket");
        S3_CONFIGURATION =  "aws-s3://" + bucket + "?amazonS3Client=#s3Client";
    }

    @Override
    public void configure() throws Exception {

    }
}
