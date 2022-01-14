package cn.bridgeli.mybatis.plugin;

import cn.bridgeli.mybatis.plugin.util.DateUtil;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellRunner;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.PropertyRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 为 mapper 添加注释，修改 mapperExt 的生成方式，为 model 添加一些东西
 */
public class BridgeLiMysqlClientGeneratorPlugin extends PluginAdapter {

    private static String XMLFILE_POSTFIX = "Ext";

    private static String JAVAFILE_POTFIX = "Ext";

    private static String SQLMAP_COMMON_POTFIX = "and IS_DELETED = '0'";

    private static String ANNOTATION_RESOURCE = "javax.annotation.Resource";

    private static String MAPPER_EXT_HINT = "<!-- 扩展自动生成或自定义的SQl语句写在此文件中 -->";

    /**
     * 重写该方法主要为默认mapper添加注释
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addModelClassComment(interfaze, introspectedTable, false);
        return true;
    }

    /**
     * 为类（model mapper 都通用）添加注释
     */
    private void addModelClassComment(Interface topLevelClass, IntrospectedTable introspectedTable, boolean isExt) {
        StringBuilder sb = new StringBuilder();

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" *");

        if (!isExt) {
            sb.append(" * 表: ");
            sb.append(introspectedTable.getFullyQualifiedTable());
            sb.append(" 的 mapper 类");
        } else {
            String name = topLevelClass.getType().getShortName();
            sb.append(" * ").append(name.substring(name.indexOf("Ext")));
            sb.append(" 的扩展 mapper 接口");
        }
        topLevelClass.addJavaDocLine(sb.toString());

        topLevelClass.addJavaDocLine(" * ");

        String author = context.getProperty("author");
        author = author == null ? "BridgeLi" : author;

        topLevelClass.addJavaDocLine(" * @author " + author);
        topLevelClass.addJavaDocLine(" * @date " + DateUtil.date2Str(new Date()));
        topLevelClass.addJavaDocLine(" */");
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {

        return true;
    }

