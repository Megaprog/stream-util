# Stream Utility

The helper class for working with Java 8 streams. It allows to create streams from Optional, from Supplier, from Matcher etc. 

## How to get it?

You can use it as a maven dependency:

```xml
<dependency>
    <groupId>org.jmmo</groupId>
    <artifactId>stream-util</artifactId>
    <version>1.3</version>
</dependency>
```

Or download the latest build at:
    https://github.com/megaprog/stream-util/releases

## How to use it?

To create stream from supplier function:

```java
Stream<Object> stream = StreamUtil.supply(() -> hasData() ? read() : null);
```
  
To create files stream (all files in directory and subdirectories) :

```java
Stream<Path> files = StreamUtil.files("myDirectory");
```
    