package umg;

import groovy.json.JsonSlurper
import org.apache.commons.lang.StringEscapeUtils
import org.json.JSONObject
import org.json.JSONTokener

import java.text.SimpleDateFormat

/**
 * Created by richard on 26/04/16.
 */
class FileManifest extends XmlHelper {
    JSONObject jsonOut = new JSONObject();
    String messageType

    public FileManifest() {

    }

    public void init(File xmFile, String messageType ){
        this.messageType = messageType
        parse(xmFile.text)
    }

    public void init(String xmlFile, String messageType) {
        this.messageType = messageType
        File xml = new File(xmlFile)
        parse(xml.text)

    }

    public void generateEnterpriseDefaults(){
        jsonOut.put("resource_id", getXpathItem("//EnterpriseMessage/EnterpriseHeader/ResourceId").toLowerCase())
        jsonOut.put("message_id", getXpathItem("//EnterpriseMessage/EnterpriseHeader/MessageId").toLowerCase())
        jsonOut.put("message_type", "${messageType}".toLowerCase())
        jsonOut.put("created_utc", getXpathItem("//EnterpriseMessage/EnterpriseHeader/ResourceUtcDateTimeStamp").toLowerCase())
        jsonOut.put("resource_created_utc", getXpathItem("//EnterpriseMessage/EnterpriseHeader/ResourceUtcDateTimeStamp").toLowerCase())
        jsonOut.put("thread_id"  , "1d883ad8-4a09-4d05-8735-63d498b7634es")
//        jsonOut.put("source", getXpathItem("//EnterpriseMessage/EnterpriseHeader/MessageSource").toLowerCase())

        // version based on date to mili seconds
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmssSSS")
        Date d = new Date()
        jsonOut.put("version", sdf.format(d))
        println "Default fields " + jsonOut.toString(4)
    }

    public void generateDefaultValues() {
        jsonOut.put("resource_id", getXpathItem("//${messageType}/MessageHeader/ResourceId").toLowerCase())
        jsonOut.put("message_id", getXpathItem("//${messageType}/MessageHeader/MessageId").toLowerCase())
        jsonOut.put("message_type", "${messageType}".toLowerCase())
        jsonOut.put("created_utc", getXpathItem("//${messageType}/MessageHeader/ResourceUtcDateTimeStamp").toLowerCase())
        jsonOut.put("resource_created_utc", getXpathItem("//${messageType}/MessageHeader/ResourceUtcDateTimeStamp").toLowerCase())
//        jsonOut.put("thread_id"  , getXpathItem("//FileManifestMessage/MessageHeader//ThreadId/")
        jsonOut.put("source", getXpathItem("//${messageType}/MessageHeader/MessageSource").toLowerCase())

        // version based on date to mili seconds
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddhhmmssSSS")
        Date d = new Date()
        jsonOut.put("version", sdf.format(d))
        println "Default fields " + jsonOut.toString(4)

    }

    /**
     * Load the json document and generate the key valye pairs based on the rules there
     * @param xpathMapping
     */
    public String  generateJson(File xpathMapping) {
        try {
            JsonSlurper json = new JsonSlurper()
            def root = json.parseText(xpathMapping.text)
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
                    }
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
            jsonOut.put("raw_message", StringEscapeUtils.unescapeJavaScript(getDocumentToSingleLine()))
//            println "Result json for $messageType \n " + jsonOut.toString(4)
            return jsonOut
        } catch (Exception e) {
            println "error extracting xpath results "
            e.printStackTrace()
        }

    }

    public String concatAll(String valueStr) {
        String[] xpathValues = (valueStr =~ /concat-all\((.*),(.*)\)/)[0]
//        println "extracted keyXpath " + xpathValues[1]
//        println "extracted value  " + xpathValues[2]

        def keys = getXpathList(xpathValues[1])
        def values = getXpathList(xpathValues[2])
        String result = "["
        int i = 0;
        keys.each {
            result += it + ":" + values[i]
            i++
            if (keys.size() > i) {
                result += ","
            }

        }
        result += "]"
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
        FileManifest fm = new FileManifest()
//        fm.init("/home/richard/workspace/workspace_spring/StaticGroovy/data/ffa249fa-d599-2827-41f7-7f30c2b516e5_20160403201027000.xml", "FileManifestMessage")
//        fm.generateDefaultValues()
//        fm.generateJson(new File("/home/richard/workspace/workspace_spring/StaticGroovy/jsonData/FileManifestMessage.json"))
//
//        // Now do FileManifest Group
        fm = new FileManifest()
        fm.init("/home/richard/workspace/workspace_spring/StaticGroovy/data/11c5692d-7f90-cd82-b44a-2c7f942410f1_20160331000000000.xml", "FileManifestGroupMessage")
        fm.generateDefaultValues()
        fm.generateJson(new File("/home/richard/workspace/workspace_spring/StaticGroovy/jsonData/filemanifestgroup.json"))
//
//        // Now do Digital Assement for Aspen
//        fm = new FileManifest()
//        fm.init("/home/richard/workspace/workspace_spring/StaticGroovy/data/20160426173033650.xml", "DigitalAssetMessage")
//        fm.generateJson(new File("/home/richard/workspace/workspace_spring/StaticGroovy/jsonData/digitalassetmessage.json"))

//        fm.readElastic()
////        fm.generateDefaultValues()
//        fm.messageType = "PhysicalAssetMessage"
//        fm.generateJson(new File("/home/richard/workspace/workspace_spring/StaticGroovy/jsonData/physicalassetmessage.json"))

    }

}
