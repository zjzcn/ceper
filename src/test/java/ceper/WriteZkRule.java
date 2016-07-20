package ceper;

import java.util.HashMap;

import com.github.zjzcn.ceper.common.Constants;
import com.github.zjzcn.ceper.rule.Defination;
import com.github.zjzcn.ceper.rule.Rule;
import com.github.zjzcn.ceper.utils.JsonUtils;
import com.github.zjzcn.ceper.utils.ZkClient;

public class WriteZkRule {

	public static void main(String[] args) {
		ZkClient zkClient = ZkClient.getClient("localhost:2181");
		for(int i=0; i<10; i++) {
			Rule rule = new Rule();
			rule.setProcessorType("esper");
			rule.setStatementId("rule" + i);
			rule.setStatement("select count(*), avg(price) from Apple"+i+".win:time_batch(5 sec)");
			Defination def = new Defination();
			def.setDataType("Apple"+i);
			def.setFieldMap(new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					put("id", long.class);
					put("type", int.class);
					put("price", int.class);
				}
			});
			rule.addDefinations(def);
			zkClient.writeData(Constants.rulePath("cluster1")+"/rule"+i, JsonUtils.toJsonString(rule));
		}
	}
}
