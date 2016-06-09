package umg.routes

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.aws.s3.S3Constants
import org.apache.camel.component.file.GenericFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients 
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.protocol.HttpContext
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import org.springframework.stereotype.Component

import javax.net.ssl.HostnameVerifier

@Component
class FileToMuleRoute extends RouteBuilder {
    String messageType = "FileManifestMessage";
    public static String inputFolder = "file://C:\\Users\\richard\\Downloads\\amcontent_mini2";
    public static String outputFolder = "C:\\Users\\richard\\Downloads\\amcontent_mini2_processed";
//    static String QA = "http://ESESBQA.global.umusic.ext:46003/rest-facade"
//    static String QA = "https://ESESBQA.global.umusic.ext:46003/rest-facade"
    static String UAT = "https://aws19lvapp001.umusic.net:47003/rest-facade"
    String trackingFolder;
    File tracking;

    FileToMuleRoute(){
        // applicaiton properties
        Properties appProps = new Properties();
        appProps.load(ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties"));
        trackingFolder = appProps.getProperty("tracking.folder");
        tracking = new File(trackingFolder + "/run_" + System.currentTimeMillis() + ".txt");
        new File(outputFolder).mkdirs() // created the processed folder
    }

    @Override
    public void configure() throws Exception {
        long startTime = System.nanoTime();

        from(inputFolder + "?flatten=true&recursive=true&noop=true").process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                File dataFile  = exchange.getIn().getBody(File.class);
                String xmlData = FileUtils.readFileToString (dataFile )
                // get the data from the file

//                InputStream is = exchange.getIn().getBody(InputStream.class);
//                String xmlData = IOUtils.toString(is);

                // create an enterprise message form the file
                EnterpriseMessage em = new EnterpriseMessage();
                String enterpriseMsg  = em.createEnterpriseMessage(xmlData, messageType);
                System.err.println "Sending EM " + enterpriseMsg
                exchange.getOut().setBody(enterpriseMsg);
                System.err.println(" sending "  + em.resourceId )
                tracking.append("Wrote, " + em.resourceId + " \n");
                exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");

                postMessage(enterpriseMsg)
//                is.close();
                File outDir = new  File(outputFolder);
                dataFile.renameTo(outputFolder + "/"  + dataFile.getName())
                println "moved file to " + outDir
            }
        });

//                .to(QA + "/publish-v2/message").process(new Processor() {
//            public void process(Exchange exchange) throws Exception {
//                Message out = exchange.getOut();
//                int responseCode = out.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
//                println "got response code " + responseCode
//            }
//        })

    }

    public void postMessage(String message){
        HttpContext context = HttpClientContext.create();
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        HostnameVerifier hostnameVerifier = new NoopHostnameVerifier()
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), hostnameVerifier);
        CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();


//        CloseableHttpClient client = HttpClients.custom()
//                .setConnectionManager(cm)
//                .build();

        // create a post body
        HttpEntity entity = new ByteArrayEntity(message.getBytes("UTF-8"));

        HttpPost post = new HttpPost(UAT + "/publish-v2/message" ); // example url
        System.err.println ("Published message to " + UAT + "/publish-v2/message" );
        post.setEntity(entity)
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + "dXNlcjpwYXNzd29yZA==" );

        //         client.execute(post, context)
        HttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        println "status code"  + response.getStatusLine().getStatusCode()
        println "got result " + result
    }
}
