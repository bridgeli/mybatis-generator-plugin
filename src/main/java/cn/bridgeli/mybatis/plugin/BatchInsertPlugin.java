package cn.bridgeli.mybatis.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class BatchInsertPlugin extends PluginAdapter {

    /**
     * 修改Mapper类
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addBatchInsertMethod(interfaze, introspectedTable);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    /**
     * 修改Mapper.xml
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addBatchInsertSelectiveXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void addBatchInsertMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        // 设置需要导入的类
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Method ibsmethod = new Method();
        // 1.设置方法可见性
        ibsmethod.setVisibility(JavaVisibility.PUBLIC);
        // 2.设置返回值类型
        // int型
        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
        ibsmethod.setReturnType(ibsreturnType);
        // 3.设置方法名
        ibsmethod.setName("insertBatch");
        // 4.设置参数列表
        FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType paramListType;
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        } else {
            throw new RuntimeException(getString("RuntimeError.12"));
        }
        paramType.addTypeArgument(paramListType);

        ibsmethod.addParameter(new Parameter(paramType, "records"));

        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(ibsmethod);
    }

    private void addBatchInsertSelectiveXml(Document document, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        // 获得要自增的列名
        String incrementField = introspectedTable.getTableConfiguration().getProperties().getProperty("incrementField");
        if (incrementField != null) {
            incrementField = incrementField.toUpperCase();
        }

        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "insertBatch"));
        insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        StringBuilder sqlElement = new StringBuilder();
        StringBuilder javaPropertyAndDbType = new StringBuilder("(");
        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();
            // 不是自增字段的才会出现在批量插入中
            if (!columnName.toUpperCase().equals(incrementField)) {
                sqlElement.append(columnName + ",\n      ");
                javaPropertyAndDbType.append("\n      #{item." + introspectedColumn.getJavaProperty() + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},");
            }
        }

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));
        insertBatchElement.addElement(new TextElement("insert into " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " ("));
        insertBatchElement.addElement(new TextElement("  " + sqlElement.delete(sqlElement.lastIndexOf(","), sqlElement.length()).toString()));
        insertBatchElement.addElement(new TextElement(") values "));
        foreachElement.addElement(new TextElement(javaPropertyAndDbType.delete(javaPropertyAndDbType.length() - 1, javaPropertyAndDbType.length()).append("\n      )").toString()));
        insertBatchElement.addElement(foreachElement);

        document.getRootElement().addElement(insertBatchElement);
    }

}