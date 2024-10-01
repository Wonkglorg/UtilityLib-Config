# UtilityLib - Config

![alt text](https://github.com/Wonkglorg/Minecraft-UtilityLib/blob/master/Logo.png?raw=true)

## Index

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Installation](#installation)
* [Overview](#overview)
* [Guides](#guide)
* [Credits](#credits)

## <a name="introduction"></a>Introduction

Lightweight Config and Language manager library as a seperate module as part of the UtilityLib library

## <a name="requirements"></a>Requirements

* Spigot
* Minecraft 1.16.* and above
* JAVA 16 or above

## <a name="installation"></a>Installation

Repository

```yml
<repository>
     <id>jitpack.io</id>
     <url>https://jitpack.io</url>
</repository>
```

Adding the dependency

```yml
<dependency>
     <groupId>com.github.Wonkglorg</groupId>
     <artifactId>UtilityLib-Config</artifactId>
     <version>version</version>
</dependency>
```

## <a name="overview"></a>Overview

### Initialisation
Before using the either of the Managers they need to be instantiated by a static createInstance function (this should be used before calling any of its methods, preferably in the onLoad Section of the plugin.
```java
public final class ExamplePlugin extends JavaPlugin {

	@Override
	public void onLoad() {
	     ConfigManager.createInstance(this);
	     LangManager.createInstance(this);
	}

	@Override
	public void onEnable() {

	}


	@Override
	public void onDisable() {

	}
}
```

### Usage
Once the instance is created, it can be filled by providing the ConfigManager with any type of valid config for example the yaml representation as ConfigYML
```java
	@Override
	public void onEnable() {
          configManager.add("items", new ConfigYML(this, Path.of("subdirectory", "items.yml")));
	}
```

Once the instance is asigned it can be called anywhere by its defined name "items" these can be retrieved by the ConfigManager#getConfig function

Langmanager works very similar but instead of a dedicated name works by a system of Locale defined configs, this defines when to show what config files value,
a default lang can also be defined to use if no valid lang was found for the desired Locale
```java
	@Override
	public void onEnable() {
          langManager.setDefaultLang(Locale.ENGLISH,new LangConfig(this,"path/to/en-us.yml"));
          langManager.addLanguage(Locale.GERMAN,new LangConfig(this,"path/to/de.yml"));
	}
```
To retrieve a value the getValue function can be used which determins the best value to return based on inputs


* Config Manager
  * Adds a singleton ConfigManger to assign and retrieve individual configs from by a programmer specified key.
* Lang Manager 
  * Adds a singleton LangManger to dynamically choose the correct lang file to retrieve a message from allows for player specific language depending on their client
## <a name="guide"></a> Guides

## <a name="credits"></a>Credits

This plugin is being developed by [Wonkglorg](https://gitlab.com/u/Wonkglorg).
