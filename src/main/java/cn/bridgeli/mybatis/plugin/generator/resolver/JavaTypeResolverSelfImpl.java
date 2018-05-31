package cn.bridgeli.mybatis.plugin.generator.resolver;

import java.sql.Types;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public class JavaTypeResolverSelfImpl extends JavaTypeResolverDefaultImpl {

    @Override
    public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
        String answer;
        JdbcTypeInformation jdbcTypeInformation = typeMap.get(introspectedColumn.getJdbcType());

        if (jdbcTypeInformation == null) {
            switch (introspectedColumn.getJdbcType()) {
                case Types.DECIMAL:
                    answer = "DECIMAL";
                    break;
                case Types.NUMERIC:
                    answer = "NUMERIC";
                    break;
                case Types.DATE:
                    answer = "TIMESTAMP";
                    break;
                default:
                    answer = null;
                    break;
            }
        } else {
            if ("DATE".equals(jdbcTypeInformation.getJdbcTypeName())) {
                answer = "TIMESTAMP";
            } else {
                answer = jdbcTypeInformation.getJdbcTypeName();
            }
        }

        return answer;
    }

}
