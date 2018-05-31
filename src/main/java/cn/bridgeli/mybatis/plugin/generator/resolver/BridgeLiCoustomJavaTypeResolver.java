package cn.bridgeli.mybatis.plugin.generator.resolver;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * 默认 {@linkplain JavaTypeResolverDefaultImpl TypeResolver} 的扩展； 主要用于将 DB
 * 中的类型转换成相应 jdbc 类型，但其实可以通过 &lt;table&gt; 中的 &lt; columnOverride
 * 属性来进行更改（不过使用的本类约定的方式更加方便）
 */
public class BridgeLiCoustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {
    protected Map<String, Integer> typeExtMap;

    public BridgeLiCoustomJavaTypeResolver() {
        super();
        typeExtMap = new HashMap<>();
        initTypeSet();
    }

    @Override
    public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {
        /**
         * 读取自定义配置，value 值必须为 className，并覆盖默认 typeMap
         */
        for (String jdbcType : typeExtMap.keySet()) {

            String value = properties.getProperty(jdbcType);

            if (hasText(value)) {
                typeMap.put(typeExtMap.get(jdbcType), new JdbcTypeInformation(jdbcType.substring(jdbcType.indexOf(".") + 1), new FullyQualifiedJavaType(value)));
            }
        }
        /**
         * 之后回调父类方法
         */
        return super.calculateJavaType(introspectedColumn);
    }

    private void initTypeSet() {
        typeExtMap.put("jdbcType.ARRAY", Types.ARRAY);
        typeExtMap.put("jdbcType.BIGINT", Types.BIGINT);
        typeExtMap.put("jdbcType.BINARY", Types.BINARY);
        typeExtMap.put("jdbcType.BIT", Types.BIT);
        typeExtMap.put("jdbcType.BLOB", Types.BLOB);
        typeExtMap.put("jdbcType.BOOLEAN", Types.BOOLEAN);
        typeExtMap.put("jdbcType.CHAR", Types.CHAR);
        typeExtMap.put("jdbcType.CLOB", Types.CLOB);
        typeExtMap.put("jdbcType.DATALINK", Types.DATALINK);
        typeExtMap.put("jdbcType.DATE", Types.DATE);
        typeExtMap.put("jdbcType.DECIMAL", Types.DECIMAL);
        typeExtMap.put("jdbcType.DISTINCT", Types.DISTINCT);
        typeExtMap.put("jdbcType.DOUBLE", Types.DOUBLE);
        typeExtMap.put("jdbcType.FLOAT", Types.FLOAT);
        typeExtMap.put("jdbcType.INTEGER", Types.INTEGER);
        typeExtMap.put("jdbcType.JAVA_OBJECT", Types.JAVA_OBJECT);
        typeExtMap.put("jdbcType.LONGNVARCHAR", Types.LONGNVARCHAR);
        typeExtMap.put("jdbcType.LONGVARBINARY", Types.LONGVARBINARY);
        typeExtMap.put("jdbcType.LONGVARCHAR", Types.LONGVARCHAR);
        typeExtMap.put("jdbcType.NCHAR", Types.NCHAR);
        typeExtMap.put("jdbcType.NCLOB", Types.NCLOB);
        typeExtMap.put("jdbcType.NVARCHAR", Types.NVARCHAR);
        typeExtMap.put("jdbcType.NULL", Types.NULL);
        typeExtMap.put("jdbcType.NUMERIC", Types.NUMERIC);
        typeExtMap.put("jdbcType.OTHER", Types.OTHER);
        typeExtMap.put("jdbcType.REAL", Types.REAL);
        typeExtMap.put("jdbcType.REF", Types.REF);
        typeExtMap.put("jdbcType.SMALLINT", Types.SMALLINT);
        typeExtMap.put("jdbcType.STRUCT", Types.STRUCT);
        typeExtMap.put("jdbcType.TIME", Types.TIME);
        typeExtMap.put("jdbcType.TIMESTAMP", Types.TIMESTAMP);
        typeExtMap.put("jdbcType.TINYINT", Types.TINYINT);
        typeExtMap.put("jdbcType.VARBINARY", Types.VARBINARY);
        typeExtMap.put("jdbcType.VARCHAR", Types.VARCHAR);
    }

    public static boolean hasText(String text) {
        if (text != null && text.trim().length() > 0) {
            return true;
        }
        return false;
    }
}
