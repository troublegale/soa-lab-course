package itmo.ivank.soap.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "organizationType")
@XmlEnum
public enum OrganizationType {
    @XmlEnumValue("COMMERCIAL")
    COMMERCIAL,
    
    @XmlEnumValue("GOVERNMENT")
    GOVERNMENT,
    
    @XmlEnumValue("PRIVATE_LIMITED_COMPANY")
    PRIVATE_LIMITED_COMPANY,
    
    @XmlEnumValue("OPEN_JOINT_STOCK_COMPANY")
    OPEN_JOINT_STOCK_COMPANY
}
