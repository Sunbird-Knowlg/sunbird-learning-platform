package org.ekstep.searchindex.util;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConsumerConfigs")
public class ConsumerConfigs {

	@XmlElement(name = "ConsumerConfig")
	public List<ConsumerConfig> consumerConfig;
	
}
