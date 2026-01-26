package itmo.ivank.restadapter.client;

import itmo.ivank.restadapter.dto.*;
import itmo.ivank.restadapter.exception.SoapClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * SOAP Client for calling the SOAP Caller Service.
 */
@Component
@RefreshScope
public class SoapCallerClient {

    @Value("${soap-caller.url}")
    private String soapServiceUrl;

    private static final String NAMESPACE_URI = "http://soap.ivank.itmo/caller";
    private static final String NAMESPACE_PREFIX = "ns";

    @PostConstruct
    public void init() {
        System.out.println("SOAP Caller Client initialized with URL: " + soapServiceUrl);
    }

    public Acquiring acquire(Long acquirerId, Long acquiredId) {
        try {
            // Create SOAP message
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();

            // SOAP Envelope
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URI);

            // SOAP Body
            SOAPBody soapBody = envelope.getBody();
            SOAPElement acquireElement = soapBody.addChildElement("acquire", NAMESPACE_PREFIX);
            
            // Child elements must NOT have namespace prefix (elementFormDefault="unqualified")
            SOAPElement acquirerIdElement = acquireElement.addChildElement("acquirerId");
            acquirerIdElement.addTextNode(String.valueOf(acquirerId));
            
            SOAPElement acquiredIdElement = acquireElement.addChildElement("acquiredId");
            acquiredIdElement.addTextNode(String.valueOf(acquiredId));

            soapMessage.saveChanges();

            // Send message and get response
            SOAPMessage response = sendSoapMessage(soapMessage);
            
            return parseAcquiringResponse(response);
        } catch (SoapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SoapClientException(500, "Error calling SOAP service: " + e.getMessage());
        }
    }

    public FireResponse fireAllOrgEmployees(Long organizationId) {
        try {
            // Create SOAP message
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();

            // SOAP Envelope
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URI);

            // SOAP Body
            SOAPBody soapBody = envelope.getBody();
            SOAPElement fireElement = soapBody.addChildElement("fireAllOrgEmployees", NAMESPACE_PREFIX);
            
            // Child elements must NOT have namespace prefix (elementFormDefault="unqualified")
            SOAPElement orgIdElement = fireElement.addChildElement("organizationId");
            orgIdElement.addTextNode(String.valueOf(organizationId));

            soapMessage.saveChanges();

            // Send message and get response
            SOAPMessage response = sendSoapMessage(soapMessage);
            
            return parseFireResponse(response);
        } catch (SoapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SoapClientException(500, "Error calling SOAP service: " + e.getMessage());
        }
    }

    private SOAPMessage sendSoapMessage(SOAPMessage soapMessage) throws Exception {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        
        try {
            URL endpoint = new URL(soapServiceUrl);
            SOAPMessage response = soapConnection.call(soapMessage, endpoint);
            
            // Check for SOAP Fault
            SOAPBody responseBody = response.getSOAPBody();
            if (responseBody.hasFault()) {
                SOAPFault fault = responseBody.getFault();
                String faultMessage = fault.getFaultString();
                
                // Try to extract error code from fault detail (CallerServiceFault)
                int errorCode = 500; // default
                Detail detail = fault.getDetail();
                if (detail != null) {
                    Iterator<?> detailEntries = detail.getDetailEntries();
                    while (detailEntries.hasNext()) {
                        Object entry = detailEntries.next();
                        if (entry instanceof javax.xml.soap.SOAPElement) {
                            javax.xml.soap.SOAPElement elem = (javax.xml.soap.SOAPElement) entry;
                            // Look for <code> element inside CallerServiceFault
                            NodeList codeNodes = elem.getElementsByTagName("code");
                            if (codeNodes.getLength() > 0) {
                                String codeStr = codeNodes.item(0).getTextContent();
                                try {
                                    errorCode = Integer.parseInt(codeStr);
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
                
                throw new SoapClientException(errorCode, "SOAP Fault: " + faultMessage);
            }
            
            return response;
        } finally {
            soapConnection.close();
        }
    }

    private Acquiring parseAcquiringResponse(SOAPMessage response) throws Exception {
        SOAPBody body = response.getSOAPBody();
        
        // Get the acquiring element from response
        NodeList acquiringNodes = body.getElementsByTagNameNS("*", "acquiring");
        if (acquiringNodes.getLength() == 0) {
            acquiringNodes = body.getElementsByTagName("acquiring");
        }
        
        if (acquiringNodes.getLength() == 0) {
            // Try to get from response wrapper
            Node firstChild = body.getFirstChild();
            if (firstChild != null) {
                NodeList children = firstChild.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getLocalName() != null && 
                        children.item(i).getLocalName().equals("acquiring")) {
                        return parseAcquiringElement(children.item(i));
                    }
                }
            }
            throw new SoapClientException(500, "Invalid SOAP response: missing acquiring element");
        }
        
        return parseAcquiringElement(acquiringNodes.item(0));
    }

    private Acquiring parseAcquiringElement(Node acquiringNode) {
        Acquiring acquiring = new Acquiring();
        NodeList children = acquiringNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getLocalName();
            if (nodeName == null) continue;
            
            switch (nodeName) {
                case "acquirerOrganization":
                    acquiring.setAcquirerOrganization(parseOrganization(child));
                    break;
                case "acquiredOrganization":
                    acquiring.setAcquiredOrganization(parseOrganization(child));
                    break;
                case "numberOfEmployeesMoved":
                    acquiring.setNumberOfEmployeesMoved(parseInteger(child.getTextContent()));
                    break;
            }
        }
        
        return acquiring;
    }

    private Organization parseOrganization(Node orgNode) {
        Organization org = new Organization();
        NodeList children = orgNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getLocalName();
            if (nodeName == null) continue;
            
            switch (nodeName) {
                case "id":
                    org.setId(parseLong(child.getTextContent()));
                    break;
                case "name":
                    org.setName(child.getTextContent());
                    break;
                case "creationDate":
                    org.setCreationDate(child.getTextContent());
                    break;
                case "annualTurnover":
                    org.setAnnualTurnover(parseFloat(child.getTextContent()));
                    break;
                case "fullName":
                    org.setFullName(child.getTextContent());
                    break;
                case "coordinates":
                    org.setCoordinates(parseCoordinates(child));
                    break;
                case "type":
                    org.setType(parseOrganizationType(child.getTextContent()));
                    break;
                case "officialAddress":
                    org.setOfficialAddress(parseAddress(child));
                    break;
            }
        }
        
        return org;
    }

    private Coordinates parseCoordinates(Node coordNode) {
        Coordinates coords = new Coordinates();
        NodeList children = coordNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getLocalName();
            if (nodeName == null) continue;
            
            switch (nodeName) {
                case "x":
                    coords.setX(parseLong(child.getTextContent()));
                    break;
                case "y":
                    coords.setY(parseFloat(child.getTextContent()));
                    break;
            }
        }
        
        return coords;
    }

    private Address parseAddress(Node addrNode) {
        Address address = new Address();
        NodeList children = addrNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getLocalName();
            if (nodeName == null) continue;
            
            switch (nodeName) {
                case "street":
                    address.setStreet(child.getTextContent());
                    break;
                case "town":
                    address.setTown(parseLocation(child));
                    break;
            }
        }
        
        return address;
    }

    private Location parseLocation(Node locNode) {
        Location location = new Location();
        NodeList children = locNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getLocalName();
            if (nodeName == null) continue;
            
            switch (nodeName) {
                case "x":
                    location.setX(parseFloat(child.getTextContent()));
                    break;
                case "y":
                    location.setY(parseLong(child.getTextContent()));
                    break;
                case "name":
                    location.setName(child.getTextContent());
                    break;
            }
        }
        
        return location;
    }

    private FireResponse parseFireResponse(SOAPMessage response) throws Exception {
        SOAPBody body = response.getSOAPBody();
        
        // Try to find fireResponse element
        NodeList responseNodes = body.getElementsByTagNameNS("*", "fireResponse");
        if (responseNodes.getLength() == 0) {
            responseNodes = body.getElementsByTagName("fireResponse");
        }
        
        if (responseNodes.getLength() > 0) {
            Node responseNode = responseNodes.item(0);
            NodeList children = responseNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String nodeName = child.getLocalName();
                if ("employeeCount".equals(nodeName)) {
                    return new FireResponse(parseInteger(child.getTextContent()));
                }
            }
            // If employeeCount is direct text content
            String content = responseNode.getTextContent();
            if (content != null && !content.isEmpty()) {
                return new FireResponse(parseInteger(content.trim()));
            }
        }
        
        // Try alternative parsing
        Node firstChild = body.getFirstChild();
        if (firstChild != null) {
            NodeList children = firstChild.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String nodeName = child.getLocalName();
                if ("fireResponse".equals(nodeName)) {
                    String content = child.getTextContent();
                    return new FireResponse(parseInteger(content.trim()));
                }
            }
        }
        
        throw new SoapClientException(500, "Invalid SOAP response: missing fireResponse element");
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Float parseFloat(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OrganizationType parseOrganizationType(String value) {
        if (value == null || value.trim().isEmpty()) return OrganizationType.COMMERCIAL;
        try {
            return OrganizationType.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return OrganizationType.COMMERCIAL;
        }
    }
}
