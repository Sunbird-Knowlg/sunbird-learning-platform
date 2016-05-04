package org.ekstep.searchindex.consumer;

import org.ekstep.searchindex.processor.IMessageProcessor;
import org.ekstep.searchindex.util.ConsumerUtil;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class ConsumerThread implements Runnable {
    private KafkaStream<byte[], byte[]> m_stream;
    private int m_threadNumber;
    private ConsumerUtil consumerUtil = new ConsumerUtil();
    private String processor;
    private IMessageProcessor messagePrcessor;
    private ConsumerConnector m_consumer;

    public ConsumerThread(KafkaStream<byte[], byte[]> a_stream, int a_threadNumber, String messageProcessor, ConsumerConnector consumer)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        System.out.println("");
        this.m_stream = a_stream;
        this.m_threadNumber = a_threadNumber;
        this.m_consumer = consumer;
        this.processor = messageProcessor;
        messagePrcessor = consumerUtil.getMessageProcessorFactory(messageProcessor);
    }

    public void run() {
        try {
            ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
            while (it.hasNext()) {
                String message = new String(it.next().message());
                messagePrcessor.processMessage(message);
                //System.out.println("message processed by: " + this.m_threadNumber + " | processor : " + this.processor);
            }
            System.out.println("Shutting down Thread: " + m_threadNumber);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
    }
}