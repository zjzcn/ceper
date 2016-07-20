package com.github.zjzcn.ceper.source;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

public class KafkaSource extends AbstractPollingSource {

	private static final Logger logger = LoggerFactory.getLogger(KafkaSource.class);

	private Consumer<String, String> consumer;

	private Properties props = new Properties();
	private String topic;
	private boolean autoCommitEnabled = true;

	@Override
	public void config(Config config) {
		if(config.hasPath("bootstrap_servers")) {
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap_servers"));
		} else {
			throw new IllegalArgumentException("Kafka source must have config item[bootstrap_servers].");
		}
		
		if(config.hasPath("topic")) {
			topic = config.getString("topic");
		} else {
			throw new IllegalArgumentException("Kafka source must have config item[topic].");
		}
		
		if(config.hasPath("group_id")) {
			props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString("group_id"));
		} else {
			throw new IllegalArgumentException("Kafka source must have config item[group_id].");
		}
		
		if(config.hasPath("fetch_min_bytes")) {
			props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, config.getString("fetch_min_bytes"));
		}
		
		if(config.hasPath("auto_commit_enabled")) {
			autoCommitEnabled = config.getBoolean("auto_commit_enabled");
			props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getString("auto_commit_enabled"));
		}
		
		if(config.hasPath("auto_commit_interval_ms")) {
			props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getString("auto_commit_interval_ms"));
		}
		if(config.hasPath("session_timeout_ms")) {
			props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.getString("session_timeout_ms"));
		}
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
	}

	@Override
	public void start() {
		logger.info("Starting kafka source {}", this.getName());
		try {
			consumer = new KafkaConsumer<String, String>(props);
			consumer.subscribe(Arrays.asList(topic));
		} catch (Exception e) {
			throw new RuntimeException("Unable to create kafka consumer.", e);
		}
		
		logger.info("Kafka source started.");
	}

	@Override
	public void stop() {
		logger.info("Stoping kafka source {}", this.getName());
		if(consumer != null) {
			consumer.close();
		}
		logger.info("Kafka source stoped.");
	}

	@Override
	public void process() {
		ConsumerRecords<String, String> records = consumer.poll(1000);
		for (ConsumerRecord<String, String> record : records) {
			logger.debug("Message received from kafka: {}.", record);
			String value = record.value();
			getFilterChain().process(value);
		}

		if (!autoCommitEnabled) {
			// commit the read transactions to Kafka to avoid duplicates
			consumer.commitSync();
		}
	}

}
