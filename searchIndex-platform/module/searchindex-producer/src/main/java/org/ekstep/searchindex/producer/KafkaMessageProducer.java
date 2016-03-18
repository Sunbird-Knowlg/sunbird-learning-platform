package org.ekstep.searchindex.producer;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.Constants;
import org.ekstep.searchindex.util.PropertiesUtil;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class KafkaMessageProducer implements IMessageProducer{
	private String TOPIC;
	private ProducerConfig producerConfig;
	private Producer<String, String> producer;
	private ObjectMapper mapper = new ObjectMapper();

	public void init() {
		TOPIC = PropertiesUtil.getProperty("topic");
		Properties properties = new Properties();
		properties.put("metadata.broker.list", PropertiesUtil.getProperty("metadata.broker.list"));
		properties.put("serializer.class", PropertiesUtil.getProperty("serializer.class"));
		properties.put("partitioner.class", "org.ekstep.searchindex.producer.MessagePartitioner");
		producerConfig = new ProducerConfig(properties);
	}

	public void pushMessage(Map<String, Object> message) {
		try {
			if (message != null && message.get("objectType") != null) {
				String objectType = (String) message.get("objectType");
				producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
				String jsonMessage = mapper.writeValueAsString(message);
				KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(TOPIC,
						objectType, jsonMessage);
				producer.send(keyedMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			producer.close();
		}
	}

	public static void main(String arg[]) throws JsonParseException, JsonMappingException, IOException {
		KafkaMessageProducer producer = new KafkaMessageProducer();
		producer.init();
		JSONBuilder builder = new JSONStringer();
		/*builder.object().key("operationType").value(Constants.OPERATION_CREATE).key("graphId").value("hi")
				.key("nodeGraphId").value(1).key("nodeUniqueId").value("hi_1").key("objectType")
				.value(Constants.OBJECT_TYPE_DOMAIN).key("nodeType").value(Constants.NODE_TYPE_DATA).endObject();*/
		
		 /*builder.object().key("operationType").value(Constants.
		 OPERATION_CREATE).key("graphId").value("hi")
		 .key("nodeGraphId").value("2").key("nodeUniqueId").value("hi_2").key(
		 "objectType")
		 .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 NODE_TYPE_DATA) .key("transactionData").object()
		 .key("addedProperties").array().object()
		 .key("propertyName").value("lemma") .key("value").value("Hi 2")
		 .endObject() .endArray() .endObject() .endObject();*/
		

		  builder.object().key("operationType").value(Constants.
		  OPERATION_UPDATE).key("graphId").value("hi")
		  .key("nodeGraphId").value(4).key("nodeUniqueId").value("hi_4").key(
		//.key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_1").key(

		  "objectType")
		  .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		  NODE_TYPE_DATA)
		  .key("transactionData").object().key("addedProperties").array().
		  object()
		  .key("propertyName") .value("lemma")
		  //.key("value").value("पाकिस्तान  वालों")
		  .key("value").value("aHi,atest")
		  .endObject()
		  .object().key("propertyName")
		  .value("sources").key("value").array().value("class 1"
		  ).value("rwo").endArray().endObject()
		  .endArray().key("removedProperties").array().value("sourceTypes").
		  endArray().key("addedTags").array() .value("grade one"
		  ).endArray().key("removedTags").array().value("grade three"
		  ).endArray().endObject() .endObject();

		
		
/*		builder.object().key("operationType").value(Constants.OPERATION_UPDATE).key("graphId").value("hi")
		.key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key("objectType")
		.value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.NODE_TYPE_DEFINITION).endObject();*/
		 
		
		Map<String, Object> message = producer.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		
		producer.pushMessage(message);
		
		builder = new JSONStringer();
		builder.object().key("operationType").value(Constants.
				  OPERATION_UPDATE).key("graphId").value("hi")
				 // .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_3").key(
				 .key("nodeGraphId").value(5).key("nodeUniqueId").value("hi_5").key(

				  "objectType")
				  .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
				  NODE_TYPE_DATA)
				  .key("transactionData").object().key("addedProperties").array().
				  object()
				  .key("propertyName") .value("lemma")
				  .key("value").value("पाकिस्तान ,वालों")
				  //.key("value").value("aHi test")
				  .endObject()
				  .object().key("propertyName")
				  .value("sources").key("value").array().value("class 1"
				  ).value("rwo").endArray().endObject()
				  .endArray().key("removedProperties").array().value("sourceTypes").
				  endArray().key("addedTags").array() .value("grade one"
				  ).endArray().key("removedTags").array().value("grade three"
				  ).endArray().endObject() .endObject();
		
		message = producer.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		
		producer.pushMessage(message);
		
		builder = new JSONStringer();
		builder.object().key("operationType").value(Constants.
				  OPERATION_UPDATE).key("graphId").value("hi")
				  .key("nodeGraphId").value(6).key("nodeUniqueId").value("hi_6").key(
				 //.key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_1").key(

				  "objectType")
				  .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
				  NODE_TYPE_DATA)
				  .key("transactionData").object().key("addedProperties").array().
				  object()
				  .key("propertyName") .value("lemma")
				  //.key("value").value("पाकिस्तान  वालों")
				  .key("value").value("[Hi,test]")
				  .endObject()
				  .object().key("propertyName")
				  .value("sources").key("value").array().value("class 1"
				  ).value("rwo").endArray().endObject()
				  .endArray().key("removedProperties").array().value("sourceTypes").
				  endArray().key("addedTags").array() .value("grade one"
				  ).endArray().key("removedTags").array().value("grade three"
				  ).endArray().endObject() .endObject();
		
		message = producer.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		
		producer.pushMessage(message);
		
	}
}
