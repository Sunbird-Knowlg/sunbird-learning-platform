package org.ekstep.searchindex.producer;

import org.ekstep.searchindex.util.PropertiesUtil;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class MessagePartitioner implements Partitioner {
	public MessagePartitioner(VerifiableProperties props) {

	}

	public int partition(Object key, int a_numPartitions) {
		int partition = 0;
		String stringKey = (String) key;
		if (PropertiesUtil.getProperty("partition_" + stringKey.toLowerCase()) == null) {
			partition = 0;
		} else {
			partition = Integer.parseInt(PropertiesUtil.getProperty("partition_" + stringKey.toLowerCase()));
		}
		return partition;
	}
}
