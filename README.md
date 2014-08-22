Humanize for Java [![Build Status](http://img.shields.io/travis/mfornos/humanize.svg)](https://travis-ci.org/mfornos/humanize)
=========================================================================

Humanize is a Java facility for adding a “human touch” to data. It is thread-safe and supports per-thread internationalization.

The main modules are provided in two complementary variants:

**humanize-slim**

Lightweight distribution that only depends on Guava and standard Java APIs.

**humanize-icu**

Provides a concise facade for access to the [International Components for Unicode][] (ICU) Java APIs. Includes the slim distribution.

### Extensions

-   [Unified Emoji][] Easy Emoji handling for the JVM
-   [Joda time][] Joda time message format extensions
-   [UCUM][] Unified Units of Measurement
-   [JSF][] Java Server Faces converters
-   [Taglib][] JSP tag library

* * * *

  [International Components for Unicode]: http://icu-project.org/
  [Unified Emoji]: https://github.com/mfornos/humanize/tree/master/humanize-emoji
  [Joda Time]: https://github.com/mfornos/humanize/tree/master/humanize-joda
  [UCUM]: https://github.com/mfornos/humanize/tree/master/humanize-ucum
  [JSF]: https://github.com/mfornos/humanize/tree/master/humanize-jsf
  [Taglib]: https://github.com/mfornos/humanize/tree/master/humanize-taglib

Getting Started
---------------

### Maven [![Maven Badge](https://maven-badges.herokuapp.com/maven-central/com.github.mfornos/humanize/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mfornos/humanize)

Current version:

```xml
<humanize.version>1.2.1</humanize.version>
```

For the **slim** variant write the following in your *pom*:

```xml
<dependency>
  <groupId>com.github.mfornos</groupId>
  <artifactId>humanize-slim</artifactId>
  <version>${humanize.version}</version>
</dependency>
```

For the full-fledged **ICU** distribution:

```xml
<dependency>
  <groupId>com.github.mfornos</groupId>
  <artifactId>humanize-icu</artifactId>
  <version>${humanize.version}</version>
</dependency>
```

* * * *

Usage
-----

Using Humanize is quite simple.

1.  Import the static methods of the Humanize class that you want to call.
2.  Invoke these methods.

```java
import static humanize.Humanize.binaryPrefix;
 
class SomeClass {

  void doSomething() {
  
    String size = binaryPrefix(1325899906842624L); // 1.18 PB
    
  }
  
}
```

* * * *

Principal Methods
-----------------

### Date&Time

| Method           | Description                                                                                                     | Output                                                                                                                                      | Slim | ICU |
|------------------|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|------|-----|
| naturalDay       | For dates that are the current day or within one day, return ‘today’,‘tomorrow’ or ‘yesterday’, as appropriate. | ‘today’, ‘tomorrow’, ‘yesterday’ or the date formatted                                                                                      | ✔    | ✔   |
| naturalTime      | Computes both past and future relative dates with optional precision.                                           | ‘2 days from now’, ‘3 decades ago’, ‘3 days 16 hours from now’, ‘3 minutes from now’, ‘3 days 15 hours 38 minutes ago’, ‘moments ago’, etc. | ✔    | ✔   |
| nanoTime         | Formats a number of nanoseconds as the proper ten power unit.                                                   | ‘1.5µs’, ‘10.51ms’, ‘30ns’, etc.                                                                                                            | ✔    | -   |
| duration         | Formats a number of seconds as hours, minutes and seconds.                                                      | ‘1 sec.’, ‘1:02:10’, etc.                                                                                                                   | ✔    | ✔   |
| smartDateFormat  | Guesses the best locale-dependent pattern to format the date/time fields that the skeleton specifies.           | skeleton ‘MMMd’ produces ‘11 Dec.’                                                                                                          | -    | ✔   |
| prettyTimeFormat | Returns a PrettyTime instance for the current thread.                                                           | -                                                                                                                                           | ✔    | -   |

### Frequencies

| Method     | Description                                                                                                                                          | Output                           | Slim | ICU |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------|------|-----|
| times      | Interprets numbers as occurrences.                                                                                                                   | ‘never’, ‘once’, ‘5 times’, etc. | ✔    | -   |
| paceFormat | Matches a pace (value and interval) with a logical time frame. Very useful for slow paces. e.g. heartbeats, ingested calories, hyperglycemic crises. | ‘Approximately 7 times per day.’ | ✔    | -   |

### Numbers

| Method         | Description                                                       | Output                                                                                                                            | Slim | ICU |
|----------------|-------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------|-----|
| compactDecimal | Abbreviates numbers with short and long styles according to CLDR. | ‘2,4 Mio’, ‘100K’, ‘1M’, ‘2 millones’, ‘100 Tsd’, ‘100 Millionen’, etc.                                                           | -    | ✔   |
| ordinal        | Converts a number to its ordinal as a string.                     | ‘1st’, ‘2nd’, ‘3rd’, etc.                                                                                                         | ✔    | ✔   |
| spellBigNumber | Converts a big number to a friendly text representation.          | ‘2.3 thousand’, ‘1 million’, ‘–1.55 billion’, ‘3 decillion’, ‘2 googol’, etc.                                                     | ✔    | -   |
| spellDigit     | For decimal digits, returns the number spelled out.               | ‘one’, ‘two’, ‘three’, etc.                                                                                                       | ✔    | -   |
| spellNumber    | Converts the given number to words.                               | ‘twenty-three’, ‘two thousand eight hundred and forty’, ‘one million four hundred and twelve thousand six hundred and five’, etc. | -    | ✔   |

### Units

| Method         | Description                                                       | Output                                                                                                                            | Slim | ICU |
|----------------|-------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------|-----|
| binaryPrefix | Converts a given number to a string preceded by the corresponding binary International System of Units (SI) prefix. | ‘2 bytes’, ‘1.5 KB’, ‘5 MB’, ‘1.18 PB’, etc. | ✔ | - |
| metricPrefix | Converts a given number to a string preceded by the corresponding decimal multiplicative prefix. | ‘100k’, ‘1M’, ‘3.5M’, etc. | ✔ | - |
| formatCurrency | Smartly formats the given number as a monetary amount. | ‘£34’, ‘£1,000’, ‘£12.50’, etc. | ✔ | ✔ |
| formatPluralCurrency | Smartly formats the given

* * * *

Documentation
-------------

-   [Javadoc humanize-slim][]
-   [Javadoc humanize-icu][]

  [Javadoc humanize-slim]: http://mfornos.github.com/humanize/humanize-slim-apidocs/index.html
  [Javadoc humanize-icu]: http://mfornos.github.com/humanize/humanize-icu-apidocs/index.html

* * * *

Code Examples
-------------

Small set of code examples.

### Date&Time

```java
naturalTime(new Date()); 
// => "moments ago"

naturalTime(new Date(1000 * 60 * 60 * 24 * 1), new Date(0)); 
// => "1 day ago"

naturalTime(new Date(3155792597470L * 3L), new Date(0)); 
// => "3 centuries ago"

naturalTime(new Date(0), new Date(1000 * 60 * 12)); 
// => "12 minutes from now"

naturalTime(new Date(0), new Date(1000 * 60 * 60 * 24 * 1)); 
// => "1 day from now"

naturalTime(new Date(0), new Date(2629743830L * 3L)); 
// => "3 months from now"
```

Specifying precision

```java
// Fixed date for demonstration
Date moment = new Date(1000 * 60 * 60 * 24 * 3 + 1000 * 60 * 60 * 15 + 1000 * 60 * 38
                       + 1000 * 2);

naturalTime(new Date(0), moment, TimeMillis.HOUR) 
// => "3 days 16 hours from now"

naturalTime(moment, new Date(0), TimeMillis.SECOND)
// => "3 days 15 hours 38 minutes ago"
```
 

### Frequencies

```java
paceFormat(1.75, TimeMillis.HOUR.millis()); 
// => "Approximately 2 times per hour."

paceFormat(0, TimeMillis.SECOND.millis()); 
// => "Never."

paceFormat(1, TimeMillis.WEEK.millis()); 
// => "Approximately one time per week."
```

```java
times(0); // => "never"
times(1); // => "once"
times(2); // => "twice"
times(3); // => "3 times"
```

### Text

**Pluralize**

```java
pluralize("one", "{0}", "none", 1);  // => "one"
pluralize("one", "{0}", "none", 2);  // => "2"
pluralize("one", "{0}", "none", 0);  // => "none"
```

```java
pluralize("one {1}.", "{0} {1}s.", "no {1}.", 1, "disk");  // => "one disk."
pluralize("one {1}.", "{0} {1}s.", "no {1}.", 2, "disk");  // => "2 disks."
pluralize("one {1}.", "{0} {1}s.", "no {1}.", 0, "disk");  // => "no disk."
```

**Parameterized Pluralize**

```java
PluralizeParams p = PluralizeParams
                .begin("one {1}.")
                .many("{0} {1}s.")
                .none("no {1}.")
                .exts("disk");

pluralize(1, p); // => "one disk."
pluralize(2, p); // => "2 disks."
pluralize(0, p); // => "no disk."
```

**Pluralize MessageFormat**

```java
MessageFormat msg = pluralizeFormat("There {0} on {1}.::are no files::is one file"
                                     + "::are {2} files");

msg.render(0, "disk");    // => "There are no files on disk."
msg.render(1, "disk");    // => "There is one file on disk."
msg.render(1000, "disk"); // => "There are 1,000 files on disk."
```

```java
MessageFormat msg = pluralizeFormat("nothing::one thing::{0} things");

msg.render(-1); // => "nothing"
msg.render(0);  // => "nothing"
msg.render(1);  // => "one thing"
msg.render(2);  // => "2 things"
```
```java
MessageFormat msg = pluralizeFormat("one thing::{0} things");

msg.render(-1); // => "-1 things"
msg.render(0);  // => "0 things"
msg.render(1);  // => "one thing"
msg.render(2);  // => "2 things"
```

**Slugify**

```java
slugify("Cet été, j’en ai rien à coder"); 
// => "cet-ete-j-en-ai-rien-a-coder"

slugify("\nキャンパス//.Я_♥@борщ\n^abc"); 
// => "kiyanpasu-ia-borshch-abc"
```

### Parse

**Date & Time**

```java
parseISODateTime("2011-09-14T15:22:01Z");
parseISOTime("15:22:01Z");
```

Smart

```java
parseSmartDate("1.2.12", "dd/MM/yy", "yyyy/MM/dd", "dd/MM/yyyy");
```

### More Examples

-  [TestHumanize](https://github.com/mfornos/humanize/blob/master/humanize-slim/src/test/java/humanize/TestHumanize.java)
-  [TestICUHumanize](https://github.com/mfornos/humanize/blob/master/humanize-icu/src/test/java/humanize/TestICUHumanize.java)

* * * *

Dependencies
------------

### Humanize-slim

-   guava 17.0
-   prettytime 3.2.5.Final
-   unidecode 0.0.7

### Humanize-icu

-   humanize-slim ${humanize.version}
-   icu4j 53.1

### Package size concerns

**ICU**

If you need customize the data included by default in the ICU distribution (all features and languages ~7.1MB), the [ICU Data Library Customizer][] may be useful.

**ProGuard**

[Using ProGuard with Guava][].

  [ICU Data Library Customizer]: http://apps.icu-project.org/datacustom/
  [Using ProGuard with Guava]: https://code.google.com/p/guava-libraries/wiki/UsingProGuardWithGuava

* * * *

Supported languages
-------------------

`humanize-slim` naturalTime supports over 25 languages, see [prettytime][]. Rest of methods have support for english and spanish.

`humanize-icu` all languages supported by the [ICU APIs][].

### Custom languages

If you want to add support for other languages simply put a properties file named `Humanize\_{your\_locale}.properties` under the classpath `i18n`.

  [prettytime]: http://ocpsoft.org/prettytime
  [ICU APIs]: http://icu-project.org/

* * * *

Extensible Message Formats
--------------------------

If you want to plug in your own formats then place a `META-INF/services/humanize.spi.FormatProvider` file inside your jar. This file must contain a list of the full qualified class names of your `FormatProvider` implementations, separated by carriage return. Also, you can register manually your format factories on the message formatter.

### Built-in Humanize Extended Formats

Create a file `META-INF/services/humanize.spi.FormatProvider` with the following content:

```
humanize.text.HumanizeFormatProvider
humanize.text.MaskFormat
```

Now you can use it

```java
Humanize.format("{0,humanize,ordinal} greetings", 1); 
// => "1st greetings"
```

* * * *

Cache configuration
-------------------

By default the caches are configured to expire after 1 hour past the last access. If you want to change this behavior you need to provide a `humanize.properties` file, or specify the location of a properties file using the `humanize.config` system property, with a CacheBuilderSpec string configuration like this:

```
cache.builder.spec:expireAfterAccess=15m
```

The value must follow the CacheBuilderSpec syntax. The string syntax is a series of comma-separated keys or key-value pairs, each corresponding to a CacheBuilder method.

-   concurrencyLevel=[integer]: sets CacheBuilder.concurrencyLevel.
-   initialCapacity=[integer]: sets CacheBuilder.initialCapacity.
-   maximumSize=[long]: sets CacheBuilder.maximumSize.
-   maximumWeight=[long]: sets CacheBuilder.maximumWeight.
-   expireAfterAccess=[duration]: sets CacheBuilder.expireAfterAccess(long, java.util.concurrent.TimeUnit).
-   expireAfterWrite=[duration]: sets CacheBuilder.expireAfterWrite(long, java.util.concurrent.TimeUnit).
-   refreshAfterWrite=[duration]: sets CacheBuilder.refreshAfterWrite(long, java.util.concurrent.TimeUnit).
-   weakKeys: sets CacheBuilder.weakKeys().
-   softValues: sets CacheBuilder.softValues().
-   weakValues: sets CacheBuilder.weakValues().

Durations are represented by an integer, followed by one of “d”, “h”, “m”, or “s”, representing days, hours, minutes, or seconds respectively. (There is currently no syntax to request expiration in milliseconds, microseconds, or nanoseconds.)

* * * *

Integrations
------------

### Handlebars Helper

Humanize is integrated in the awesome [Handlebars.java][] project.

* * * *

Epilog
------

Have fun and stay fresh!

  [Handlebars.java]: https://github.com/jknack/handlebars.java

