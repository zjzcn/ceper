package com.github.zjzcn.ceper.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zjzcn.ceper.utils.Assert;
import com.github.zjzcn.ceper.utils.ClassUtils;

public class SourceFactory {

		private static final Logger logger = LoggerFactory.getLogger(SourceFactory.class);

		public static Source create(String type) {
			Assert.notBlank(type, "type must have value");
			logger.info("Creating instance of source, type {}", type);
			try {
				Source source = null;
				switch (type) {
				case "kafka":
					source = new KafkaSource();
					break;
				default:
					source = (Source) ClassUtils.newInstance(type);
					break;
				}
				return source;
			} catch (Exception ex) {
				throw new RuntimeException("Unable to create source, type: " + type, ex);
			}
		}
	}