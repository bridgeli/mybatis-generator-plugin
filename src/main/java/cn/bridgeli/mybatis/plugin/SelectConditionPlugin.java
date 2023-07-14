package cn.bridgeli.mybatis.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 指定查询字段
 */
public class SelectConditionPlugin extends PluginAdapter {

    /**
     * 修改Mapper类
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        addSelectConditionMethod(interfaze, introspectedTable);

        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    /**
     * 修改Mapper.xml
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addSelectConditionXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }


    @Override
    public boolean validate(List<String> list) {
        return true;
    }


    private void addSelectConditionMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());


        Method ibsmethod = new Method();
        // 1.设置方法可见性
        ibsmethod.setVisibility(JavaVisibility.PUBLIC);

        // 2.设置返回值类型
        FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getNewListInstance();

        //设置List的类型是实体类的对象
        FullyQualifiedJavaType listType;
        listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        importedTypes.add(listType);

        ibsreturnType.addTypeArgument(listType);
        ibsmethod.setReturnType(ibsreturnType);

        // 3.设置方法名
        ibsmethod.setName("selectByCondition");

        // 4.设置参数列表
        FullyQualifiedJavaType paramType = new FullyQualifiedJavaType(introspectedTable.getExampleType());

        ibsmethod.addParameter(new Parameter(paramType, "example", "@Param(\"example\")"));
        importedTypes.add(paramType);

        paramType = FullyQualifiedJavaType.getStringInstance();
        ibsmethod.addParameter(new Parameter(paramType, "fields", "@Param(\"fields\")"));

        importedTypes.add(paramType);


        // 设置需要导入的类
        interfaze.addImportedTypes(importedTypes);

        interfaze.addMethod(ibsmethod);
    }

    public void addSelectConditionXml(Document document, IntrospectedTable introspectedTable) {

        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("prefix", "("));
        trimElement.addAttribute(new Attribute("suffix", ")"));
        trimElement.addAttribute(new Attribute("suffixOverrides", ","));

        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", "selectByCondition"));
        selectElement.addAttribute(new Attribute("parameterType", "map"));
        selectElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        selectElement.addElement(new TextElement("select ${fields} from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        selectElement.addElement(new TextElement("<if test=\"example != null\">"));
        selectElement.addElement(new TextElement("  <include refid=\"" + introspectedTable.getMyBatis3UpdateByExampleWhereClauseId() + "\" />"));
        selectElement.addElement(new TextElement("</if>"));

        selectElement.addElement(new TextElement("<if test=\"example.orderByClause != null\">"));
        selectElement.addElement(new TextElement("  order by ${example.orderByClause}"));
        selectElement.addElement(new TextElement("</if>"));

        selectElement.addElement(new TextElement("<if test=\"example.limit != null\">"));
        selectElement.addElement(new TextElement("  <if test=\"example.offset != null\">"));
        selectElement.addElement(new TextElement("      limit ${example.offset}, ${example.limit}"));
        selectElement.addElement(new TextElement("  </if>"));
        selectElement.addElement(new TextElement("  <if test=\"example.offset == null\">"));
        selectElement.addElement(new TextElement("      limit ${example.limit}"));
        selectElement.addElement(new TextElement("  </if>"));
        selectElement.addElement(new TextElement("</if>"));

        document.getRootElement().addElement(selectElement);
    }

}