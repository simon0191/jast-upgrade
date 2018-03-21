# JAST upgrade

_Java Abstract Syntax Tree upgrade_ is a proof of concept that aims to explore unconventional uses of java compiler annotation processors and AST modifications during compile time

## Examples

To run the examples exec `./gradlew :examples:run`

### String injection from JavaDoc

Writting multiline strings in Java is painful. Why not retrieve them from JavaDoc using [@StringFromComment](https://github.com/simon0191/jast-upgrade/blob/master/processors/src/main/java/pw/smn/jastupgrade/processors/StringFromCommentProcessor.java)?

```
  // Conventional multile String declaration
  String json = "{\n"
      +"  \"description\": \"This json comes from a comment\",\n"
      +"  \"hello\" : \"world\",\n"
      +"  \"blah\" : [1, 2, 3],\n"
      +"  \"four\" : {\n"
      +"    \"x\" : \"y\"\n"
      +"  }\n"
      +"}"

  // With jast-upgrade @StringFromComment annotation
  /**
   *{
   *  "description": "This json comes from a comment",
   *  "hello" : "world",
   *  "blah" : [1, 2, 3],
   *  "four" : {
   *    "x" : "y"
   *  }
   *}
   */
  @StringFromComment
  private static String multiLineJson() {
    return ""; // This will be replaced by the String in the Javadoc during compile time
  }
```

### Remove debug labels from the compiled code

(First of all. Did you know that you can use labels in Java? I didn't until starting to play with this project... More info: https://stackoverflow.com/questions/2710422/please-explain-the-usage-of-labeled-statements) So...

Why not abuse labels to wrap debug code that we don't want in the production build using [DebugLabelRemoverProcessor](https://github.com/simon0191/jast-upgrade/blob/master/processors/src/main/java/pw/smn/jastupgrade/processors/DebugLabelRemoverProcessor.java)?

```
  // Compiled code without DebugLabelRemoverProcessor
  void someMethod() {
    debug: {
      superSlowDebugMethod();
      log.debug("Some debug message that should not be in production");
    }
    // The actual code
  }

  // Compiled code with jast-upgrade DebugLabelRemoverProcessor
  void someMethod() {
    // The actual code
  }
```

## Potential use cases

- Validate and inject JSON, SQL, YAML, etc, strings extracted from javadocs on compilation time

- Do in compilation time what is being done with reflection like ORMs and dependency injection libraries.

- Embed non-java code in javadoc, compile it and inject its result in the java compiled code

- Remove code to make a lightweight jar/apk

- You tell me -> A.K.A. [submit a PR with an example](https://github.com/simon0191/jast-upgrade/pulls)

## Downsides

- IDE support. The IDEs are not expecting the modification of the AST and by default they don't know how to handle that. Project Lombok has already solved the problem with plugins, so maybe the solution is to build on top of them.

- Hard to debug. Since the modifications happen in compile time rather than runtime, I'm not sure if it's possible to attach a debugger to processors or at least I haven't found a way just yet.

- Going against the standards. Java has a very mature ecosystem with some already stablished and widely accepted standards like not using labels.

- Unkown edge cases. What if there are multiple libraries modifying the AST in the same project? Will that be a problem if they are expecting to modify the same aspects? 

- Usage of private API. The examples of this project mostly depend on a private API that is not well documented and changes with every new version of java.

## References

- [The Hacker’s Guide to Javac](http://scg.unibe.ch/archive/projects/Erni08b.pdf) by David Erni and Adrian Kuhn.

- [Project Lombok](https://projectlombok.org/) java library that automatically plugs into your editor and build tools, spicing up your java. Never write another getter or equals method again. Early access to future java features such as val, and much more.

- [Oracles's Doctree Javadocs](https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/com/sun/source/doctree/package-summary.html) which provides interfaces to represent documentation comments as abstract syntax trees (AST).

- Some blog posts about java processors and AST modification:
  - https://www.jcore.com/2016/12/03/modify-java-8-behaviour-annotations/

  - https://chariotsolutions.com/blog/post/changing-java-8-handling-nulls-ast/

  - http://www.baeldung.com/java-build-compiler-plugin