    /**
     * 添加 xxxMapperExt.java
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType() + JAVAFILE_POTFIX);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(interfaze);

        FullyQualifiedJavaType baseInterfaze = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        interfaze.addSuperInterface(baseInterfaze);

        // 添加注释
        addModelClassComment(interfaze, introspectedTable, true);

        FullyQualifiedJavaType annotation = new FullyQualifiedJavaType(ANNOTATION_RESOURCE);
        interfaze.addAnnotation("@Resource");
        interfaze.addImportedType(annotation);

        CompilationUnit compilationUnits = interfaze;
        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(compilationUnits, context.getJavaModelGeneratorConfiguration().getTargetProject(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());

        if (isExistExtFile(generatedJavaFile.getTargetProject(), generatedJavaFile.getTargetPackage(), generatedJavaFile.getFileName())) {
            return super.contextGenerateAdditionalJavaFiles(introspectedTable);
        }
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<GeneratedJavaFile>(1);
        generatedJavaFile.getFileName();
        generatedJavaFiles.add(generatedJavaFile);
        return generatedJavaFiles;
    }

    /**
     * 生成xxxExt.xml
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        String[] splitFile = introspectedTable.getMyBatis3XmlMapperFileName().split("\\.");
        String fileNameExt = null;
        if (splitFile[0] != null) {
            fileNameExt = splitFile[0] + XMLFILE_POSTFIX + ".xml";
        }

        if (isExistExtFile(context.getSqlMapGeneratorConfiguration().getTargetProject(), introspectedTable.getMyBatis3XmlMapperPackage(), fileNameExt)) {
            return super.contextGenerateAdditionalXmlFiles(introspectedTable);
        }

        Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID, XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);

        XmlElement root = new XmlElement("mapper");
        document.setRootElement(root);
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace() + XMLFILE_POSTFIX;
        root.addAttribute(new Attribute("namespace", namespace));
        root.addElement(new TextElement(MAPPER_EXT_HINT));

        GeneratedXmlFile gxf = new GeneratedXmlFile(document, fileNameExt, introspectedTable.getMyBatis3XmlMapperPackage(), context.getSqlMapGeneratorConfiguration().getTargetProject(), false, context.getXmlFormatter());

        List<GeneratedXmlFile> answer = new ArrayList<>(1);
        answer.add(gxf);

        return answer;
    }

    /**
     * 添删改默认 mapper.xml 中的sql语句及属性
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement parentElement = document.getRootElement();
        updateDocumentNameSpace(introspectedTable, parentElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 修改 gmt_create, modifier 等的查改语句
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<Element> elements = element.getElements();
        XmlElement setItem = null;
        int modifierItemIndex = -1;
        int gmtModifiedItemIndex = -1;
        boolean needIsDeleted = false;
        XmlElement gmtCreatedEle = null;
        XmlElement creatorEle = null;
        for (Element e : elements) {
            if (e instanceof XmlElement) {
                setItem = (XmlElement) e;
                for (int i = 0; i < setItem.getElements().size(); i++) {
                    XmlElement xmlElement = (XmlElement) setItem.getElements().get(i);
                    for (Attribute att : xmlElement.getAttributes()) {
                        if (att.getValue().equals("modifier != null")) {
                            modifierItemIndex = i;
                            break;
                        }
                        if (att.getValue().equals("gmtModified != null")) {
                            gmtModifiedItemIndex = i;
                            break;
                        }
                        if (att.getValue().equals("isDeleted != null")) {
                            needIsDeleted = true;
                            break;
                        }
                        if (att.getValue().equals("gmtCreated != null")) {
                            gmtCreatedEle = xmlElement;
                            break;
                        }
                        if (att.getValue().equals("creator != null")) {
                            creatorEle = xmlElement;
                            break;
                        }
                    }
                }
            }
        }

        if (setItem != null) {
            if (modifierItemIndex != -1) {
                addModifierXmlElement(setItem, modifierItemIndex);
            }
            if (gmtModifiedItemIndex != -1) {
                addGmtModifiedXmlElement(setItem, gmtModifiedItemIndex);
            }
            if (gmtCreatedEle != null) {
                setItem.getElements().remove(gmtCreatedEle);
            }
            if (creatorEle != null) {
                setItem.getElements().remove(creatorEle);
            }
        }
        if (needIsDeleted) {
            TextElement text = new TextElement(SQLMAP_COMMON_POTFIX);
            element.addElement(text);
        }

        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    private void updateDocumentNameSpace(IntrospectedTable introspectedTable, XmlElement parentElement) {
        Attribute namespaceAttribute = null;
        for (Attribute attribute : parentElement.getAttributes()) {
            if (attribute.getName().equals("namespace")) {
                namespaceAttribute = attribute;
            }
        }
        parentElement.getAttributes().remove(namespaceAttribute);
        parentElement.getAttributes().add(new Attribute("namespace", introspectedTable.getMyBatis3JavaMapperType()));
    }

    private void addGmtModifiedXmlElement(XmlElement setItem, int gmtModifiedItemIndex) {
        XmlElement defaultGmtModified = new XmlElement("if");
        defaultGmtModified.addAttribute(new Attribute("test", "gmtModified == null"));
        defaultGmtModified.addElement(new TextElement("GMT_MODIFIED = NOW(),"));
        setItem.getElements().add(gmtModifiedItemIndex + 1, defaultGmtModified);
    }

    private void addModifierXmlElement(XmlElement setItem, int modifierItemIndex) {
        XmlElement defaultmodifier = new XmlElement("if");
        defaultmodifier.addAttribute(new Attribute("test", "modifier == null"));
        defaultmodifier.addElement(new TextElement("MODIFIER = 'SYSTEM',"));
        setItem.getElements().add(modifierItemIndex + 1, defaultmodifier);
    }

    private boolean isExistExtFile(String targetProject, String targetPackage, String fileName) {
        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                return true;
            }
        }

        File testFile = new File(directory, fileName);
        if (testFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This plugin is always valid - no properties are required
     */
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    public static void main(String[] args) {
        String config = BridgeLiMysqlClientGeneratorPlugin.class.getClassLoader().getResource("generatorConfig.xml").getFile();
        String[] arg = {"-configfile", config};
        ShellRunner.main(arg);
    }
}
