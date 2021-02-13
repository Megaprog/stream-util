[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jmmo/stream-util/badge.png)](https://maven-badges.herokuapp.com/maven-central/org.jmmo/stream-util)

# Stream Utility

The helper class for working with Java 8 streams. It allows to create streams from Optional, from Supplier, from Matcher etc. 

## How to use it?

To create stream from supplier function:

```java
Stream<Object> stream = StreamUtil.supply(() -> hasData() ? read() : null);
```
  
To create files stream (all files in directory and subdirectories):

```java
Stream<Path> files = StreamUtil.files("myDirectory");
```

To wrap lambdas throws checked exceptions:
 
```java
void method(int a, String b) throws IOException, InterruptedException { }

boolean isSomething(int a, String b) throws IOException, TimeoutException { return true; }

public void testUnchecked_Throw() throws Exception {
    Stream.of(1)
        .filter(i1 -> StreamUtil.unchecked(() -> isSomething(i1, "1")))
        .forEach(i2 -> StreamUtil.unchecked(() -> method(i2, "2")));
}
```

To wrap lambdas throws InterruptedException:
 
```java
StreamUtil.uninterrupted(() -> Thread.sleep(1));
```

To logging expensive expressions:
 
```java
log.debug("Debugging of {}", LazyToString.of(() -> "some expensive expression"));
```
