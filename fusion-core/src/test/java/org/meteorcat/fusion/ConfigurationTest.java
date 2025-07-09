package org.meteorcat.fusion;

import org.junit.Test;
import org.meteorcat.fusion.configuration.ConfigOption;
import org.meteorcat.fusion.configuration.Configuration;
import org.meteorcat.fusion.util.ConfigurationUtils;

import java.util.HashMap;

/**
 * 配置测试单元
 */
public class ConfigurationTest {

    /**
     * 写入单项配置到内部配置集合
     */
    @Test
    public void writeConfigOption() {
        // 可以忽略 No SLF4J providers were found 错误, 因为想没有直接配置 SLF4J 日志对象绑定
        Configuration config = new Configuration();
        ConfigOption<Boolean> enabled = ConfigurationUtils.getBooleanConfigOption("fs.search.enabled");
        config.set(enabled, true);
        assert config.containsKey("fs.search.enabled");
        System.out.println(config);
    }


    /**
     * 列表对象转化为配置
     */
    @Test
    public void writeConfigOptions() {
        Configuration configuration = Configuration.fromMap(new HashMap<>() {{
            put("fs.search.enabled", "true");
            put("net.hostname", "localhost");
            put("net.port", "8080");
        }});
        assert configuration.getKeys().size() == 3;
        System.out.println(configuration);
    }

}
