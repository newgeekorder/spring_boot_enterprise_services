package umg.routes

import com.google.gson.JsonObject
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HttpContext
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import com.google.gson.JsonArray
import org.json.JSONArray
import org.springframework.stereotype.Component
import umg.helper.ElasticHelper

import javax.net.ssl.HostnameVerifier


@Component
class QueryMule extends RouteBuilder {
    ArrayList keys = ["resource_id",
                      "dc\\:subject",
                      "message_id",
                      "message_type",
                      "source",
                      "classification", "version", "dc\\:identifier"]

    Map queryAttributes = ["resource_id"   : "",
                           "dc:subject"    : "",
                           "message_id"    : "",
                           "message_type"  : "",
                           "source"        : "",
                           "classification": ""]
    static String UAT = "https://aws19lvapp001.umusic.net:47003/rest-facade"

    String dataOut;
    String dataIn;
    static int i = 0;
    String rest_facade_url = "https://internal-mule-uat-rest-fac-int-elb-1173435248.us-east-1.elb.amazonaws.com/rest-facade"


    ElasticHelper elasticHelper

    QueryMule() {
        elasticHelper = new ElasticHelper()
        elasticHelper.connect();
    }

    @Override
    public void configure() throws Exception {
        long startTime = System.nanoTime(); https://camel.apache.org/rest.html

        from("jetty:http://localhost:8078/query").process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                String body = exchange.getIn().getBody(String.class)
                queryAttributes.resource_id = exchange.getIn().getHeader("resource_id")
                if (queryAttributes.resource_id != null && queryAttributes.resource_id.length() > 0) {
                    exchange.getOut().setBody(testElastic(queryAttributes.resource_id))
                } else {
                    exchange.getOut().setBody("Error: Expected a resource_id=123456 parameter");
                }
            }
        })
        long endTime = System.nanoTime();
        System.out.println("Processed  " + i + "  files in " + (endTime - startTime) / 1000 + " milli-seconds ");
    }

    public String testElastic(String resource_id) {
        String query = "resource_id : " + resource_id
        println "running query " + query
        JsonObject json = elasticHelper.searchQueryStringJson(query)
        if (json == null) {
            return "no result found in elastic for " + resource_id
        } else {
            com.google.gson.JsonArray hits = json.getAsJsonObject("hits").getAsJsonArray("hits")
            println("Found " + hits.size() + " matches in query")
            if (hits.size() == 0) {
                return "No elastic search hits found for resource_id " + resource_id
            } else {
                JsonObject source = hits.get(0)
                JsonObject realSource = source.get("_source")
                queryAttributes.each{ key, value ->
                    queryAttributes.put(key , realSource.get(key))
                }
                testAllMuleAttributes()

            }

        }
    }

    public String  testAllMuleAttributes() {
        String result = ""
        queryAttributes.each{ key, value ->
            String query = key + " : " +  value
            String muleQuery = createMuleQuyery(query)
            System.err.println "running " + muleQuery
            searchMule( muleQuery  )
        }
        return result
    }

    public String createMuleQuyery ( String searchQuery ){
         String muleQueryStr = """<es:EnterpriseSearch xmlns:es="http://schemas.umusic.com/enterprise/services/2014/05">
  <es:Offset>0</es:Offset>
  <es:Rows>10</es:Rows>
  <es:Result>Master</es:Result>
  <es:SearchQuery> ${searchQuery}</es:SearchQuery>
</es:EnterpriseSearch>
"""
        return muleQueryStr
    }


    public HttpResponse searchMule(String message) {
        String url = UAT + "/search-v2/"
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

        HttpPost post = new HttpPost( url ); // example url
        System.err.println("Published message to " + url );
        post.setEntity(entity)
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + "dXNlcjpwYXNzd29yZA==");

        //         client.execute(post, context)
        HttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        println "status code" + response.getStatusLine().getStatusCode()
        println "got result " + result
        return response
    }
}
