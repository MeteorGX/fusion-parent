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

package org.meteorcat.fusion.configuration;

import org.meteorcat.fusion.configuration.description.Description;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * 全局配置项, 用于保存配置数据对象
 * 内部 fallback keys 主要用于向后兼容处理
 * 假如老配置是 aaa.bbb.ccc 切换为 ddd.bbb.ccc 的时候, 可以通过配置其别名达到兼容处理
 * <pre>
 * // 这里就是 fallback keys 用来做兼容处理的配置
 * ConfigOption<String> NEW_CONFIG = ConfigOptions.key("new.config.name")
 *     .stringType()
 *     .fallbackKeys("old.config.name")  // 旧键名作为备选
 *     .defaultValue("default");
 * </pre>
 *
 * @param <T>
 */
public class ConfigOption<T> {

    /**
     * 空的备用兼容性配置
     */
    private static final FallbackKey[] EMPTY = new FallbackKey[0];

    /**
     * 配置详情默认内容为空
     */
    static final Description EMPTY_DESCRIPTION = Description.builder().text("").build();


    // ------------------------------------------------------------------------

    /**
     * 当前配置的KEY对象
     */
    private final String key;


    /**
     * 当前配置的默认值对象
     */
    private final T defaultValue;


    /**
     * 当前配置的详细信息
     */
    private final Description description;


    /**
     * 保持兼容性的配置列表
     */
    private final FallbackKey[] fallbackKeys;


    /**
     * 具体的配置匹配类, 这里通过泛型匹配获取到配置类型
     *
     * <ul>
     *   <li>typeClass == atomic class (e.g. {@code Integer.class}) -> {@code ConfigOption<Integer>}
     *   <li>typeClass == {@code Map.class} -> {@code ConfigOption<Map<String, String>>}
     *   <li>typeClass == atomic class and isList == true for {@code ConfigOption<List<Integer>>}
     * </ul>
     */
    private final Class<?> clazz;


    /**
     * 判断配置的内部数据结构是否为列表对象
     */
    private final boolean isList;

    // ------------------------------------------------------------------------

    /**
     * 获取配置当中记录的值类型
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 判断配置当中的值对象是否为列表对象
     */
    public boolean isList() {
        return isList;
    }

    /**
     * Creates a new config option with fallback keys.
     * 创建一个新的配置对象, 如果配置变动过需要追加 fallback key 兼容配置列表
     *
     * @param key          The current key for that config option
     * @param clazz        describes type of the ConfigOption, see description of the clazz field
     * @param description  Description for that option
     * @param defaultValue The default value for this option
     * @param isList       tells if the ConfigOption describes a list option, see description of the clazz
     *                     field
     * @param fallbackKeys The list of fallback keys, in the order to be checked
     */
    ConfigOption(
            String key,
            Class<?> clazz,
            Description description,
            T defaultValue,
            boolean isList,
            FallbackKey... fallbackKeys
    ) {

        // 这里我和 flink 不一样, 官方专门设计 checkNotNull 全局函数用于判断是否空指针, 我这里直接编写
        if (key == null) throw new NullPointerException();
        if (clazz == null) throw new NullPointerException();
        this.key = key;
        this.description = description;
        this.defaultValue = defaultValue;
        this.fallbackKeys = fallbackKeys == null || fallbackKeys.length == 0 ? EMPTY : fallbackKeys;
        this.clazz = clazz;
        this.isList = isList;
    }

    // ------------------------------------------------------------------------

    /**
     * Creates a new config option, using this option's key and default value, and adding the given
     * fallback keys.
     * 创建写入 fallback key 标识的配置项
     *
     * <p>When obtaining a value from the configuration via {@link
     * Configuration#getValue(ConfigOption)}, the fallback keys will be checked in the order
     * provided to this method. The first key for which a value is found will be used - that value
     * will be returned.
     *
     * @param fallbackKeys The fallback keys, in the order in which they should be checked.
     * @return A new config options, with the given fallback keys.
     */
    public ConfigOption<T> withFallbackKeys(String... fallbackKeys) {
        final Stream<FallbackKey> newFallbackKeys =
                Arrays.stream(fallbackKeys).map(FallbackKey::createFallbackKey);
        final Stream<FallbackKey> currentAlternativeKeys = Arrays.stream(this.fallbackKeys);

        // put fallback keys first so that they are prioritized
        final FallbackKey[] mergedAlternativeKeys =
                Stream.concat(newFallbackKeys, currentAlternativeKeys).toArray(FallbackKey[]::new);
        return new ConfigOption<>(
                key, clazz, description, defaultValue, isList, mergedAlternativeKeys);
    }

