# utils-template
[![Build Status](https://api.travis-ci.org/Gilandel/utils-template.svg?branch=master)](https://travis-ci.org/Gilandel/utils-template/builds)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/93179c9285e84c389aaeb7dcc305b32f)](https://www.codacy.com/app/gilles/utils-template)
[![codecov.io](https://codecov.io/github/Gilandel/utils-template/coverage.svg?branch=master)](https://codecov.io/github/Gilandel/utils-template?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-template/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-template)
[![Javadocs](http://www.javadoc.io/badge/fr.landel.utils/utils-template.svg)](http://www.javadoc.io/doc/fr.landel.utils/utils-template)

[![Tokei LoC](https://tokei.rs/b1/github/Gilandel/utils-template)](https://github.com/Aaronepower/tokei)
[![Tokei NoFiles](https://tokei.rs/b1/github/Gilandel/utils-template?category=files)](https://github.com/Aaronepower/tokei)
[![Tokei LoComments](https://tokei.rs/b1/github/Gilandel/utils-template?category=comments)](https://github.com/Aaronepower/tokei)

[![codecov.io tree](https://codecov.io/gh/Gilandel/utils-template/branch/master/graphs/tree.svg)](https://codecov.io/gh/Gilandel/utils-template/branch/master)
[![codecov.io sunburst](https://codecov.io/gh/Gilandel/utils-template/branch/master/graphs/sunburst.svg)](https://codecov.io/gh/Gilandel/utils-template/branch/master)

Work progress:
![Code status](http://vbc3.com/script/progressbar.php?text=Code&progress=100)
![Test status](http://vbc3.com/script/progressbar.php?text=Test&progress=100)
![Benchmark status](http://vbc3.com/script/progressbar.php?text=Benchmark&progress=100)
![JavaDoc status](http://vbc3.com/script/progressbar.php?text=JavaDoc&progress=100)

```xml
<dependency>
    <groupId>fr.landel.utils</groupId>
    <artifactId>utils-template</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Summary

1. [Summary](#summary)
1. [Base configuration](#base-configuration)
   1. [Simple replacements](#simple-replacements)
   1. [Load a single script](#load-a-single-script)
   1. [Load multiple scripts](#load-multiple-scripts)
1. [Example with an SQL script](#example-with-an-sql-script)
1. [Example with a JSON script](#example-with-a-json-script)
1. [Example with a custom script](#example-with-a-custom-script)
1. [Changelog](#changelog)
1. [License](#license)

## Base configuration

### Simple replacements

```java
ScriptsReplacer replacer = new ScriptsReplacer();

String result = replacer.replace("{ a ??{a}::%s}", Collections.singletonMap("a", "b"));
// result => b
```

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

The input script template
```
-- Get the name and height of all motorcycles or bikes, following if they contain an engine, with a length inferior to 1500mm
-- if it's a racing or touring motorbike, get the tire front and rear width, 
-- if it's a racing bike, get the tire width,
-- else return -1
select
        b.name,
        s.height,
        {engine && (racing || touring)??
            tf.width as front_tire,
            tr.width as rear_tire
        ::
            {!engine && racing??
                t.width,
                t.width
            ::
                -1,
                -1
            }
        }
    from
    {engine??
        motorcycle b
        inner join specification s on b.id=s.fk_vehicule
        {racing || touring??
            inner join tire tf on tf.id=s.fk_front_tire
            inner join tire tr on tr.id=s.fk_rear_tire
        }
    ::
        bike b
        inner join specification s on b.id=s.fk_vehicule
        {racing??
            inner join tire t on t.id=s.fk_tire
        }
    }
    where s.length < :length;
```

The replacements:
```java
final Map<String, Object> replacements = new HashMap<>();
replacements.put("engine", true); // the value doesn't count, here engine is not replaced in the script, so the value is unused
replacements.put("racing", "competition");

// we inject the replacement of my variable
final StringBuilder builder = loader.get(script, replacements);
```

The result:
```sql
select
        b.name,
        s.height,
            tf.width as front_tire,
            tr.width as rear_tire
    from
        motorcycle b
        inner join specification s on b.id=s.fk_vehicule
            inner join tire tf on tf.id=s.fk_front_tire
            inner join tire tr on tr.id=s.fk_rear_tire
    where s.length < :length;
```

## Example with a JSON script

The script template:
```json
{
    "size" : 0,
    "query" : {
        "filtered" : {
            "query" : {
                "match_all" : {}
            },
            "filter" : {
                "bool" : {
                    "should" : [{
                            "terms" : {
                                "app_id" : ["<apps>"]
                            }
                        }
                    ],
                    "must" : [{
                            "range" : {
                                "review_date" : {
                                    "gte" : "<start>",
                                    "lte" : "<end>",
                                    "format" : "epoch_millis"
                                }
                            }
                        }
                    ],
                    "must_not" : []
                }
            }
        }
    }
}
```

The replacements:
```java
final Map<String, Object> replacements = new HashMap<>();
replacements.put("apps", "my_app_id");
replacements.put("start", startDate.getTime());
replacements.put("end", endDate.getTime());

final ScriptsLoader loader = new ScriptsLoader();

loader.setPath("my_scripts");
loader.getReplacer().setTemplate(ScriptsTemplate.TEMPLATE_JSON);

loader.init(EnumScripts.MY_SCRIPT);

StringBuilder builder = loader.get(EnumScripts.MY_SCRIPT, replacements);
```

The result:
```json
{
    "size" : 0,
    "query" : {
        "filtered" : {
            "query" : {
                "match_all" : {}
            },
            "filter" : {
                "bool" : {
                    "should" : [{
                            "terms" : {
                                "app_id" : ["my_app_id"]
                            }
                        }
                    ],
                    "must" : [{
                            "range" : {
                                "review_date" : {
                                    "gte" : "1489833298121",
                                    "lte" : "1489833299132",
                                    "format" : "epoch_millis"
                                }
                            }
                        }
                    ],
                    "must_not" : []
                }
            }
        }
    }
}
```

## Example with a custom script

The template:
```java
public class MyTemplate extends AbstractScriptsTemplate {
    @Override
    protected void init() {
        this.setExpressionOpen("$");
        this.setExpressionClose("£");
        this.setBlockOpen("\\");
        this.setBlockClose("/");
        this.setOperatorThen("THEN");
        this.setOperatorElse("ELSE");
        this.setOperatorAnd("AND");
        this.setOperatorOr("OR");
        this.setOperatorNot("NOT");

        this.setRemoveComments(Boolean.TRUE);
        this.setRemoveBlankLines(Boolean.TRUE);
        
        this.setOneLineCommentOperator("#");
        this.setMultiLineCommentOperators("#_", "_#");
        
        this.setChecker((input) -> {
            Assertor.that(input).not().contains('=').orElseThrow("the script cannot contains the '=' character");
        });
    }
}
```

The input script template
```
#_
multi line comment
_#

$ value1 AND \value2 OR value3/ THEN
    # comment line
    12
ELSE
    $value1£
£

# empty variable
$£

#_
multi line comment without end
```

The replacements:
```java
// Create the script loader
final ScriptsLoader loader = new ScriptsLoader();

// Init the path and our custom template
loader.setPath("my_scripts");
loader.getReplacer().setTemplate(new MyTemplate());

// Init the scripts list
loader.init(EnumScripts2.values());

// Create the replacements map
final Map<String, String> replacements = new HashMap<>();
replacements.put("value1", "v1");

// we inject the replacement of my variable
final StringBuilder builder = loader.get(script, replacements);
```

The result:
```
    v1
```

## Changelog
### 1.0.4 - 2018-07-02
- Misc: update dependencies
- Misc: remove classpath definition from JAR (Wildfly warning when some dependencies are in multiple versions and defined provided)

### 1.0.3 - 2018-02-15
- Add: variable tags to customize replacer template

## License
Apache License, version 2.0