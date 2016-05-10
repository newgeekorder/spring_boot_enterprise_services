package umg;

import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Producer {

    @Autowired
    private StaticCredentialsProvider staticCredentialsProvider;

    @Bean(name = "s3Client")
    public AmazonS3 s3Client(){
        return new AmazonS3Client(staticCredentialsProvider);
    }

}