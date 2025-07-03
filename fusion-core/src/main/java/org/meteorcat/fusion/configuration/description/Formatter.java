package org.meteorcat.fusion.configuration.description;

import java.util.EnumSet;

/**
 * 详情格式化工具
 */
public abstract class Formatter {

    /**
     * 字符串构建器
     */
    private final StringBuilder state = new StringBuilder();


    /**
     * Formats the description into a String using format specific tags.
     * 主要的核心函数入口, 详情类内部的数据最后打包输出成文本内容
     *
     * @param description description to be formatted
     * @return string representation of the description
     */
    public String format(Description description) {
        // 提取所有的段落内容移交给内部衍生做文本格式化
        for (BlockElement blockElement : description.getBlocks()) {
            blockElement.format(this);
        }
        // 完成格式化之后重置字符串构建器
        return finalizeFormatting();
    }

    /**
     * 转发 LinkElement 处理
     */
    public void format(LinkElement element) {
        formatLink(state, element.getLink(), element.getText());
    }

    /**
     * 转发 TextElement 处理
     */
    public void format(TextElement element) {
        String[] inlineElements =
                element.getElements().stream()
                        .map(
                                // 这里就是直接递归每行文本内容构建新的字符串之后合并成数组
                                el -> {
                                    Formatter formatter = newInstance();
                                    el.format(formatter);
                                    return formatter.finalizeFormatting();
                                })
                        .toArray(String[]::new);

        // 最后合并成对应的文本格式
        formatText(
                state,
                escapeFormatPlaceholder(element.getFormat()),
                inlineElements,
                element.getStyles());
    }

    /**
     * 转发 LineBreakElement 处理
     */
    public void format(LineBreakElement ignore) {
        formatLineBreak(state);
    }

    /**
     * 转发 ListElement 处理
     */
    public void format(ListElement element) {
        String[] inlineElements =
                element.getEntries().stream()
                        .map(
                                el -> {
                                    Formatter formatter = newInstance();
                                    el.format(formatter);
                                    return formatter.finalizeFormatting();
                                })
                        .toArray(String[]::new);
        formatList(state, inlineElements);
    }

    /**
     * 输出文本内容构建器并重置, 最后返回格式化内容
     */
    private String finalizeFormatting() {
        String result = state.toString();
        state.setLength(0);
        return result.replaceAll("%%", "%");
    }

    /**
     * 要求继承的抽象格式化必须实现 LinkElement 的节点处理(外部连接)
     */
    protected abstract void formatLink(StringBuilder state, String link, String description);

    /**
     * 要求继承的抽象格式化必须实现 LineBreakElement 的节点处理(换行)
     */
    protected abstract void formatLineBreak(StringBuilder state);

    /**
     * 要求继承的抽象格式化必须实现 TextElement 的节点处理(文本)
     */
    protected abstract void formatText(
            StringBuilder state,
            String format,
            String[] elements,
            EnumSet<TextElement.TextStyle> styles);

    /**
     * 要求继承的抽象格式化必须实现 ListElement 的节点处理(多行文本)
     */
    protected abstract void formatList(StringBuilder state, String[] entries);

    /**
     * 获取构建全新实例化对象, 用于迭代遍历所有 Element 的是否生成新段落
     */
    protected abstract Formatter newInstance();


    /**
     * 内部随机占位符内容, 用于一些声明 %s 占位替代
     */
    private static final String TEMPORARY_PLACEHOLDER = "randomPlaceholderForStringFormat";

    /**
     * 对 %s 内容占位符进行检查并且替换
     */
    private static String escapeFormatPlaceholder(String value) {
        return value.replaceAll("%s", TEMPORARY_PLACEHOLDER)
                .replaceAll("%", "%%")
                .replaceAll(TEMPORARY_PLACEHOLDER, "%s");
    }
}
