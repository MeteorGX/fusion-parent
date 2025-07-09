package org.meteorcat.fusion.configuration;

import org.meteorcat.fusion.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;


/**
 * 通用配置功能类
 */
public class Configuration implements
        Serializable, // Java序列化实现
        Cloneable, // Java Clone 对象复制
        //ReadableConfig,  // 自定义的读取配置接口
        WritableConfig // 自定义的写入配置接口

{

    /**
     * 序列化的版本ID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志打印对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);


    /**
     * 加载的配置列表
     */
    protected final HashMap<String, Object> configs;


    /**
     * 将配置序列化之后的附加上的值
     * ordinal() 从 0 - Integer.MAX_VALUE 开始, 所以可以提取内部 int 值作为类型标识
     */
    public enum Type {
        String,
        Integer,
        Long,
        Boolean,
        Float,
        Double,
        Bytes;
    }

    /**
     * 默认无参数初始化构造方法
     */
    public Configuration() {
        this.configs = new HashMap<>();
    }

    /**
     * 带初始化容量的构造方法
     */
    public Configuration(int initialCapacity) {
        this.configs = new HashMap<>(initialCapacity);
    }

    /**
     * 复制其他类初始化
     */
    public Configuration(Configuration other) {
        this.configs = new HashMap<>(other.configs);
    }


    /**
     * 静态复制集合类对象
     */
    public static Configuration fromMap(Map<String, String> map) {
        final Configuration config = new Configuration(map.size());
        map.forEach(config::setString);
        return config;
    }

    /**
     * 获取所有配置Set
     */
    public Set<String> getKeys() {
        synchronized (this.configs) {
            return new HashSet<>(this.configs.keySet());
        }
    }


    /**
     * 设置配置值
     */
    public void setString(String key, String value) {
        setValueInternal(key, value);
    }

    /**
     * 获取配置值
     */
    public String getString(String key, String defaultValue) {
        return getRawValue(key)
                .map(ConfigurationUtils::convertToString)
                .orElse(defaultValue);
    }


    /**
     * Cloneable 需要实现的的对象复制
     */
    @Override
    public Configuration clone() {
        Configuration config = new Configuration(this.configs.size());
        config.addAll(this);
        return config;
    }

    /**
     * 填充配置到内部集合
     * 注: 小心不要 addAll(Configuration other, String prefix) 和 addAll(Configuration other) 同时调用, 会导致线程死锁
     */
    public void addAll(Configuration other, String prefix) {
        final StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        final int pl = builder.length();


        synchronized (this.configs) {
            synchronized (other.configs) {
                for (Map.Entry<String, Object> entry : other.configs.entrySet()) {
                    builder.setLength(pl);
                    builder.append(entry.getKey());
                    this.configs.put(builder.toString(), entry.getValue());
                }
            }
        }
    }

    /**
     * 填充配置到内部集合
     */
    public void addAll(Configuration other) {
        synchronized (this.configs) {
            synchronized (other.configs) {
                this.configs.putAll(other.configs);
            }
        }
    }


    /**
     * 配置项写入内部配置类内部
     */
    @Override
    public <T> Configuration set(ConfigOption<T> option, T value) {
        final boolean canBePrefixMap = ConfigurationUtils.canBePrefixMap(option);
        setValueInternal(option.key(), value, canBePrefixMap);
        return this;
    }

    /**
     * 匹配对象
     */
    public boolean containsKey(String key) {
        synchronized (this.configs) {
            return this.configs.containsKey(key);
        }
    }


    /**
     * 遍历所有配置回调
     */
    private <T> Optional<T> applyWithOption(
            ConfigOption<?> option,
            BiFunction<String, Boolean, Optional<T>> applier
    ) {
        final boolean canBePrefixMap = ConfigurationUtils.canBePrefixMap(option);
        final Optional<T> valueFromExactKey = applier.apply(option.key(), canBePrefixMap);
        if (valueFromExactKey.isPresent()) {
            return valueFromExactKey;
        } else if (option.hasFallbackKeys()) {
            // try the fallback keys
            for (FallbackKey fallbackKey : option.fallbackKeys()) {
                final Optional<T> valueFromFallbackKey =
                        applier.apply(fallbackKey.getKey(), canBePrefixMap);
                if (valueFromFallbackKey.isPresent()) {
                    loggingFallback(fallbackKey, option);
                    return valueFromFallbackKey;
                }
            }
        }
        return Optional.empty();
    }


    /**
     * 配置过时的异常日志记录
     */
    private void loggingFallback(FallbackKey fallbackKey, ConfigOption<?> configOption) {
        if (fallbackKey.isDeprecated()) {
            LOG.warn(
                    "Config uses deprecated configuration key '{}' instead of proper key '{}'",
                    fallbackKey.getKey(),
                    configOption.key());
        } else {
            LOG.info(
                    "Config uses fallback configuration key '{}' instead of key '{}'",
                    fallbackKey.getKey(),
                    configOption.key());
        }
    }


    // 原生类转化 ------------------------------------------------------

    /**
     * 获取配置当中的原生值
     */
    public Optional<Object> getRawValue(String key) {
        return getRawValue(key, false);
    }

    /**
     * 获取匹配KEY的原生值
     */
    public Optional<Object> getRawValue(String key, boolean canBePrefixMap) {
        if (key == null) throw new NullPointerException("Configuration Key not be null.");

        // 注意, 这里要做好线程同步操作, 配置类是会被多线程调用的
        synchronized (this.configs) {
            final Object valueFromExactKey = this.configs.get(key);
            if (!canBePrefixMap || valueFromExactKey != null) {
                return Optional.ofNullable(valueFromExactKey);
            }

            // 匹配出关联配置, 如果匹配到返回对应配置列表
            final Map<String, String> valueFromPrefixMap = ConfigurationUtils.convertToPropertiesPrefixed(configs, key);
            if (valueFromPrefixMap.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(valueFromPrefixMap);
        }
    }


    /**
     * 替换对应配置项的值
     */
    <T> void setValueInternal(String key, T value, boolean canBePrefixMap) {
        if (key == null) throw new NullPointerException("Key not be null.");
        if (value == null) throw new NullPointerException("Value cannot be null.");

        // 保持跨线程安全
        synchronized (this.configs) {
            if (canBePrefixMap) {
                ConfigurationUtils.removePrefixMap(this.configs, key);
            }
            this.configs.put(key, value);
        }
    }

    /**
     * 替换对应单独配置项的值
     */
    <T> void setValueInternal(String key, T value) {
        setValueInternal(key, value, false);
    }

    // ----------------------------------------------------------------


    // 重载系统所需 -----------------------------------------------------

    /**
     * 按照KEY字符串所有列表生成实例化唯一哈希值
     * 注: 这里可以学习下, 用于设计后续包装列表对象的的唯一哈希值生成
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (String s : this.configs.keySet()) {
            hash ^= s.hashCode();
        }
        return hash;
    }

    /**
     * 比较匹配重载功能
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Configuration) {
            // 用来做 Configuration 内部配置比较, 用于处理相同配置项但是不同实例化的情况
            Map<String, Object> otherConfigs = ((Configuration) obj).configs;
            for (Map.Entry<String, Object> entry : otherConfigs.entrySet()) {
                Object thisValue = entry.getValue();
                Object otherValue = otherConfigs.get(entry.getKey());

                if (!thisValue.getClass().equals(byte[].class)) {
                    if (!thisValue.equals(otherValue)) {
                        return false;
                    }
                } else if (otherValue.getClass().equals(byte[].class)) {
                    if (!Arrays.equals((byte[]) thisValue, (byte[]) otherValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 转化字符串
     * 注: 这里 flink采用另外的包装模式, 如果要单独拿出来讲也很麻烦, 所以这里也精简处理
     */
    @Override
    public String toString() {
        return this.configs.toString();
    }

    // ----------------------------------------------------------------


}
