package org.ekstep.searchindex.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConsumerGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsumerGroup {
	@XmlAttribute
	public String id;
	
	@XmlAttribute
    public String messageProcessor;

	@XmlElementWrapper(name = "Consumers")
	@XmlElement(name = "Consumer")
	public List<Consumer> consumers;
	
	public void setMessageProcessor(String messageProcessor) {
        this.messageProcessor = messageProcessor;
    }
}
