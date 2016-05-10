package umg.helper

import com.google.gson.JsonObject
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.JestResult
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Index
import io.searchbox.core.Search
import io.searchbox.core.SearchResult
import org.json.JSONObject
import org.json.JSONTokener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import umg.FileManifest



public class ElasticHelper {
    private static Logger logger = LoggerFactory.getLogger(ElasticHelper.class);

    FileManifest fm = new FileManifest()
    JestClient client
    static String TYPE = "resources"
    static String QA = "http://10.254.176.50:9200"
    static String UAT = "http://usaws19lvapp025:9200"
    public static String ELASTIC_URL;
    static String ELASTIC_USERNAME;
    static String ELASTIC_PASSWORD;
    static String ELASTIC_INDEX;


    ElasticHelper(){
            Properties props = new Properties()
            InputStream propIn = ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties");
            props.load(propIn);
            ELASTIC_URL = props.getProperty("elastic.url");
            ELASTIC_USERNAME = props.getProperty("elastic.username", "");
            ELASTIC_PASSWORD = props.getProperty("elastic.password", "");
            ELASTIC_INDEX = props.getProperty("elastic.index")
    }


    /**
     * connect to elastic search
     */
    public void connect() {
        try {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(
                    new HttpClientConfig.Builder(ELASTIC_URL)
                            .defaultCredentials(ELASTIC_USERNAME, ELASTIC_PASSWORD)
                            .build()
            );
            client = factory.getObject();
            logger.info("Connected to elastic search .. ");
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * Simple Search elastic search
     */
    public String search() {
        String query = '''{
   "query": {
      "match_all": {}
   }
}'''
        Search search = new Search.Builder(query)
                .addIndex(ELASTIC_INDEX)
                .build();

        SearchResult result = client.execute(search);
        def resultSize = result.getTotal()
        JsonObject json = result.getJsonObject()
        return json.toString()
    }

    /**
     * Do bulk update to elastic search
     * @param jsonScript
     */
    public void bulkUpdate(String jsonScript) {

    }


    public void scroll() {
        // 1. Initiate the scroll request
//    Search search = new Search.Builder(searchSourceBuilder.toString())
//            .addIndex("my-index")
//            .addType("my-document")
//            .addSort(new Sort("_doc"))
//            .setParameter(Parameters.SIZE, size)
//            .setParameter(Parameters.SCROLL, "5m")
//            .build();
//    JestResult result = jestClient.execute(search);
//
//    // 2. Get the scroll_id to use in subsequent request
//    String scrollId = result.getJsonObject().get("_scroll_id").getAsString();
//
//    // 3. Issue scroll search requests until you have retrieved all results
//    boolean moreResults = true;
//    while (moreResults) {
//        SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m")
//                .setParameter(Parameters.SIZE, size).build();
//        result = client.execute(scroll);
//        def hits = result.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
//        moreResults = hits.size() > 0;
    }

    /**
     * Single post to elastic search
     * @param jsonScript
     * @param demo
     */
    public void post(String jsonScript, String messageType, boolean demo) {
        JSONObject json = new JSONObject(new JSONTokener(jsonScript))
        String resourceId = json.get("resource_id")

        // check if we are doing a practive run
        if (demo == false) {
            JestResult result = client.execute(new Index.Builder(jsonScript)
                    .index("resources_v21")
                    .type(TYPE.toLowerCase())
                    .id(new String(messageType.toLowerCase() + "_" + resourceId))
                    .build());

            println "posted " + result.responseCode + " error " + result.getErrorMessage()
        } else {
            println "Constructed " + jsonScript
        }
    }


    public String createIndex(File f, String messageType, String jsonRulesPath) {
        fm.init(f, messageType)
        fm.generateDefaultValues()
        return fm.generateJson(new File(jsonRulesPath))
    }


    /**
     *
     * @param filePath
     * @param messageType
     * @param jsonRulesPath
     * @param demo
     */
    public void getData(String filePath, String messageType, String jsonRulesPath, boolean demo) {
        // iterate over files
        int fileCount = 0
        File fmdir = new File(filePath)
        if (fmdir.isDirectory()) {
            println "processing ${messageType} dir.. "
            File[] fmList = fmdir.listFiles()
            fmList.each { dir ->
                if (dir.isDirectory()) {
                    println "looking in " + dir.getName()
                    def list = dir.listFiles()
                    list.each { it ->
                        println "process file " + it.name
                        String json = createIndex(it, messageType, jsonRulesPath) // create the json index
                        post(json, messageType, demo)
                        fileCount++
                        if (fileCount > 100) {
                            System.exit(0)
                        }
                    }
                }
            }
        }
        println "Processing " + fileCount + " files "
    }


    public static void main(String[] args) {
        String dataPath = "E:\\DataLake\\filemanifestgroup"
        String messageType = "FileManifestGroupMessage"
        String jsonRulesPath = "E:\\workspace\\workspace_groovy\\Test\\jsonData\\filemanifestgroup.json"
        boolean demo = false

        ElasticHelper jt = new ElasticHelper()
        jt.connect()
        jt.search()
        jt.getData(dataPath, messageType, jsonRulesPath, demo)

//        jt.getData()
    }

}
