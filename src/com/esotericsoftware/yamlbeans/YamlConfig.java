/*
 * Copyright (c) 2008 Nathan Sweet
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.esotericsoftware.yamlbeans;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.esotericsoftware.yamlbeans.Beans.Property;
import com.esotericsoftware.yamlbeans.emitter.EmitterConfig;
import com.esotericsoftware.yamlbeans.scalar.DateSerializer;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

/** Stores configuration for reading and writing YAML.
 * @author <a href="mailto:misc@n4te.com">Nathan Sweet</a> */
public class YamlConfig {
	/** Configuration for writing YAML. */
	public WriteConfig writeConfig = new WriteConfig();

	/** Configuration for reading YAML. */
	public ReadConfig readConfig = new ReadConfig();

	final Map<String, String> classNameToTag = new HashMap();
	final Map<String, Class> tagToClass = new HashMap();
	final Map<Class, ScalarSerializer> scalarSerializers = new IdentityHashMap();
	final Map<Property, Class> propertyToElementType = new HashMap();
	final Map<Property, Class> propertyToDefaultType = new HashMap();
	boolean beanProperties = true;
	boolean privateFields;
	boolean privateConstructors = true;
	boolean allowDuplicates = true;
	String tagSuffix;

	public YamlConfig() {
		scalarSerializers.put(Date.class, new DateSerializer());

		tagToClass.put("tag:yaml.org,2002:str", String.class);
		tagToClass.put("tag:yaml.org,2002:int", Integer.class);
		tagToClass.put("tag:yaml.org,2002:seq", ArrayList.class);
		tagToClass.put("tag:yaml.org,2002:map", HashMap.class);
		tagToClass.put("tag:yaml.org,2002:float", Float.class);
	}

	/**
	 * Allows duplicate keys in YAML document. Default is true
	 * @param allowDuplicates allow duplicates.
	 */
	public void setAllowDuplicates (boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}


	/**
	 * Allows the specified tag to be used in YAML instead of the full class name.
	 * @param tag the tag to be used instead of the class.
	 * @param type the class the tag will link to.
	 */
	public void setClassTag (String tag, Class type) {
		if (tag == null) throw new IllegalArgumentException("tag cannot be null.");
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (!tag.startsWith("!")) tag = "!" + tag;
		classNameToTag.put(type.getName(), tag);
		tagToClass.put(tag, type);
	}


