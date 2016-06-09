package umg


import com.sun.org.apache.xerces.internal.parsers.DOMParser
import org.apache.camel.Produce
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.XMLUnit
import org.json.JSONObject
import org.json.JSONArray
import org.junit.Test
import org.w3c.dom.Document
import umg.helper.ElasticHelper
import umg.model.EnterpriseMessageJson
import umg.routes.EnterpriseMessage
import umg.routes.FileToS3AndEsRoute

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Created by richard on 24/05/2016.
 */

class TestFileTransfer  extends CamelTestSupport{

    @Produce(uri = "FileToS3AndEsRoute.inputFolder +\"?flatten=true&recursive=true&move=processed")
    protected ProducerTemplate mockStart;

//    @Test
    public void testXmlStripping(){
        File testData = new File("./xmlData/ffa249fa-d599-2827-41f7-7f30c2b516e5_20160403201027000.xml")
        EnterpriseMessage em = new EnterpriseMessage()
        println em.stripLeadingXml(testData.text)
    }

//    @Test
    public void testEntepriseMessage(){

        //        String cleanData  =  xmlData[1..xmlData.size-1].join("")
        File testData = new File("./xmlData/ffa249fa-d599-2827-41f7-7f30c2b516e5_20160403201027000.xml")
        EnterpriseMessage em = new EnterpriseMessage()
        String result = em.createEnterpriseMessage(testData.text , "FileManifestMessage")
        File expectedResult = new File("./xmlData/ExpectedTestRestults/enterpriseMessage.xml")
        assertEquals(true, xmlTheSame(result.trim(), expectedResult.text ))
    }

//    @Test
    public void testJsonCreation(){
        EnterpriseMessageJson fm = new EnterpriseMessageJson()
        File data = new File("./xmlData/EnterpriseMessage.xml");

        File xpathMapping = new File("./src/main/resources/jsonRules/FileManifestMessage.json")
        String jsonResult = fm.generateJson(data.text, xpathMapping.text, "filemanifest")
        jsonResult = removeVersion(jsonResult)

        File expectedJson = new File("./xmlData/ExpectedTestRestults/fileManifestMessage.json")
        String expectedJsonStr = removeVersion(expectedJson.text)

        assertEquals(jsonResult.trim(), expectedJsonStr.trim())

    }

//    @Test
    public void testElasticPosting(){
        EnterpriseMessageJson fm = new EnterpriseMessageJson()
        File data = new File("./xmlData/EnterpriseMessage.xml");

        File xpathMapping = new File("./src/main/resources/jsonRules/FileManifestMessage.json")
        String jsonResult = fm.generateJson(data.text, xpathMapping.text, "filemanifest")

        // post to elastic
        ElasticHelper eh = new ElasticHelper();
        eh.connect()
        eh.post(jsonResult, "FileManifestMessage", false)
        //sleep for 2 second to ensure indexed and search able .. an alternate would be to loop x times
        System.sleep(2000)
        // validate the queries
        JSONObject json = new JSONObject(jsonResult);
        String query = "resource_id : " + json.get("resource_id")
        String searchResult  = eh.searchQueryString( query );
        String extractedXml =  extractRawXml(searchResult)
//        assertEquals(true, xmlTheSame(data.text, extractedXml))

        // delete from elastic
        int responseCode = eh.deleteQueryString( query )
        assertEquals(200, responseCode);
    }

    @Test
    public void testPostingtoS3(){
        // trigger file folder
        File data = new File("./xmlData/EnterpriseMessage.xml");
        File inputDir = Properties.getClassLoader().getResourceAsStream()
        FileUtils.copyFile(data, )

        // post message to S3

        // retrive the message

    }

    public void testExtractingFromS3(){

    }

    //------------------------------------------
    //   Helper Methods
    //------------------------------------------

    /**
     * Helper method to compare xml documents
     */
    public boolean xmlTheSame(String xml1, String xml2 ){
        XMLUnit.setIgnoreWhitespace(true)
        Diff diff = new Diff(xml1, xml2);

        DetailedDiff detDiff = new DetailedDiff(diff);
        List differences = detDiff.getAllDifferences();
        for (Object object : differences) {
            Difference difference = (Difference)object;
            System.out.println("***********************");
            System.out.println(difference);
            System.out.println("***********************");
        }
        return  diff.identical();
    }

/**
 * remove the contents of the verison tag to allow comparison of json documents
 */
    public String removeVersion(String data ){
        JSONObject json = new JSONObject(data)
        json.put("version", "");
        return json.toString()
    }

    public String extractRawXml(String result ){
        JSONObject json = new JSONObject(result)
        JSONArray array = json.getJSONObject("hits").getJSONArray("hits")
        println "Got " + array.size() + " hits "
        if ( array.size() > 0 ) {
            String raw_message = array.getJSONObject(0).getJSONObject("_source").getString("raw_message")

//                .getJSONObject("_source").get("raw_message")
            return raw_message
        } else {
            return ""
        }
    }

}
