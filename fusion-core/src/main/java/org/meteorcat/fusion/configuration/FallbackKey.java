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

/**
 * 兼容性的配置Key配置, 用来标识配置是变动修改还是已过时的配置
 * 配置类当中基本会遇到的情况: 1.配置KEY名称变动 2.配置已经过时不再支持
 * <pre>{@code
 *     // 使用方法
 *     FallbackKey fallbackKey = FallbackKey.createFallbackKey("server.hostname");
 *     System.out.println(fallbackKey);
 * }</pre>
 */
public class FallbackKey {

    // -------------------------
    //  工厂方法
    // -------------------------

    /**
     * 静态构建变动的配置KEY
     */
    static FallbackKey createFallbackKey(String key) {
        return new FallbackKey(key, false);
    }

    /**
     * 静态构建过时的配置KEY
     */
    static FallbackKey createDeprecatedKey(String key) {
        return new FallbackKey(key, true);
    }


    // ------------------------------------------------------------------------


    /**
     * 修改重载的配置KEY
     */
    private final String key;

    /**
     * 配置KEY是否已经过时淘汰
     */
    private final boolean isDeprecated;

    /**
     * 获取重载配置KEY
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取是否配置已经过时
     */
    public boolean isDeprecated() {
        return isDeprecated;
    }

    /**
     * 私有化配置构造
     */
    private FallbackKey(String key, boolean isDeprecated) {
        this.key = key;
        this.isDeprecated = isDeprecated;
    }

    /**
     * 重载比较方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && o.getClass() == FallbackKey.class) {
            FallbackKey that = (FallbackKey) o;
            return this.key.equals(that.key) && (this.isDeprecated == that.isDeprecated);
        } else {
            return false;
        }
    }

    /**
     * 重载HashCode方法
     */
    @Override
    public int hashCode() {
        return 31 * key.hashCode() + (isDeprecated ? 1 : 0);
    }

    /**
     * 重载ToString方法
     */
    @Override
    public String toString() {
        return String.format("{key=%s, isDeprecated=%s}", key, isDeprecated);
    }
}
