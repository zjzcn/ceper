package com.github.zjzcn.ceper.filter;

import java.util.LinkedList;
import java.util.List;

import com.github.zjzcn.ceper.event.SourceEvent;
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.typesafe.config.Config;

/**
 * Support json: {"dataType":"cpu_usage", "data":{}, "attachments":{},"timestamp":1469015301632}
 * 
 * @author zjzcn
 *
 */
public class JsonConverter implements Converter {

	@Override
	public void config(Config config) {

	}

	@Override
	public void init() {

	}

	@Override
	public void close() {

	}

	@Override
	public List<SourceEvent> convert(Object rawData) {
		List<SourceEvent> list = new LinkedList<>();
		if(rawData instanceof String) {
			SourceEvent event = JsonUtils.toBean((String)rawData, SourceEvent.class);
			event.setHashTag(event.getDataType());
			list.add(event);
		}
		return list;
	}

}
