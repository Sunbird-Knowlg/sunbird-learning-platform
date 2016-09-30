package org.ekstep.searchindex.util;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ekstep.searchindex.processor.IMessageProcessor;

import com.ilimi.common.dto.CoverageIgnore;

public class ConsumerUtil {

	private ConsumerConfig consumerConfig;
	
	public ConsumerUtil() {
		this.consumerConfig = readConsumerProperties();
	}

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	@CoverageIgnore
	@SuppressWarnings("unchecked")
	public IMessageProcessor getMessageProcessorFactory(String messageProcessorName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<IMessageProcessor> clazz = (Class<IMessageProcessor>) Class.forName(messageProcessorName);
		IMessageProcessor messageProcessor= clazz.newInstance();
		return messageProcessor;
	}

	@CoverageIgnore
	public ConsumerConfig readConsumerProperties() {
		try {
			String filename = "consumer-config.xml";
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
			JAXBContext jaxbContext = JAXBContext.newInstance(ConsumerConfig.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ConsumerConfig consumerConfig = (ConsumerConfig) jaxbUnmarshaller.unmarshal(is);
			return consumerConfig;

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;

	}
}
