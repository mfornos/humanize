# Unified Emoji for Java

Easy Emoji handling for the JVM. :green_heart:

## Features

* Concise API for common use cases
* Search by code points, raw characters (moji) and annotations
* Unified Emoji character database
* UTF-16 encoding support
* Vendor codes mapping (DoCoMo, KDDI, SoftBank)
* Sexy and easy text interpolation of moji characters and aliases.

## Dependency

**Maven**

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mfornos/humanize-emoji/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mfornos/humanize-emoji)

```xml
<dependency>
  <groupId>com.github.mfornos</groupId>
  <artifactId>humanize-emoji</artifactId>
  <version>${humanize.version}</version>
</dependency>
```

## Usage
 
### Emoji Succinct API

**Image replacement**

```java
EmojiApi.replaceUnicodeWithImages("I ❤ Emoji")
// => "I <img class="emoji" src="http://localhost/img/2764.png" alt="heavy black heart" /> Emoji"

EmojiApi.imageTagByUnicode("❤");
// => <img class="emoji" src="http://localhost/img/2764.png" alt="heavy black heart" />
```

**Configuration**

Before calling image replacement methods, please, configure the EmojiApi object.

```java
EmojiApi.configure().assetsURL("http://localhost/img/");
```

**Search**

You can search Emoji characters by name, tags, hex string and raw Unicode.

```java
EmojiApi.byName("green heart");
```

Returns an EmojiChar object with the following structure:

```json
{
   "ordering":173,
   "code":"1F49A",
   "defaultStyle":"emoji",
   "sources":"j",
   "name":"green heart",
   "version":"V6.0",
   "raw":"?",
   "annotations":[
      "emotion",
      "green",
      "heart"
   ],
   "mappings":{
      "KDDI":[
         "F37B",
         "?"
      ],
      "SOFT_BANK":[
         "F9CB",
         "?"
      ]
   }
}
```

```java
EmojiApi.search("cat", "smile");
```

Returns a list of EmojiChar objects:

```json
[
   {
      "ordering":245,
      "code":"1F63A",
      "defaultStyle":"emoji",
      "sources":"j",
      "name":"smiling cat face with open mouth",
      "version":"V6.0",
      "raw":"?",
      "annotations":[
         "animal",
         "cat",
         "face",
         "mouth",
         "nature",
         "open",
         "smile",
         "smiley",
         "smiling"
      ],
      "mappings":{
         "KDDI":[
            "F465",
            "?"
         ]
      }
   },
   {
      "ordering":247,
      "code":"1F63C",
      "defaultStyle":"emoji",
      "sources":"j",
      "name":"cat face with wry smile",
      "version":"V6.0",
      "raw":"?",
      "annotations":[
         "animal",
         "cat",
         "face",
         "ironic",
         "nature",
         "smile",
         "smiley",
         "smiling",
         "wry"
      ],
      "mappings":{
         "KDDI":[
            "F46E",
            "?"
         ]
      }
   },
   {
      "ordering":243,
      "code":"1F638",
      "defaultStyle":"emoji",
      "sources":"j",
      "name":"grinning cat face with smiling eyes",
      "version":"V6.0",
      "raw":"?",
      "annotations":[
         "animal",
         "cat",
         "eye",
         "face",
         "grin",
         "grinning",
         "nature",
         "smile",
         "smiley",
         "smiling"
      ],
      "mappings":{
         "KDDI":[
            "F484",
            "?"
         ]
      }
   },
   {
      "ordering":246,
      "code":"1F63B",
      "defaultStyle":"emoji",
      "sources":"j",
      "name":"smiling cat face with heart-shaped eyes",
      "version":"V6.0",
      "raw":"?",
      "annotations":[
         "animal",
         "cat",
         "eye",
         "face",
         "heart",
         "love",
         "nature",
         "smile",
         "smiley",
         "smiling"
      ],
      "mappings":{
         "KDDI":[
            "F469",
            "?"
         ]
      }
   }
]
```

```java
EmojiApi.byUnicode("\uD83D\uDD36");
```

```json
{
   "ordering":1484,
   "code":"1F536",
   "defaultStyle":"emoji",
   "sources":"j",
   "name":"large orange diamond",
   "version":"V6.0",
   "raw":"?",
   "annotations":[
      "diamond",
      "geometric",
      "orange",
      "sign"
   ],
   "mappings":{
      "KDDI":[
         "F762",
         "?"
      ]
   }
}
```

```java
EmojiApi.byHexCode("1f536");
```


### Advanced

[TBD]

