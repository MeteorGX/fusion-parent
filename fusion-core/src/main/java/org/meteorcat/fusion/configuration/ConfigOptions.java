package org.meteorcat.fusion.configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 构建 ConfigOption 配置
 * <pre>{@code
 * // simple string-valued option with a default value
 * ConfigOption<String> tempDirs = ConfigOptions
 *     .key("tmp.dir")
 *     .stringType()
 *     .defaultValue("/tmp");
 *
 * // simple integer-valued option with a default value
 * ConfigOption<Integer> parallelism = ConfigOptions
 *     .key("application.parallelism")
 *     .intType()
 *     .defaultValue(100);
 *
 * // option of list of integers with a default value
 * ConfigOption<Integer> parallelism = ConfigOptions
 *     .key("application.ports")
 *     .intType()
 *     .asList()
 *     .defaultValue(8000, 8001, 8002);
 *
 * // option with no default value
 * ConfigOption<String> userName = ConfigOptions
 *     .key("user.name")
 *     .stringType()
 *     .noDefaultValue();
 *
 * // option with deprecated keys to check
 * ConfigOption<Double> threshold = ConfigOptions
 *     .key("cpu.utilization.threshold")
 *     .doubleType()
 *     .defaultValue(0.9)
 *     .withDeprecatedKeys("cpu.threshold");
 * }</pre>
 */
public class ConfigOptions {

    /**
     * 禁止实例化
     */
    private ConfigOptions() {
    }

    /**
     * 静态生成初始化配置构建器
     */
    public static OptionBuilder key(String key) {
        if (key == null) throw new NullPointerException();
        return new OptionBuilder(key);
    }

    /**
     * 配置 Builder
     */
    public static final class OptionBuilder {

        /**
         * 配置KEY名称
         */
        @SuppressWarnings("FieldCanBeLocal")
        private final String key;

        /**
         * 构造方法, 注意: 只允许同级包被实例化
         */
        OptionBuilder(String key) {
            this.key = key;
        }


        /**
         * 默认的强类型映射
         */
        @SuppressWarnings("unchecked")
        private static final Class<Map<String, String>> PROPERTIES_MAP_CLASS =
                (Class<Map<String, String>>) (Class<?>) Map.class;


        /**
         * 包装强类型 Boolean Builder
         */
        public TypedConfigOptionBuilder<Boolean> booleanType() {
            return new TypedConfigOptionBuilder<>(key, Boolean.class);
        }

        /**
         * 包装强类型 Integer Builder
         */
        public TypedConfigOptionBuilder<Integer> intType() {
            return new TypedConfigOptionBuilder<>(key, Integer.class);
        }

        /**
         * 包装强类型 Long Builder
         */
        public TypedConfigOptionBuilder<Long> longType() {
            return new TypedConfigOptionBuilder<>(key, Long.class);
        }

        /**
         * 包装强类型 Float Builder
         */
        public TypedConfigOptionBuilder<Float> floatType() {
            return new TypedConfigOptionBuilder<>(key, Float.class);
        }

        /**
         * 包装强类型 Double Builder
         */
        public TypedConfigOptionBuilder<Double> doubleType() {
            return new TypedConfigOptionBuilder<>(key, Double.class);
        }

        /**
         * 包装强类型 String Builder
         */
        public TypedConfigOptionBuilder<String> stringType() {
            return new TypedConfigOptionBuilder<>(key, String.class);
        }

        /**
         * 包装强类型 Duration Builder
         */
        public TypedConfigOptionBuilder<Duration> durationType() {
            return new TypedConfigOptionBuilder<>(key, Duration.class);
        }

        /**
         * 包装强类型 Enum Builder
         */
        public <T extends Enum<T>> TypedConfigOptionBuilder<T> enumType(Class<T> enumType) {
            return new TypedConfigOptionBuilder<>(key, enumType);
        }

        /**
         * 包装强类型 Map Builder
         */
        public TypedConfigOptionBuilder<Map<String, String>> mapType() {
            return new TypedConfigOptionBuilder<>(key, PROPERTIES_MAP_CLASS);
        }
    }

    /**
     * 通用强类型配置 Builder
     */
    public static class TypedConfigOptionBuilder<T> {
        /**
         * 强类型的配置KEY名称
         */
        private final String key;

        /**
         * 强类型的配置保存的原生类型
         */
        private final Class<T> clazz;

        /**
         * 只允许同级包内部初始化
         */
        TypedConfigOptionBuilder(String key, Class<T> clazz) {
            this.key = key;
            this.clazz = clazz;
        }

        /**
         * 生成带默认值的配置值
         */
        public ConfigOption<T> defaultValue(T value) {
            return new ConfigOption<>(key, clazz, ConfigOption.EMPTY_DESCRIPTION, value, false);
        }


        /**
         * 生成不带默认值的配置值
         */
        public ConfigOption<T> noDefaultValue() {
            return new ConfigOption<>(key, clazz, ConfigOption.EMPTY_DESCRIPTION, null, true);
        }


        /**
         * 生成列表的配置项
         */
        public ListConfigOptionBuilder<T> asList() {
            return new ListConfigOptionBuilder<>(key, clazz);
        }
    }

    /**
     * 列表对象配置
     */
    public static class ListConfigOptionBuilder<E> {
        private final String key;
        private final Class<E> clazz;

        /**
         * 不允许外部初始化实例化
         */
        ListConfigOptionBuilder(String key, Class<E> clazz) {
            this.key = key;
            this.clazz = clazz;
        }

        /**
         * 构建带默认值的列表配置项
         */
        @SafeVarargs
        public final ConfigOption<List<E>> defaultValues(E... values) {
            return new ConfigOption<>(
                    key,
                    clazz,
                    ConfigOption.EMPTY_DESCRIPTION,
                    Arrays.asList(values),
                    true
            );
        }


        /**
         * 构建不含默认值的列表配置项
         */
        public ConfigOption<List<E>> noDefaultValues() {
            return new ConfigOption<>(key, clazz, ConfigOption.EMPTY_DESCRIPTION, null, true);
        }
    }

}
