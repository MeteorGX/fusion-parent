package org.meteorcat.fusion;


import org.junit.Test;
import org.meteorcat.fusion.configuration.description.Description;
import org.meteorcat.fusion.configuration.description.Formatter;
import org.meteorcat.fusion.configuration.description.LinkElement;
import org.meteorcat.fusion.configuration.description.TextElement;

import java.util.EnumSet;

/**
 * 详情内容构建器测试
 */
public class DescriptionTests {

    /**
     * 生成文本节点
     */
    @Test
    public void textElement() {
        // 生成构建器
        final String text = "Hello World!";
        final Description.DescriptionBuilder builder = Description.builder();

        // 创建文本节点
        builder.text(text);// 其实内部是做 blocks.add(TextElement.text(text)) 添加节点

        // 上面的其实等效于下面语法, 其实就是追加 list 内容
        builder.add(TextElement.text("oh!!!!!"));

        // 最后生成详情对象
        Description description = builder.build();
        assert !description.getBlocks().isEmpty();
    }

    /**
     * 衍生的文档格式化工具类
     */
    public static class HtmlFormatter extends Formatter {

        @Override
        protected void formatLink(StringBuilder state, String link, String description) {
            state.append(String.format("<a href=\"%s\">%s</a>", link, description));
        }

        @Override
        protected void formatLineBreak(StringBuilder state) {
            state.append("<br />");
        }

        @Override
        protected void formatText(
                StringBuilder state,
                String format,
                String[] elements,
                EnumSet<TextElement.TextStyle> styles) {
            String escapedFormat = escapeCharacters(format);

            String prefix = "";
            String suffix = "";
            if (styles.contains(TextElement.TextStyle.CODE)) {
                prefix = "<code class=\"highlighter-rouge\">";
                suffix = "</code>";
            }
            state.append(prefix);
            state.append(String.format(escapedFormat, (Object) elements));
            state.append(suffix);
        }

        @Override
        protected void formatList(StringBuilder state, String[] entries) {
            state.append("<ul>");
            for (String entry : entries) {
                state.append(String.format("<li>%s</li>", entry));
            }
            state.append("</ul>");
        }

        @Override
        protected Formatter newInstance() {
            return new HtmlFormatter();
        }

        private static String escapeCharacters(String value) {
            return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
    }

    /**
     * 节点格式化输出文档
     */
    @Test
    public void textFormat() {
        // 生成节点对象
        final Description.DescriptionBuilder builder = Description.builder();
        builder.add(TextElement.text("Hello"));
        builder.add(TextElement.text("World"));
        builder.add(TextElement.text("!"));
        builder.linebreak(); // 换行
        builder.list(LinkElement.link("https://www.meterorcat.net", "meteorcat"));
        final Description description = builder.build();

        // 输出所有详情文档
        final HtmlFormatter formatter = new HtmlFormatter();
        final String htmlContent = formatter.format(description);

        // 这里主要是打印看下内容效果而不采用断言
        System.out.println(htmlContent);
    }
}
