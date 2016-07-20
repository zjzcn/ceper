package com.github.zjzcn.ceper.sink;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.event.ResultEvent;
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.typesafe.config.Config;

public class KafkaSink extends AbstractSink {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaSink.class);


	private Producer<String, String> producer;

	private Properties props = new Properties();
	private String topic;
	
	@Override
	public void config(Config config) {
		if(config.hasPath("bootstrap_servers")) {
			props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap_servers"));
		} else {
			throw new IllegalArgumentException("Kafka source must have config item[bootstrap_servers].");
		}

		if(config.hasPath("topic")) {
			topic = config.getString("topic");
		} else {
			throw new IllegalArgumentException("Kafka source must have config item[topic].");
		}

		if(config.hasPath("acks")) {
			props.put(ProducerConfig.ACKS_CONFIG, config.getString("acks"));
		}

		if(config.hasPath("batch_size")) {
			props.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getString("batch_size"));
		}

		if(config.hasPath("max_request_size")) {
			props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config.getString("max_request_size"));
		}
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
	}

	@Override
	public void start() {
		logger.info("Starting kafka sink {}.", getName());
		try {
			producer = new KafkaProducer<String, String>(props);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to create kafka producer.", e);
		}
		logger.info("Kafka sink started.");
	}

	@Override
	public void stop() {
		logger.info("Stoping kafka sink {}.", this);
		try {
			producer.close();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to close kafka producer.", e);
		}
		logger.info("Kafka sink {} stoped.");
	}

	@Override
	public void process(ResultEvent event) {
		try {
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, JsonUtils.toJsonString(event));
			logger.debug("Message sending to kafka, record={}.", record);
			producer.send(record);
			logger.debug("Message sent to kafka.");
		} catch (Exception ex) {
			logger.error("Failed while sending message to kafka.", ex);
		}
	}

}