    /**
     * Creates a new config option, using this option's key and default value, and adding the given
     * deprecated keys.
     * 用于追加过时的配置, 用于保持兼容性配置
     *
     * <p>When obtaining a value from the configuration via {@link
     * Configuration#getValue(ConfigOption)}, the deprecated keys will be checked in the order
     * provided to this method. The first key for which a value is found will be used - that value
     * will be returned.
     *
     * @param deprecatedKeys The deprecated keys, in the order in which they should be checked.
     * @return A new config options, with the given deprecated keys.
     */
    public ConfigOption<T> withDeprecatedKeys(String... deprecatedKeys) {
        final Stream<FallbackKey> currentAlternativeKeys = Arrays.stream(this.fallbackKeys);
        final Stream<FallbackKey> newDeprecatedKeys =
                Arrays.stream(deprecatedKeys).map(FallbackKey::createDeprecatedKey);

        // put deprecated keys last so that they are de-prioritized
        final FallbackKey[] mergedAlternativeKeys =
                Stream.concat(currentAlternativeKeys, newDeprecatedKeys)
                        .toArray(FallbackKey[]::new);
        return new ConfigOption<>(
                key, clazz, description, defaultValue, isList, mergedAlternativeKeys);
    }

    /**
     * Creates a new config option, using this option's key and default value, and adding the given
     * description. The given description is used when generation the configuration documentation.
     * 对配置项追加附带上详情内容
     *
     * @param description The description for this option.
     * @return A new config option, with given description.
     */
    public ConfigOption<T> withDescription(final String description) {
        return withDescription(Description.builder().text(description).build());
    }

    /**
     * Creates a new config option, using this option's key and default value, and adding the given
     * description. The given description is used when generation the configuration documentation.
     *
     * @param description The description for this option.
     * @return A new config option, with given description.
     */
    public ConfigOption<T> withDescription(final Description description) {
        return new ConfigOption<>(key, clazz, description, defaultValue, isList, fallbackKeys);
    }

    // ------------------------------------------------------------------------

    /**
     * 获取配置的Key对象
     *
     * @return The configuration key
     */
    public String key() {
        return key;
    }

    /**
     * 判断配置时候有默认值
     *
     * @return True if it has a default value, false if not.
     */
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    /**
     * 获取默认值对象
     *
     * @return The default value, or null.
     */
    public T defaultValue() {
        return defaultValue;
    }

    /**
     * 判断是否带有向后兼容配置
     *
     * @return True if the option has fallback keys, false if not.
     */
    public boolean hasFallbackKeys() {
        return fallbackKeys != EMPTY;
    }

    /**
     * 获取当前配置之中所有兼容配置项
     *
     * @return The option's fallback keys.
     */
    public Iterable<FallbackKey> fallbackKeys() {
        return (fallbackKeys == EMPTY) ? Collections.emptyList() : Arrays.asList(fallbackKeys);
    }

    /**
     * 获取目前配置当中的详情说明
     *
     * @return The option's description.
     */
    public Description description() {
        return description;
    }

    // ------------------------------------------------------------------------

    /**
     * 重载对象比较功能
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && o.getClass() == ConfigOption.class) {
            ConfigOption<?> that = (ConfigOption<?>) o;
            return this.key.equals(that.key)
                    && Arrays.equals(this.fallbackKeys, that.fallbackKeys)
                    && (this.defaultValue == null
                    ? that.defaultValue == null
                    : (that.defaultValue != null
                    && this.defaultValue.equals(that.defaultValue)));
        } else {
            return false;
        }
    }

    /**
     * 重载HashCode功能
     */
    @Override
    public int hashCode() {
        return 31 * key.hashCode()
                + 17 * Arrays.hashCode(fallbackKeys)
                + (defaultValue != null ? defaultValue.hashCode() : 0);
    }

    /**
     * 重载ToString
     */
    @Override
    public String toString() {
        return String.format(
                "Key: '%s' , default: %s (fallback keys: %s)",
                key, defaultValue, Arrays.toString(fallbackKeys));
    }

}
