package cn.bridgeli.mybatis.plugin.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 自定义注释生成
 */
public class CNCommentGenerator extends DefaultCommentGenerator {
    private Properties properties;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.properties = new Properties();
        this.properties.putAll(properties);
    }

    /**
     * 添加copyright说明
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine(" * ");

        String copyright = " * Copyright From 2018, BridgeLi.";
        compilationUnit.addFileCommentLine(copyright);

        compilationUnit.addFileCommentLine(" * ");
        compilationUnit.addFileCommentLine(" * " + compilationUnit.getType().getShortNameWithoutTypeArguments() + ".java");
        compilationUnit.addFileCommentLine(" * ");
        compilationUnit.addFileCommentLine(" */");
    }

    /**
     * 为类生成注释，并让类实现 Serializable 接口
     */
    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * <p>");

        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * " + remarkLine);
            }
            sb.append(" * ");
        }

        sb.append(" * 表 : ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        sb.append("的 model 类");
        topLevelClass.addJavaDocLine(sb.toString());

        topLevelClass.addJavaDocLine(" * ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

        String author = "$author$";

        if (properties.containsKey("author")) {
            author = properties.getProperty("author");
        }

        topLevelClass.addJavaDocLine(" * @author \t" + author);
        topLevelClass.addJavaDocLine(" * @date \t" + sdf.format(new Date()));
        topLevelClass.addJavaDocLine(" */");
        FullyQualifiedJavaType serializable = new FullyQualifiedJavaType("java.io.Serializable");

        topLevelClass.addImportedType(serializable);
        topLevelClass.addSuperInterface(serializable);

        Field serialVersionUID = new Field();
        serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
        serialVersionUID.setStatic(true);
        serialVersionUID.setFinal(true);
        serialVersionUID.setName("serialVersionUID");
        serialVersionUID.setType(new FullyQualifiedJavaType("long"));
        serialVersionUID.setInitializationString("1L");
        sb = new StringBuilder();
        sb.append("/** ");
        sb.append(" 类的 seri version id");
        sb.append(" */");
        serialVersionUID.addJavaDocLine(sb.toString());

        topLevelClass.addField(serialVersionUID);
    }

    /**
     * 为字段生成注释
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        /**
         * 这是个示例字段注释 表字段：table.name
         */
        StringBuffer sb = new StringBuffer();

        sb.append("/** ");
        sb.append("字段:");
        sb.append(introspectedColumn.getActualColumnName());

        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            sb.append("，");
            sb.append(remarks);
        }

        sb.append(" */");
        field.addJavaDocLine(sb.toString());
    }
}
