package cn.bridgeli.mybatis.plugin.dom.formatter;

import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Document;

/**
 * 格式化XML 将tab 变为4空格长度，
 */
public class BridgeLiXmlFormatter extends DefaultXmlFormatter {
    /**
     * 通过 plugin 无法修改 dom 的 attr 或者 文本，Mybatis 不支持这种方式
     * 折衷方案是通过格式化的时候将其文本改变，这里使用一个更简便的方案，但不通用
     */
    @Override
    public String getFormattedContent(Document document) {
        String content = document.getFormattedContent();
        try {
            if ("true".equalsIgnoreCase(context.getProperty("suppressColumnType"))) {
                content = content.replaceAll("[, ]?jdbcType=\"?[A-Z]+\"?", "");
            }
            content = content.replaceAll("  ", "    ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }
}
