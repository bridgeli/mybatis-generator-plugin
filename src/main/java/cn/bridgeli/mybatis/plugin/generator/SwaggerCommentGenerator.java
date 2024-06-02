package cn.bridgeli.mybatis.plugin.generator;

import cn.bridgeli.mybatis.plugin.util.DateUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Date;
import java.util.Properties;

/**
 * 自定义注释生成
 */
public class SwaggerCommentGenerator extends DefaultCommentGenerator {
    private Properties properties;
    private boolean addRemarkComments = false;
    private static final String API_MODEL_FULL_CLASS_NAME = "io.swagger.v3.oas.annotations.media.Schema";

    /**
     * 设置用户配置的参数
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.addRemarkComments = StringUtility.isTrue(properties.getProperty("addRemarkComments"));
        this.properties = new Properties();
        this.properties.putAll(properties);
    }

    /**
     * 给字段添加注释
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        String remarks = introspectedColumn.getRemarks();
        //根据参数和备注信息判断是否添加备注信息
        if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
            addFieldJavaDoc(field, introspectedColumn);
            //数据库中特殊字符需要转义
            if (remarks.contains("\"")) {
                remarks = remarks.replace("\"", "'");
            }
            //给model的字段添加swagger注解
            field.addJavaDocLine("@Schema(title = \"" + remarks + "\")");
        }
    }

    /**
     * 给model的字段添加注释
     */
    private void addFieldJavaDoc(Field field, IntrospectedColumn introspectedColumn) {

        StringBuffer sb = new StringBuffer();
        sb.append("/**\n    ");
        sb.append(" * 字段: ");
        sb.append(introspectedColumn.getActualColumnName());
        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            sb.append("，");
            sb.append(remarks);
        }

        sb.append("\n     */");
        field.addJavaDocLine(sb.toString());
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" *");

        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * " + remarkLine);
            }
            sb.append(" * ");
        }

        sb.append("表: ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        sb.append(" 的 model 类");
        topLevelClass.addJavaDocLine(sb.toString());
        topLevelClass.addJavaDocLine(" *");

        String author = "BridgeLi";
        if (properties.containsKey("author")) {
            author = properties.getProperty("author");
        }

        topLevelClass.addJavaDocLine(" * @author " + author);
        topLevelClass.addJavaDocLine(" * @date " + DateUtil.date2Str(new Date()));
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
        sb.append("/**\n    ");
        sb.append(" * 类的 serial version id\n    ");
        sb.append(" */");
        serialVersionUID.addJavaDocLine(sb.toString());

        topLevelClass.addField(serialVersionUID);
        topLevelClass.addImportedType(API_MODEL_FULL_CLASS_NAME);
        topLevelClass.addAnnotation("@Schema(title = \"" + introspectedTable.getFullyQualifiedTable() + "\", description = \"" + remarks + "\")");
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");

        String copyright = " * Copyright From 2018, BridgeLi.";
        compilationUnit.addFileCommentLine(copyright);

        compilationUnit.addFileCommentLine(" * ");
        compilationUnit.addFileCommentLine(" * " + compilationUnit.getType().getShortNameWithoutTypeArguments() + ".java");
        compilationUnit.addFileCommentLine(" */");
    }
}
