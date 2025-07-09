package org.meteorcat.fusion.util;

import org.meteorcat.fusion.configuration.ConfigOption;
import org.meteorcat.fusion.configuration.ConfigOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration工具集
 */
public class ConfigurationUtils {
    /**
     * 不允许实例化
     */
    private ConfigurationUtils() { /* 不允许实例化 */}


    // 构建配置项目 -------------------------------------

    /**
     * 获取初始化配置值项: Boolean
     */
    public static ConfigOption<Boolean> getBooleanConfigOption(String key) {
        return ConfigOptions.key(key).booleanType().noDefaultValue();
    }

    /**
     * 获取初始化配置值项: Double
     */
    public static ConfigOption<Double> getDoubleConfigOption(String key) {
        return ConfigOptions.key(key).doubleType().noDefaultValue();
    }

    /**
     * 获取初始化配置值项: Float
     */
    public static ConfigOption<Float> getFloatConfigOption(String key) {
        return ConfigOptions.key(key).floatType().noDefaultValue();
    }

    /**
     * 获取初始化配置值项: Integer
     */
    public static ConfigOption<Integer> getIntegerConfigOption(String key) {
        return ConfigOptions.key(key).intType().noDefaultValue();
    }

    /**
     * 获取初始化配置值项: Long
     */
    public static ConfigOption<Long> getLongConfigOption(String key) {
        return ConfigOptions.key(key).longType().noDefaultValue();
    }

    // -------------------------------------------------------


    // 配置匹配增删改查匹配 -------------------------------------

    /**
     * 用于比较判断配置是否为列表对象
     */
    public static boolean canBePrefixMap(ConfigOption<?> option) {
        return option.getClazz() == Map.class && !option.isList();
    }


    /**
     * 用来比较配置相似度
     * <pre>
     * // 比如一下配置都是 avro-confluent.properties. 开头, 那么应该都算是同种类配置:
     *  avro-confluent.properties.schema = 1
     *  avro-confluent.properties.other-prop = 2
     * // 都是归属于 avro-confluent.properties 配置, 所以能够被一下判断符合种类
     * filterPrefixMapKey("avro-confluent.properties.schema","avro-confluent.properties")
     * </pre>
     */
    public static boolean filterPrefixMapKey(String key, String candidate) {
        final String prefix = key + ".";
        return candidate.startsWith(prefix);
    }

    /**
     * 对整个Map元素做比较, 判断某个Key配置是否在其中
     */
    public static boolean containsPrefixMap(Map<String, Object> configs, String key) {
        return configs.keySet().stream().anyMatch(candidate -> filterPrefixMapKey(key, candidate));
    }


    // -------------------------------------------------------


    // 配置匹配类型, 将 Object 还原成原生功能 -------------------


    /**
     * 转化为枚举对象
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<?>> E convertToEnum(Object o, Class<E> clazz) {
        if (o.getClass().equals(clazz)) {
            return (E) o;
        }
        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> e.toString()
                        .toLowerCase(Locale.ROOT)
                        .equals(o.toString().toUpperCase(Locale.ROOT))
                ).findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(
                                "Could not parse value for enum %s. Expected one of: [%s]",
                                clazz, Arrays.toString(clazz.getEnumConstants()))
                ));
    }


    /**
     * 强制转化为字符串
     * 注: 这里我和官方不一致, 官方内部内部嵌套 YAML 类格式化转化; 额外扩展出来又是很多知识点, 所以这里做精简处理
     */
    public static String convertToString(Object o) {
        if (o.getClass() == String.class) {
            return (String) o;
        } else {
            throw new ClassCastException("Convert to String failed: " + o.getClass().getName());
        }
    }


    /**
     * 强制转化为Integer
     */
    public static Integer convertToInteger(Object o) {
        if (o.getClass() == Integer.class) {
            return (Integer) o;
        } else if (o.getClass() == Long.class) {
            long v = (Long) o;
            if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
                return (int) v;
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "Configuration value %s overflows/underflow the integer type.",
                                v));
            }
        }
        return Integer.parseInt(o.toString());
    }

    /**
     * 强制转化为Long
     */
    public static Long convertToLong(Object o) {
        if (o.getClass() == Long.class) {
            return (Long) o;
        } else if (o.getClass() == Integer.class) {
            return ((Integer) o).longValue();
        }
        return Long.parseLong(o.toString());
    }

    /**
     * 强制转化为Float
     */
    public static Float convertToFloat(Object o) {
        if (o.getClass() == Float.class) {
            return (Float) o;
        } else if (o.getClass() == Double.class) {
            double v = (Double) o;
            if (v == 0.0
                    || (v >= Float.MIN_VALUE && v <= Float.MAX_VALUE)
                    || (v >= -Float.MAX_VALUE && v <= -Float.MIN_VALUE)) {
                return (float) v;
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "Configuration value %s overflows/underflow the float type.",
                                v));
            }
        }
        return Float.parseFloat(o.toString());
    }

    /**
     * 强制转化为Double
     */
    public static Double convertToDouble(Object o) {
        if (o.getClass() == Double.class) {
            return (Double) o;
        } else if (o.getClass() == Float.class) {
            return ((Float) o).doubleValue();
        }
        return Double.parseDouble(o.toString());
    }


    /**
     * 强制转化为Double
     * 注: 这里采用了 Java17 的 switch 匹配方法
     */
    public static Boolean convertToBoolean(Object o) {
        if (o.getClass() == Boolean.class) {
            return (Boolean) o;
        }
        return switch (o.toString().toUpperCase()) {
            case "TRUE" -> true;
            case "FALSE" -> false;
            default -> throw new IllegalArgumentException(
                    String.format(
                            "Unrecognized option for boolean: %s. Expected either true or false(case insensitive)",
                            o));
        };
    }


    /**
     * 把指定配置KEY的值还原成原生 String 类型
     */
    public static Map<String, String> convertToPropertiesPrefixed(
            Map<String, Object> configs,
            String key
    ) {
        final String prefixKey = key + ".";
        return configs.keySet().stream()
                .filter(k -> k.startsWith(prefixKey))
                .collect(Collectors.toMap(
                        k -> k.substring(prefixKey.length()),
                        k -> convertToString(configs.get(k))
                ));
    }


    /**
     * 匹配关联的配置并且删除
     */
    public static boolean removePrefixMap(Map<String, Object> configs, String key) {
        // 提取配置当中相似KEY并构成列表
        final List<String> prefixKeys = configs
                .keySet()
                .stream()
                .filter(candidate -> filterPrefixMapKey(key, candidate))
                .toList();

        // 删除关联的KEY, 并确认匹配满足删除条件
        prefixKeys.forEach(configs::remove);
        return !prefixKeys.isEmpty();
    }

    // -------------------------------------------------------

}
