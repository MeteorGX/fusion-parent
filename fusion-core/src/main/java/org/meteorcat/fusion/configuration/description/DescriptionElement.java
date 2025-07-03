package org.meteorcat.fusion.configuration.description;

/**
 * 格式化详情的抽象, 用于传递格式化类进行详情格式化
 */
public interface DescriptionElement {
    /**
     * 核心的格式化处理
     */
    void format(Formatter formatter);
}

