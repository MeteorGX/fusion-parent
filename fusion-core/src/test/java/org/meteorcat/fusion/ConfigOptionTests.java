package org.meteorcat.fusion;


import org.junit.Test;
import org.meteorcat.fusion.configuration.ConfigOption;
import org.meteorcat.fusion.configuration.ConfigOptions;

import java.util.List;

/**
 * 配置项的测试单元
 */
public class ConfigOptionTests {


    /**
     * 文本配置
     */
    @Test
    public void createStringOptions() {
        ConfigOption<String> tempDirs = ConfigOptions
                .key("tmp.dir")
                .stringType()
                .defaultValue("/tmp");
        System.out.println(tempDirs);
    }

    /**
     * 数值配置Integer
     */
    @Test
    public void createIntegerOptions() {
        ConfigOption<Integer> parallelism = ConfigOptions
                .key("application.parallelism")
                .intType()
                .defaultValue(100);
        System.out.println(parallelism);
    }

    /**
     * 数值配置列表Integer, 注: 官方文档没更新, 所以这里可能不太一样
     */
    @Test
    public void createIntegersOptions() {
        ConfigOption<List<Integer>> parallelism = ConfigOptions
                .key("application.ports")
                .intType()
                .asList()
                .defaultValues(8000, 8001, 8002);
        System.out.println(parallelism);
    }


    /**
     * 数值配置Boolean
     */
    @Test
    public void createBooleanOptions() {
        ConfigOption<Boolean> allowWebSocket = ConfigOptions
                .key("service.websocket.enabled")
                .booleanType()
                .defaultValue(true);
        System.out.println(allowWebSocket);
    }


    /**
     * 数值配置Double, 附带配置变动匹配
     */
    @Test
    public void createDoubleOptions() {
        ConfigOption<Double> threshold = ConfigOptions
                .key("cpu.utilization.threshold")
                .doubleType()
                .defaultValue(0.9)
                .withDeprecatedKeys("cpu.threshold");
        System.out.println(threshold);
    }

}
