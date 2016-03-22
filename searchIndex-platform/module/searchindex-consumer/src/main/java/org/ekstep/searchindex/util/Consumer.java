package org.ekstep.searchindex.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Consumer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Consumer {

	@XmlAttribute
	public String partitions;

	public void setPartitions(String partitions) {
		this.partitions = partitions;
	}
}