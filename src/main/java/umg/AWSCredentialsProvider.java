package umg;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.Properties;

@Configuration
public class AWSCredentialsProvider{
//        static String accessKeyId = "AKIAIQMTFWQ3UGJARK3A";
//        static String secretKey= "cEt4VdvVXpVQm8lEu3wAapS7aNWj+
    public static String bucket;
    static String accessKeyId;
    static String secretKey;


    @Bean
    public StaticCredentialsProvider staticCredentialsProvider() {
        try {
            Properties props = new Properties();
            props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("AwsCredentials.properties"));
            accessKeyId = props.getProperty("aws.accessKeyId");
            secretKey = props.getProperty("aws.secretKey");
            bucket = props.getProperty("bucket");
            return new StaticCredentialsProvider(new BasicAWSCredentials( accessKeyId ,  secretKey ));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}