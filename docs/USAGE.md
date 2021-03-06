[![Released Version][maven-img]][maven]

# UML Doclet Usage

## Usage

A [Doclet][doclet-api] is a sort of _plugin_ to the [javadoc][javadoc-wiki] 
tool to modify the standard behaviour of generating documentation.
The `UMLDoclet` delegates to the 'Standard' doclet to generate
all the 'normal' output you are used to. Furthermore, it analyzes the
parsed code to produce `UML` diagrams of your _classes_ and _packages_.
These UML diagrams can be produced both in a [text-based][plantuml] 
and image format (e.g. `svg` or `png`).
Javadoc generation can be integrated into many build systems such 
as _[maven](#configuring-your-maven-build)_, 
_[gradle](#using-gradle)_ or even _[ant](#using-ant)_.

The commandline javadoc command is [explained here by Oracle][javadoc-command]
but the main syntax is as follows:
```bash
javadoc [packages|source-files] [options][@files]
```

Suppose you have downloaded version `2.x` of the UML doclet (`umldoclet-2.x.jar`).
Say you have a java project with sources under `src` and classpath dependencies on `lib`.
Run the following command to document your software package `com.foobar` 
in the directory `apidocs` using the UML doclet:
```bash
javadoc -sourcepath src -classpath lib -d apidocs \
    -docletpath umldoclet-2.x.jar -doclet nl.talsmasoftware.umldoclet.UMLDoclet \
    com.foobar
``` 

:information_source: **Tip:**
_The [javadoc][javadoc-command] commandline becomes verbose rather quickly,
therefore please consider using one of the following build systems for your projects:_
- [Maven](#configuring-your-maven-build) 
- [Gradle](#using-gradle)

## Configuring your maven build

Maven builds have the advantage of dependency management to fetch the UML doclet
as part of the rest of your build:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <version>3.0.1</version>
            <executions>
                <execution>
                    <id>attach-javadocs</id>
                    <goals>
                        <goal>jar</goal>
                    </goals>
                    <configuration>
                        <doclet>nl.talsmasoftware.umldoclet.UMLDoclet</doclet>
                        <docletArtifact>
                            <groupId>nl.talsmasoftware</groupId>
                            <artifactId>umldoclet</artifactId>
                            <version>2.x</version>
                        </docletArtifact>
                        <additionalOptions>
                            <!--<additionalOption>...</additionalOption>-->
                        </additionalOptions>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Please don't forget to change the `2.x` above to the latest release (see top of this page). 

:exclamation: **Note:**
_Version 2 and higher uses the new [Javadoc API][javadoc-oracle] from JDK 9 and above.
To build with an older JDK, please use the latest [1.x version][usage-v1] of this doclet_

## Using gradle

In gradle, the doclet and its dependency need to be declared.
From there on, the configuration is the same as your regular JavaDoc configuration.

```groovy
apply plugin: 'java'

configurations {
    umlDoclet
}

dependencies {
    umlDoclet "nl.talsmasoftware:umldoclet:2.x"
}

javadoc {
    source = sourceSets.main.allJava
    options.docletpath = configurations.umlDoclet.files.asType(List)
    options.doclet = "nl.talsmasoftware.umldoclet.UMLDoclet"
    options.addStringOption "additionalParamName", "additionalParamValue"
}
```

Please don't forget to change the `2.x` above to the latest release (see top of this page).

Obviously, replace `additionalParamName` and `additionalParamValue` with the
[additional options](#additional-options) you require.

## Using ant

In ant, the javadoc task needs to be told to use the UML Doclet in a similar way.

```xml
<javadoc destdir="target/javadoc" sourcepath="src">
    <doclet name="nl.talsmasoftware.umldoclet.UMLDoclet" pathref="umlDoclet.classpath"> 
        <param name="additionalParamName" value="additionalParamValue" />
    </doclet>
</javadoc>
```

Make sure a path reference is defined for `umlDoclet.classpath` pointing to the 
`umldoclet-2.x.jar`. It may be a good idea to use [Ivy] in this case.  
Replace `additionalParamName` and `additionalParamValue` with the name and value 
of each [additional parameter](#additional-options) you need.

## Additional options

The UML doclet supports all options of the `Standard` doclet and adds some of its own.
To display all options, please run:
```bash
javadoc --help -docletpath umldoclet-2.x.jar -doclet nl.talsmasoftware.umldoclet.UMLDoclet
```

The rest of this section lists various options that are specific to the UML doclet.

#### -umlImageDirectory &lt;image-dir&gt;

By default, the UML images are generated relative to the HTML pages 
for the class or package. Specifying an _image directory_ will place all 
generated images in this single directory, making linking to them easier
in some cases.  
_This was requested in issue [Send images to a single directory](#25)_

#### -umlImageFormat (svg|png|eps|none)

By default `.svg` images are generated as they will be significantly smaller
in size than equivalent `.png` images and scale better.
This option allows this default to be overridden. You can even generate _multiple_ 
images for each diagram, by providing this option more than once.


  [maven-img]: https://img.shields.io/maven-central/v/nl.talsmasoftware/umldoclet.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22nl.talsmasoftware%22%20AND%20a%3A%22umldoclet%22
  [javadoc-wiki]: https://en.wikipedia.org/wiki/Javadoc
  [javadoc-oracle]: https://docs.oracle.com/javase/9/javadoc/javadoc.htm
  [javadoc-command]: https://docs.oracle.com/javase/9/javadoc/javadoc-command.htm
  [doclet-api]: https://docs.oracle.com/javase/10/docs/api/jdk/javadoc/doclet/package-summary.html 
  [plantuml]: http://plantuml.com
  [usage-v1]: https://github.com/talsma-ict/umldoclet/blob/develop-v1/docs/USAGE.md
  [ivy]: http://ant.apache.org/ivy
