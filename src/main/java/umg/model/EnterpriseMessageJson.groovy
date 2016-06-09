package umg.model

import groovy.json.JsonSlurper
import org.apache.commons.lang.StringEscapeUtils
import org.json.JSONObject
import org.json.JSONTokener
import umg.XmlHelper

import java.text.SimpleDateFormat


/**
 * Created by richard on 10/05/16.
 */
class EnterpriseMessageJson extends XmlHelper {

    JSONObject jsonOut = new JSONObject();
    String messageType



    public void generateEnterpriseDefaults(){
        String value = getXpathItem("//EnterpriseMessage/EnterpriseHeader/Resource/ResourceId")
        jsonOut.put("resource_id", getXpathItem("//EnterpriseMessage/EnterpriseHeader/Resource/ResourceId"))
        jsonOut.put("message_id",  getXpathItem("//EnterpriseMessage/EnterpriseHeader/MessageId").toLowerCase())

        jsonOut.put("message_type", this.messageType.toLowerCase() )
        jsonOut.put("created_utc", getXpathItem("//ResourceUtcDateTimeStamp").toLowerCase())
        jsonOut.put("resource_created_utc", getXpathItem("//ResourceUtcDateTimeStamp").toLowerCase())
        jsonOut.put("thread_id"  , "1d883ad8-4a09-4d05-8735-63d498b7634es")
//        jsonOut.put("source", getXpathItem("//EnterpriseMessage/EnterpriseHeader/MessageSource").toLowerCase())

        // version based on date to mili seconds
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmssSSS")
        Date d = new Date()
        jsonOut.put("version", sdf.format(d))
        println "Default fields " + jsonOut.toString(4)
    }

    public String lowerCase(Object field){
        if ( field == null ){
            return "";
         } else {
            return field.toLowerCase();
        }
    }

    /**
     * Load the json document and generate the key valye pairs based on the rules there
     * @param xpathMapping
     */
    public String  generateJson(String xmlData, String xpathMappingData, String messageType) {
        this.messageType = messageType;

        parse(xmlData);
        generateEnterpriseDefaults();
        try {
            JsonSlurper json = new JsonSlurper()

            def root = json.parseText(xpathMappingData)
            println "Processing xpath rules " + xpathMappingData + " with " + root.xpathmapping.size() + " rules "
            root.xpathmapping.each { key, value ->
                if ( key.equals("identifiers")){
                    JSONObject idjson = new JSONObject()
                    System.err.println("Processing identifiers ")
                    root.xpathmapping.identifiers.each{ idkey, idvalue ->
                        ArrayList lists = getXpathList(idvalue)
                        if ( lists.size() > 1 ){
                            idjson.put(idkey, lists )
                        } else if ( lists.size() == 1 ){
                            idjson.put(idkey ,lists.get(0).trim())
                        }
                    }x
                    jsonOut.put("identifiers", idjson)
                }
                else if (value.trim().startsWith("concat-all")) {
                    jsonOut.put(key, concatAll(value))
                }
                else if ( ! value.startsWith("/")){  // not an expath we are adding a constant
                    jsonOut.put(key, value)
                }
                else {
                    ArrayList lists = getXpathList(value)
                    if ( lists.size() > 1 ){
                        jsonOut.put(key, lists )
                    } else if ( lists.size() == 1 ){
                        jsonOut.put(key,lists.get(0).trim())
                    }
                }
            }
            jsonOut.put("raw_message", StringEscapeUtils.escapeJavaScript(getDocumentToSingleLine()))
//            println "Result json for $messageType \n " + jsonOut.toString(4)
            return jsonOut
        } catch (Exception e) {
            println "error extracting xpath results "
            e.printStackTrace()
        }

    }

    public ArrayList concatAll(String valueStr) {
        String[] xpathValues = (valueStr =~ /concat-all\((.*),(.*)\)/)[0]
//        println "extracted keyXpath " + xpathValues[1]
//        println "extracted value  " + xpathValues[2]

        def keys = getXpathList(xpathValues[1])
        def values = getXpathList(xpathValues[2])
        ArrayList result = new ArrayList()
        int i = 0;
        keys.each {
            result.add(it + ":" + values[i])
        }
        println "concatAll got result " + result
        return result
    }

    public String  readElastic(){
//        URI uri = new URI("http://someserver/data.json");
        String testData = "/home/richard/workspace/workspace_spring/StaticGroovy/searchData/physicalasset.json"
        JSONTokener tokener = new JSONTokener(new File(testData).text )
        JSONObject root = new JSONObject(tokener);
        String xml = root.getJSONObject("_source").getString("raw_message")
        parse(xml)

    }

    public static void main(String[] args) {
        EnterpriseMessageJson fm = new EnterpriseMessageJson()
        File data = new File("/media/richard/08378273-c010-466a-b3fb-34e00cd85e93/workspace/workspace_springboot/camelboot/xmlData/EnterpriseMessage.xml");
        File xpathMapping = new File("/media/richard/08378273-c010-466a-b3fb-34e00cd85e93/workspace/workspace_springboot/camelboot/jsonRules/fileManifest.json")
        println fm.generateJson(data.text, xpathMapping.text, "filemanifest")



    }

}
