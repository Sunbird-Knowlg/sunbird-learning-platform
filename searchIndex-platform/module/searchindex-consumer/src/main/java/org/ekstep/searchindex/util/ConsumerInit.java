package org.ekstep.searchindex.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConsumerInit")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsumerInit{

	@XmlAttribute  
	public String serverURI;
	@XmlAttribute  
	public String topic;
}