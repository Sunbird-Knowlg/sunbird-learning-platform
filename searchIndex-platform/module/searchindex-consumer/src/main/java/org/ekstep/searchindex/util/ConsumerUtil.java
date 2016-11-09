package org.ekstep.searchindex.util;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ekstep.searchindex.processor.IMessageProcessor;

import com.ilimi.common.dto.CoverageIgnore;

public class ConsumerUtil {

	private ConsumerConfigs consumerConfigs;
	
	public ConsumerUtil() {
		this.consumerConfigs = readConsumerProperties();
	}

	public ConsumerConfigs getConsumerConfigs() {
		return consumerConfigs;
	}

	@CoverageIgnore
	@SuppressWarnings("unchecked")
	public IMessageProcessor getMessageProcessorFactory(String messageProcessorName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<IMessageProcessor> clazz = (Class<IMessageProcessor>) Class.forName(messageProcessorName);
		IMessageProcessor messageProcessor= clazz.newInstance();
		return messageProcessor;
	}

	@CoverageIgnore
	public ConsumerConfigs readConsumerProperties() {
		try {
			String filename = "consumer-config.xml";
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
			JAXBContext jaxbContext = JAXBContext.newInstance(ConsumerConfigs.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ConsumerConfigs consumerConfigs = (ConsumerConfigs) jaxbUnmarshaller.unmarshal(is);
			return consumerConfigs;

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;

	}
}
