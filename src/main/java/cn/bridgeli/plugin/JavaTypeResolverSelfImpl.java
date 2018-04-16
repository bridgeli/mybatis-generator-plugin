package cn.bridgeli.plugin;

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
                    answer = "DECIMAL"; //$NON-NLS-1$
                    break;
                case Types.NUMERIC:
                    answer = "NUMERIC"; //$NON-NLS-1$
                    break;
                case Types.DATE:
                    answer = "TIMESTAMP"; //$NON-NLS-1$
                    break;
                default:
                    answer = null;
                    break;
            }
        } else {
            if (jdbcTypeInformation.getJdbcTypeName().equals("DATE")) {
                answer = "TIMESTAMP";
            } else {
                answer = jdbcTypeInformation.getJdbcTypeName();
            }
        }

        return answer;
    }

}
