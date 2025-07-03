/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meteorcat.fusion.configuration.description;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置详情信息, 内部关键信息需要元素类记录:
 * - BlockElement: 抽象的通用详情内容块, 提供详情内容文本块
 * - InlineElement: 抽象的通用详情内容, 提供以下元素(Element)实现
 * - TextElement: 实现的单行文本内容信息, 单段详情内容
 * - LineBreakElement：实现的内容换行信息, 就是多段 TextElement 追加换行符号
 * - ListElement: 实现的多行列表内容内容, 单纯的多段 TextElement 列表对象
 * - LinkElement: 实现的附带外部URL的内容信息, 也就是有外部访问连接地址
 * 这其实就代表文档的元素: 1.内容(text), 2.换行(linebreak), 3.段落(block)
 * 以上元素之中, 扩展追加的外部链接资料(link)的功能
 * -------------------------------------------
 * 后续可以思考下, 如果要设计 LicenseElement 该怎么处理? 建议试试自己思考编写
 */
public class Description {

    /**
     * 获取详情的所有段落, 包含有文本|换行等特殊Element节点
     */
    private final List<BlockElement> blocks;

    /**
     * 返回详情内容构建器
     */
    public static DescriptionBuilder builder() {
        return new DescriptionBuilder();
    }

    /**
     * 返回详情的所有段落
     */
    public List<BlockElement> getBlocks() {
        return blocks;
    }


    /**
     * 详情内容构建器
     */
    public static class DescriptionBuilder {

        /**
         * 文本段落容器
         */
        private final List<BlockElement> blocks = new ArrayList<>();

        /**
         * Adds a block of text with placeholders ("%s") that will be replaced with proper string
         * representation of given {@link InlineElement}. For example:
         * 格式化文本节点
         * <p>{@code text("This is a text with a link %s", link("https://somepage", "to here"))}
         *
         * @param format   text with placeholders for elements
         * @param elements elements to be put in the text
         * @return description with added block of text
         */
        public DescriptionBuilder text(String format, InlineElement... elements) {
            blocks.add(TextElement.text(format, elements));
            return this;
        }

        /**
         * Creates a simple block of text.
         * 追加一段文本
         *
         * @param text a simple block of text
         * @return block of text
         */
        public DescriptionBuilder text(String text) {
            blocks.add(TextElement.text(text));
            return this;
        }

        /**
         * Block of description add.
         * 追加文本段落
         *
         * @param block block of description to add
         * @return block of description
         */
        public DescriptionBuilder add(BlockElement block) {
            blocks.add(block);
            return this;
        }

        /**
         * Creates a line break in the description.
         * 追加文本换行
         */
        public DescriptionBuilder linebreak() {
            blocks.add(LineBreakElement.linebreak());
            return this;
        }

        /**
         * Adds a bulleted list to the description.
         * 把文本内容列表追加进来
         */
        public DescriptionBuilder list(InlineElement... elements) {
            blocks.add(ListElement.list(elements));
            return this;
        }

        /**
         * Creates description representation.
         * 构建最终的文本内容详情对象
         */
        public Description build() {
            return new Description(blocks);
        }
    }

    /**
     * 私有化构建方法, 不允许外部实例化
     */
    private Description(List<BlockElement> blocks) {
        this.blocks = blocks;
    }
}
