package com.esotericsoftware.yamlbeans.issues.issue37;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

public class Issue37Test {

	@Test
	public void test() throws YamlException {

		TestObject testObject = new TestObject();
		testObject.sexType = SexType.FEMALE;

		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.setScalarSerializer(SexType.class, new SexTypeSerializer());
		StringWriter sw = new StringWriter();
		YamlWriter writer = new YamlWriter(sw, yamlConfig);
		writer.write(testObject);
		writer.close();
		System.out.println(sw);
		YamlReader reader = new YamlReader(sw.toString(), yamlConfig);
		TestObject obj = reader.read(TestObject.class);
		assertEquals(SexType.FEMALE, obj.sexType);
	}
}
