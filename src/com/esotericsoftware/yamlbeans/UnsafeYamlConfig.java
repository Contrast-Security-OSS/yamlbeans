
package com.esotericsoftware.yamlbeans;

/** UnsafeYamlConfig extends YamlConfig and enables class tags and anchors. If this config is used, it opens the user
 * to Denial of Service and Deserialization attacks. Only use this if you trust the author of the YAML document being
 * read and utilise the class tag or anchor functionality.
 * <p>
 * Usage :
 * <pre>
 * UnsafeYamlConfig config = new UnsafeYamlConfig();
 * YamlReader reader = new YamlReader(yamlData.toString(), config);
 * Data data = reader.read();
 * </pre>
 */
public class UnsafeYamlConfig extends YamlConfig {
	public UnsafeYamlConfig() {
		super.readConfig = new UnsafeReadConfig();
		super.writeConfig = new UnsafeWriteConfig();
	}

	static public class UnsafeReadConfig extends ReadConfig {
		public UnsafeReadConfig () {
			super.anchors = true;
			super.classTags = true;
		}

		public void setClassTags (boolean classTags) {
			super.classTags = classTags;
		}

		public void setAnchors (boolean anchors) {
			super.anchors = anchors;
		}
	}

	static public class UnsafeWriteConfig extends WriteConfig {
		public UnsafeWriteConfig () {
			super.autoAnchor = true;
			super.writeClassName = WriteClassName.AUTO;
		}
	}
}
