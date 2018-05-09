# mybatis-generator-plugin

* 把该项目 `clone` 下来之后，在本地执行 `mvn clean install` 或者 `mvn clean deploy` (`deploy` 需要修改 `pom` 中的 `distributionManagement`)

* 在要生成的项目的 `pom` 文件中添加如下配置：

```java
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.3.5</version>
                <dependencies>
                    <dependency>
                        <groupId>cn.bridgeli</groupId>
                        <artifactId>mybatis-generator-plugin</artifactId>
                        <version>0.0.2-SNAPSHOT</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <verbose>true</verbose>
                    <overwrite>true</overwrite>
                    <configurationFile>src/main/resources/generatorConfig.xml</configurationFile>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

* `configurationFile` 配置 `generatorConfig.xml` 文件的位置，其内容在本项目的 `src/main/resources` 有一个模板供参考，`copy` 其内容放到相应的位置

* 修改 `generatorConfig.xml` 中 想用的内容，主要是：`jdbcConnection`、`javaModelGenerator`、`sqlMapGenerator`、`javaClientGenerator`、`table`

* 在项目的 `pom` 所在的地方执行命令：`mvn mybatis-generator:generate` 生成相应的 `Java` 类和 `mapper` 文件
