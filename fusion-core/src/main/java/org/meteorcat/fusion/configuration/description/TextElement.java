package org.meteorcat.fusion.configuration.description;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * 文本节点实现
 * 注意: 涉及到单行文本+多行文本的实现必须要继承 BlockElement+InlineElement, 如果是相对简单的内容可以直接声明 InlineElement
 */
public class TextElement implements BlockElement, InlineElement {

    private final String format;
    private final List<InlineElement> elements;
    private final EnumSet<TextStyle> textStyles = EnumSet.noneOf(TextStyle.class);

    /**
     * Creates a block of text with placeholders ("%s") that will be replaced with proper string
     * representation of given {@link InlineElement}. For example:
     * 创建支持 %s 格式化的文本
     *
     * <p>{@code text("This is a text with a link %s", link("https://somepage", "to here"))}
     *
     * @param format   text with placeholders for elements
     * @param elements elements to be put in the text
     * @return block of text
     */
    public static TextElement text(String format, InlineElement... elements) {
        return new TextElement(format, Arrays.asList(elements));
    }

    /**
     * Creates a simple block of text.
     * 创建简单的单行文本
     *
     * @param text a simple block of text
     * @return block of text
     */
    public static TextElement text(String text) {
        return new TextElement(text, Collections.emptyList());
    }

    /**
     * Wraps a list of {@link InlineElement}s into a single {@link TextElement}.
     * 对文本的 %s 占位符进行批量替换
     */
    public static InlineElement wrap(InlineElement... elements) {
        return text(StringUtils.repeat("%s", elements.length), elements);
    }

    /**
     * Creates a block of text formatted as code.
     * 这里其实就是声明文本追加 <code></code> 自定义标签
     *
     * @param text a block of text that will be formatted as code
     * @return block of text formatted as code
     */
    public static TextElement code(String text) {
        TextElement element = text(text);
        element.textStyles.add(TextStyle.CODE);
        return element;
    }

    /**
     * 获取格式化文本
     */
    public String getFormat() {
        return format;
    }

    /**
     * 获取文本段落列表内容
     */
    public List<InlineElement> getElements() {
        return elements;
    }

    /**
     * 获取文本的风格
     */
    public EnumSet<TextStyle> getStyles() {
        return textStyles;
    }

    /**
     * 私有构建方法
     */
    private TextElement(String format, List<InlineElement> elements) {
        this.format = format;
        this.elements = elements;
    }

    /**
     * 重载格式化处理, 其实就是转发到 Formatter 让其帮忙格式化输出文本
     */
    @Override
    public void format(Formatter formatter) {
        formatter.format(this);
    }

    /**
     * Styles that can be applied to {@link TextElement} e.g. code, bold etc.
     * 声明文本详情的风格, 这里其实就是参考 HTML 风格的 <code></code> 节点, 后续如果想要追加更多外部节点可以手动扩展
     */
    public enum TextStyle {
        CODE
    }
}
