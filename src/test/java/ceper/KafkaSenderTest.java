package ceper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class KafkaSenderTest {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaSenderTest.class);

	private Properties props = new Properties();

	private Producer<String, String> producer;

	private String topic = "topic";
	
	public void config() {
		 props.put("bootstrap.servers", "localhost:9092");
		 props.put("acks", "all");
		 props.put("retries", 0);
		 props.put("batch.size", 16384);
		 props.put("linger.ms", 1);
		 props.put("buffer.memory", 33554432);
		 props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		 props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

	}

	public void start() {
		try {
			producer = new KafkaProducer<String, String>(props);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create kafka producer.", e);
		}
	}

	public void process(String data) {
		try {
			producer.send(new ProducerRecord<String, String>(topic, data));
			logger.info("Message sent kafka: {}.", data);
		} catch (Exception ex) {
			logger.error("Failed to send message", ex);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		KafkaSenderTest sender = new KafkaSenderTest();
		sender.config();
		sender.start();
		while(true) {
			for(int i=0; i<10; i++) {
				Map<String, Object> data = new HashMap<>();
				data.put("id", i);
				data.put("type", i%2);
				data.put("price", i);
				Map<String, Object> event = new HashMap<>();
				event.put("dataType", "Apple"+i);
				event.put("data", data);
				event.put("timestamp", System.currentTimeMillis());
				sender.process(JSON.toJSONString(event));
				Thread.sleep(1000);
			}
		}
	}
}
