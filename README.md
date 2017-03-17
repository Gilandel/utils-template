# utils-template
[![Build Status](https://api.travis-ci.org/Gilandel/utils-template.svg?branch=master)](https://travis-ci.org/Gilandel/utils-template)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/93179c9285e84c389aaeb7dcc305b32f)](https://www.codacy.com/app/gilles/utils-template)
[![Dependency Status](https://www.versioneye.com/user/projects/58b29b6f7b9e15003a17e544/badge.svg?style=flat)](https://www.versioneye.com/user/projects/58b29b6f7b9e15003a17e544)
[![codecov.io](https://codecov.io/github/Gilandel/utils-template/coverage.svg?branch=master)](https://codecov.io/github/Gilandel/utils-template?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-template/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-template)

[![codecov.io tree](https://codecov.io/gh/Gilandel/utils-template/branch/master/graphs/tree.svg)](https://codecov.io/gh/Gilandel/utils-template/branch/master)
[![codecov.io sunburst](https://codecov.io/gh/Gilandel/utils-template/branch/master/graphs/sunburst.svg)](https://codecov.io/gh/Gilandel/utils-template/branch/master)

Work progress:
![Code status](http://vbc3.com/script/progressbar.php?text=Code&progress=100)
![Test status](http://vbc3.com/script/progressbar.php?text=Test&progress=100)
![Benchmark status](http://vbc3.com/script/progressbar.php?text=Benchmark&progress=0)
![JavaDoc status](http://vbc3.com/script/progressbar.php?text=JavaDoc&progress=90)

```xml
<dependency>
	<groupId>fr.landel.utils</groupId>
	<artifactId>utils-template</artifactId>
	<version>1.0.0</version>
</dependency>
```

## Summary

1. [Summary](#summary)
2. [Base configuration](#base-configuration)
  1. [Load a single script](#load-a-single-script)
  2. [Load multiple scripts](#load-multiple-scripts)
3. [Example with an SQL script](#example-with-an-sql-script)
4. [Example with a JSON script](#example-with-a-json-script)
5. [Example with a custom script](#example-with-a-custom-script)

## Base configuration

### Load a single script

```java
// my_scripts is a classpath directory (example in a Maven project: src/main/resources/my_scripts)
final ScriptsLoader loader = new ScriptsLoader("my_scripts");

// to load the specified scripts
final ScriptsList<?> script = loader.init("test.sql", StandardCharsets.UTF_8);

// here, my script file contains two lines:
// -- comment
// select * from test where id = '{app.id}'

// we inject the replacement of my variable
final StringBuilder builder = loader.get(script, "app.id", "my_best_app");

// the output will look like (the first comment line has been removed)
// builder => select * from test where id = 'my_best_app'
```

### Load multiple scripts
First step, we create an enumeration that will list all scripts used.
```java
public enum EnumScripts implements ScriptsList<EnumScripts> {

    /**
     * The SQL test file (for test on loader)
     */
    LIST_USERS("users.sql"),

    /**
     * Select the main applications
     */
    MAIN_APPS("main_app.elastic", StandardCharsets.UTF_8);

    private final String name;
    private final Charset charset;

    EnumScripts(final String name, final Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    EnumScripts(final String name) {
        this(name, StandardCharsets.UTF_8);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public EnumScripts[] getValues() {
        return EnumScripts.values();
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }
}
```

After, we create a class that init the script loader.
```java
// In JavaEE
@ApplicationScoped
public class ScriptsInitializer {

	private static final String PATH = "my_scripts";
	
	@Produce
	public ScriptsLoader getLoader() {
		final ScriptsLoader scriptsLoader = new ScriptsLoader(PATH);
		scriptsLoader.init(EnumScripts.values());
		return scriptsLoader;
	}
}

// OR in Spring
@Configuration
public class ScriptsInitializer {

	private static final String PATH = "my_scripts";
	
	@Bean
	public ScriptsLoader getLoader() {
		final ScriptsLoader scriptsLoader = new ScriptsLoader(PATH);
		scriptsLoader.init(EnumScripts.values());
		return scriptsLoader;
	}
}

// OR through a simple singleton
public class ScriptsInitializer {

	private static final String PATH = "my_scripts";
	private static ScriptsLoader scriptsLoader;
	
	public static ScriptsLoader getLoader() {
		if (scriptsLoader == null) {
			scriptsLoader = new ScriptsLoader(PATH);
			scriptsLoader.init(EnumScripts.values());
		}
		return scriptsLoader;
	}
}
```

Last step, use the templates
```java

// In JavaEE
@Inject
private ScriptsLoader scriptsLoader;

// In Spring
@Autowired
private ScriptsLoader scriptsLoader;

// Through the singleton
private ScriptsLoader scriptsLoader = ScriptsInitializer.getLoader();

public List<User> loadScript(final String name, final List<Integer> ids) {
	final Map<String, String> replacements = new HashMap<>();
	
	replacements.put("name", "toto");
	replacements.put("multipleIds", CollectionUtils.isNotEmpty(ids));

	final String query = scriptsLoader.get(EnumScripts.LIST_USERS, replacements).toString();
	
	...
}

```

!!!Be careful about code injection!!!
Some checks can be implement in the template, but never trust parameters, for example in SQL use setParameter method to set unknown variable.

## Example with an SQL script

## Example with a JSON script

## Example with a custom script