package com.yy.hawk4j.core.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin runtime.
 */
@RequiredArgsConstructor
@Getter
public class PluginRuntime {

    /**
     * Plugin id
     */
    private final String pluginId;

    /**
     * Runtime info
     */
    private final List<Info> infoList = new ArrayList<>();

    /**
     * Add a runtime info item.
     *
     * @param name  name
     * @param value value
     * @return runtime info item
     */
    public PluginRuntime addInfo(String name, Object value) {
        infoList.add(new Info(name, value));
        return this;
    }


    /**
     * Plugin runtime info.
     */
    @RequiredArgsConstructor
    @Getter
    public static class Info {

        /**
         * Name
         */
        private final String name;

        /**
         * Value
         */
        private final Object value;
    }

}
