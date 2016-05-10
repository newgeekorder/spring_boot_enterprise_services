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
<EnterpriseMessage>
    <EnterpriseHeader>
        <MessageId>${messageId}</MessageId>
        <ThreadId>42143eeb-1387-4f35-af74-8f4d763be64f</ThreadId>
        <CorrelationId></CorrelationId>
        <CreatedUtc>${createdUtc}</CreatedUtc>
        <Source>userSource</Source>
        <Action>Publish</Action>
        <Communication>Asynchronous</Communication>
        <Resource>
            <ResourceName>${messageType}</ResourceName>
            <ResourceId>${resourceId}</ResourceId>
            <ResourceCreatedUtc>${resourceCreatedUtc}</ResourceCreatedUtc>
        </Resource>
    </EnterpriseHeader>
    <MessageBody>
        ${messageBody}
    </MessageBody>
</EnterpriseMessage>'''


    public String getResult(){
        return result;
    }

    public String createEnterpriseMessage(String messageBody, String messageType) {
        parse(messageBody)

        this.messageType = messageType;
        messageId = getXpathItem("//MessageHeader/MessageId")
        createdUtc = getXpathItem("//MessageHeader/ResourceUtcDateTimeStamp")
        messageUtc = getXpathItem("//MessageHeader/MessageUtcDateTimeStamp")
        source = getXpathItem("//MessageHeader/MessageSource")
        resourceId = getXpathItem("//MessageHeader/ResourceId")
        resourceCreatedUtc = getXpathItem("//MessageHeader/ResourceUtcDateTimeStamp")

        // s3 object id is messageid and a stripped createdUTC
        objectId = messageId + "_" + createdUtc.replaceAll("Z|T|\\.|:|-","")
        Map data = ["messageId": messageId, "createdUtc": createdUtc, "messageUtc": messageUtc, "source": source, "resourceId": resourceId, "resourceCreatedUtc"
                               : resourceCreatedUtc, "messageType": messageType]
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


