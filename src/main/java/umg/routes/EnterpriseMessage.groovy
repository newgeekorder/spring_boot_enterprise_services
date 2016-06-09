package umg.routes

import groovy.text.SimpleTemplateEngine;
import groovy.text.XmlTemplateEngine
import org.apache.commons.io.IOUtils
import umg.XmlHelper

class EnterpriseMessage extends XmlHelper {
    String messageId;
    String threadId;
    String correlationId;
    String createdUtc;
    String messageUtc;
    String source;
    String messageType;
    String resourceId;
    String resourceCreatedUtc;
    String messageBody;
    String objectId;
    SimpleTemplateEngine engine = new groovy.text.SimpleTemplateEngine()
    String result;


    String enterpriseHeader = '''<?xml version="1.0" encoding="UTF-8"?>
<ns1:EnterpriseMessage xmlns:ns2="http://es.umgi.com/XMLSchemas/CustomProps"
                       xmlns:ns1="http://schemas.umusic.com/enterprise/services/2014/05">
    <ns1:EnterpriseHeader>
        <ns1:MessageId>${messageId}</ns1:MessageId>
        <ns1:ThreadId>42143eeb-1387-4f35-af74-8f4d763be64f</ns1:ThreadId>
        <ns1:CreatedUtc>${createdUtc}</ns1:CreatedUtc>
        <ns1:Source>DataLake</ns1:Source>
        <ns1:Action>Publish</ns1:Action>
        <ns1:Communication>Asynchronous</ns1:Communication>
        <ns1:Resource>
            <ns1:ResourceName>filemanifestmessage</ns1:ResourceName>
            <ns1:ResourceId>${resourceId}</ns1:ResourceId>
            <ns1:ResourceCreatedUtc>${resourceCreatedUtc}</ns1:ResourceCreatedUtc>
        </ns1:Resource>
    </ns1:EnterpriseHeader>
    <ns1:MessageBody>
        ${messageBody}
    </ns1:MessageBody>
</ns1:EnterpriseMessage>'''


    public String getResult(){
        return result;
    }

    // strip the leading <?xml version
    public String stripLeadingXml(String message){
        if ( message.startsWith("<?xml")){
            message.replaceFirst("<\\?xml([\\s\\S]*)\\?>", "")
        }
    }

    public String createEnterpriseMessage(String messageBody, String messageType) {
        messageBody = stripLeadingXml(messageBody);
        parse(messageBody)

        this.messageType = messageType.toLowerCase();
        messageId = getXpathItem("//MessageHeader/MessageId")
        createdUtc = getXpathItem("//MessageHeader/ResourceUtcDateTimeStamp")
        messageUtc = getXpathItem("//MessageHeader/MessageUtcDateTimeStamp")
        source = getXpathItem("//MessageHeader/MessageSource")
        resourceId = getXpathItem("//MessageHeader/ResourceId")
        resourceCreatedUtc = getXpathItem("//MessageHeader/ResourceUtcDateTimeStamp")

        // s3 object id is messageid and a stripped createdUTC
        objectId = messageId + "_" + createdUtc.replaceAll("Z|T|\\.|:|-","")
        Map data = ["messageId": messageId, "createdUtc": createdUtc, "messageUtc": messageUtc, "source": source, "resourceId": resourceId, "resourceCreatedUtc"
                               : resourceCreatedUtc, "messageType": messageType.toLowerCase()]
        //parse the message body and create the enterprise message
        data.put("messageBody", messageBody)

        def template = engine.createTemplate(enterpriseHeader).make(data)
        result =  template.toString()
        return result;

    }


    public static void main(String[] args) {
//        File testData = new File("/media/richard/08378273-c010-466a-b3fb-34e00cd85e93/workspace/workspace_springboot/camelboot/xmlData/ffa249fa-d599-2827-41f7-7f30c2b516e5_20160403201027000.xml")
//        List<String> xmlData = testData.readLines()
//        String cleanData  =  xmlData[1..xmlData.size-1].join("")
        EnterpriseMessage em = new EnterpriseMessage()
//        println(em.createEnterpriseMessage(cleanData , "FileManifestMessage"))


    }
}


