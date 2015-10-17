# Stream Utility

The helper class for working with Java 8 streams. It allows to create streams from Optional, from Supplier, from Matcher etc. 

## How to get it?

You can use it as a maven dependency:

```xml
<dependency>
    <groupId>org.jmmo</groupId>
    <artifactId>stream-util</artifactId>
    <version>1.0</version>
</dependency>
```

Or download the latest build at:
    https://github.com/megaprog/stream-util/releases

## How to use it?

```java
Stream<Object> stream = StreamUtil.supply(() -> hasData() ? read() : null);
```
    