	/**
	 * Adds a serializer for the specified scalar type
	 * @param type the class type the serializer will be linked to.
	 * @param serializer the serializer.
	 */
	public void setScalarSerializer (Class type, ScalarSerializer serializer) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (serializer == null) throw new IllegalArgumentException("serializer cannot be null.");
		scalarSerializers.put(type, serializer);
	}


	/**
	 * Sets the default type of elements in a Collection or Map property. No tag will be output for elements of this type.
	 * This type will be used for each element if no tag is found.
	 * @param type default class.
	 * @param propertyName name of property.
	 * @param elementType element type.
	 */
	public void setPropertyElementType (Class type, String propertyName, Class elementType) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (propertyName == null) throw new IllegalArgumentException("propertyName cannot be null.");
		if (elementType == null) throw new IllegalArgumentException("propertyType cannot be null.");
		Property property = Beans.getProperty(type, propertyName, beanProperties, privateFields, this);
		if (property == null)
			throw new IllegalArgumentException("The class " + type.getName() + " does not have a property named: " + propertyName);
		if (!Collection.class.isAssignableFrom(property.getType()) && !Map.class.isAssignableFrom(property.getType())) {
			throw new IllegalArgumentException("The '" + propertyName + "' property on the " + type.getName()
				+ " class must be a Collection or Map: " + property.getType());
		}
		propertyToElementType.put(property, elementType);
	}


	/**
	 * Sets the default type of a property. No tag will be output for values of this type. This type will be used if no tag is
	 * found.
	 * @param type class type
	 * @param propertyName  name of property
	 * @param defaultType default class type.
	 */
	public void setPropertyDefaultType (Class type, String propertyName, Class defaultType) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (propertyName == null) throw new IllegalArgumentException("propertyName cannot be null.");
		if (defaultType == null) throw new IllegalArgumentException("defaultType cannot be null.");
		Property property = Beans.getProperty(type, propertyName, beanProperties, privateFields, this);
		if (property == null)
			throw new IllegalArgumentException("The class " + type.getName() + " does not have a property named: " + propertyName);
		propertyToDefaultType.put(property, defaultType);
	}

	/**  */
	/**
	 * If true, bean properties with both a getter and setter will be used. Note the getter and setter methods must be named the
	 * same as the field they get or set. Default is true.
	 * @param beanProperties bean properties.
	 */
	public void setBeanProperties (boolean beanProperties) {
		this.beanProperties = beanProperties;
	}

	/**
	 * If true, private non-transient fields will be used. Default is false.
	 * @param privateFields enable private fields.
	 */
	public void setPrivateFields (boolean privateFields) {
		this.privateFields = privateFields;
	}


	/**
	 * If true, private no-arg constructors will be used. Default is true.
	 * @param privateConstructors enable private constructors.
	 */
	public void setPrivateConstructors (boolean privateConstructors) {
		this.privateConstructors = privateConstructors;
	}


	/**
	 * When not null, YAML read into a {@link Map} stores any value tags using key + tagSuffix, and when writing YAML the value
	 * tags are output. Key tags are not stored in the map. Default is null.
	 * @param tagSuffix tag suffix
	 */
	public void setTagSuffix (String tagSuffix) {
		this.tagSuffix = tagSuffix;
	}

	static public class WriteConfig {
		boolean explicitFirstDocument = false;
		boolean explicitEndDocument = false;
		boolean writeDefaultValues = false;
		boolean writeRootTags = true;
		boolean writeRootElementTags = true;
		boolean autoAnchor = false;
		boolean keepBeanPropertyOrder = false;
		WriteClassName writeClassName = WriteClassName.NEVER;
		Quote quote = Quote.NONE;
		Version version;
		Map<String, String> tags;
		boolean flowStyle;
		EmitterConfig emitterConfig = new EmitterConfig();

		WriteConfig () {
			emitterConfig.setUseVerbatimTags(false);
		}

		/** If true, the first document will have a document start token (---). Default is false. */
		public void setExplicitFirstDocument (boolean explicitFirstDocument) {
			this.explicitFirstDocument = explicitFirstDocument;
		}

		/** If true, the every document will have a document end token (...). Default is false. */
		public void setExplicitEndDocument (boolean explicitEndDocument) {
			this.explicitEndDocument = explicitEndDocument;
		}

		/** If true, the root of each YAML document will have a tag defining the class that was written, if necessary. Tags are not
		 * necessary for primitive types, Strings, {@link ArrayList}, or {@link HashMap}. It is useful to set this to false when
		 * planning to read the object with the {@link YamlReader#read(Class)} method. Default is true. */
		public void setWriteRootTags (boolean writeRootTags) {
			this.writeRootTags = writeRootTags;
		}

		/** If true, the elements of a Collection or Map root for each YAML document will have a tag defining the class that was
		 * written, if necessary. Tags are not necessary for primitive types, Strings, {@link ArrayList}, or {@link HashMap}. It is
		 * useful to set this to false when planning to read the object with the {@link YamlReader#read(Class, Class)} method.
		 * Default is true. */
		public void setWriteRootElementTags (boolean writeRootElementTags) {
			this.writeRootElementTags = writeRootElementTags;
		}

		/** If false, object fields with default values will not be written. A prototype object is created to determine the default
		 * value for each field on the object. Default is false. */
		public void setWriteDefaultValues (boolean writeDefaultValues) {
			this.writeDefaultValues = writeDefaultValues;
		}

		/** If true, values that are referenced multiple times will use an anchor. This works across YAML documents (ie multiple
		 * calls to {@link YamlWriter#write(Object)}). When true, objects are not actually written until
		 * {@link YamlWriter#clearAnchors()} or {@link YamlWriter#close()} is called. If changing auto anchor to false,
		 * {@link YamlWriter#clearAnchors()} should be called first to output any buffered objects. Default is true. */
		public void setAutoAnchor (boolean autoAnchor) {
			this.autoAnchor = autoAnchor;
		}

		/** If true, bean fields/properties are written in the same order as the fields are defined in the bean class. If false,
		 * they are sorted alphabetically. Default is false. */
		public void setKeepBeanPropertyOrder (boolean keepBeanPropertyOrder) {
			this.keepBeanPropertyOrder = keepBeanPropertyOrder;
		}

		/** Sets the YAML version to output. Default is 1.1. */
		public void setVersion (Version version) {
			this.version = version;
		}

		/** Sets the YAML tags to output. */
		public void setTags (Map<String, String> tags) {
			this.tags = tags;
		}

		/** If true, the YAML output will be canonical. Default is false. */
		public void setCanonical (boolean canonical) {
			emitterConfig.setCanonical(canonical);
		}

		/** Sets the number of spaces to indent. Default is 3. */
		public void setIndentSize (int indentSize) {
			emitterConfig.setIndentSize(indentSize);
		}

		/** Sets the column at which values will attempt to wrap. Default is 100. */
		public void setWrapColumn (int wrapColumn) {
			emitterConfig.setWrapColumn(wrapColumn);
		}

		/** If false, tags will never be surrounded by angle brackets (eg, "!&lt;java.util.LinkedList&gt;"). Default is false. */
		public void setUseVerbatimTags (boolean useVerbatimTags) {
			emitterConfig.setUseVerbatimTags(useVerbatimTags);
		}

		/** If false, unicode characters will be output instead of the escaped unicode character code. */
		public void setEscapeUnicode (boolean escapeUnicode) {
			emitterConfig.setEscapeUnicode(escapeUnicode);
		}

		/** Determines when class name tags are output. */
		public void setWriteClassname (WriteClassName write) {
			writeClassName = write;
		}

		/** The type of quotes to use when writing YAML output. */
		public void setQuoteChar (Quote quote) {
			this.quote = quote;
		}

		public Quote getQuote () {
			return quote;
		}

		/** If true, the YAML output will be flow. Default is false. */
		public void setFlowStyle (boolean flowStyle) {
			this.flowStyle = flowStyle;
		}

		public boolean isFlowStyle () {
			return flowStyle;
		}

		/** If true, the YAML output will be pretty flow. Default is false. */
		public void setPrettyFlow (boolean prettyFlow) {
			emitterConfig.setPrettyFlow(prettyFlow);
		}
	}

	static public class ReadConfig {
		Version defaultVersion = Version.DEFAULT_VERSION;
		ClassLoader classLoader;
		final Map<Class, ConstructorParameters> constructorParameters = new IdentityHashMap();
		boolean ignoreUnknownProperties;
		boolean autoMerge = true;
		boolean classTags = false;
		boolean guessNumberTypes;
		boolean anchors = false;

		ReadConfig () {
		}


		/**
		 * Sets the default YAML version to expect if a YAML document does not explicitly specify a version. Default is 1.1.
		 * @param defaultVersion the default version to set.
		 */
		public void setDefaultVersion (Version defaultVersion) {
			if (defaultVersion == null) throw new IllegalArgumentException("defaultVersion cannot be null.");
			this.defaultVersion = defaultVersion;
		}


		/**
		 * Sets the class loader to use to find classes read from the YAML
		 * @param classLoader sets the class loader.
		 */
		public void setClassLoader (ClassLoader classLoader) {
			this.classLoader = classLoader;
		}


		/**
		 * Sets the names of the constructor parameters so classes without no-arg constructors can be instantiated. The Java 6+
		 * annotation java.beans.ConstructorProperties can be used instead of this method.
		 * @param type the class type.
		 * @param parameterTypes the parameter type.
		 * @param parameterNames the parameter name.
		 */
		public void setConstructorParameters (Class type, Class[] parameterTypes, String[] parameterNames) {
			if (type == null) throw new IllegalArgumentException("type cannot be null.");
			if (parameterTypes == null) throw new IllegalArgumentException("parameterTypes cannot be null.");
			if (parameterNames == null) throw new IllegalArgumentException("parameterNames cannot be null.");
			ConstructorParameters parameters = new ConstructorParameters();
			try {
				parameters.constructor = type.getConstructor(parameterTypes);
			} catch (Exception ex) {
				throw new IllegalArgumentException(
					"Unable to find constructor: " + type.getName() + "(" + Arrays.toString(parameterTypes) + ")", ex);
			}
			parameters.parameterNames = parameterNames;
			constructorParameters.put(type, parameters);
		}


		/**
		 * When true, fields in the YAML that are not found on the class will not throw a {@link YamlException}. Default is
		 * false.
		 * @param allowUnknownProperties allow unknown properties
		 */
		public void setIgnoreUnknownProperties (boolean allowUnknownProperties) {
			this.ignoreUnknownProperties = allowUnknownProperties;
		}

		/**
		 * When false, tags are not used to look up classes. Default is true.
		 * @param classTags enable/disable class tags.
		 */
		public void setClassTags (boolean classTags) {
			if (classTags) throw new IllegalArgumentException("Class Tags cannot be enabled in YamlConfig, use UnsafeYamlConfig instead.");
		}


		/**
		 * When false, the merge key (&lt;&lt;) is not used to merge values into the current map. Default is true.
		 * @param autoMerge enable/disable automerge
		 */
		public void setAutoMerge (boolean autoMerge) {
			this.autoMerge = autoMerge;
		}


		/**
		 * When true, if the type for a scalar value is unknown and it looks like a number, it is read as a double or long. When
		 * false, if the type for a scalar value is unknown it is always read a string. Default is true.
		 * @param guessNumberTypes enable/disable guess number types
		 */
		public void setGuessNumberTypes (boolean guessNumberTypes) {
			this.guessNumberTypes = guessNumberTypes;
		}


		/**
		 * When false, anchors in the YAML are ignored. Default is true.
		 * @param anchors enable/disable anchors
		 */
		public void setAnchors (boolean anchors) {
			if (anchors) throw new IllegalArgumentException("Anchors cannot be enabled in YamlConfig, use UnsafeYamlConfig instead.");
		}
	}

	static class ConstructorParameters {
		public Constructor constructor;
		public String[] parameterNames;
	}

	public static enum WriteClassName {
		ALWAYS, NEVER, AUTO
	}

	public static enum Quote {
		NONE('\0'), SINGLE('\''), DOUBLE('"'), LITERAL('|'), FOLDED('>');

		char c;

		Quote (char c) {
			this.c = c;
		}

		public char getStyle () {
			return c;
		}
	}
}
