package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;
import com.yy.hawk4j.core.plugin.PluginRuntime;
import com.yy.hawk4j.core.plugin.TaskAwarePlugin;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.task.TaskDecorator;

import java.util.ArrayList;
import java.util.List;

/**
 * @方法描述：装饰器插件对象，只有这个插件对象需要用户自己向里面添加真正的装饰器对象，任务装饰器对象也是用户自己定义的
 */
public class TaskDecoratorPlugin implements TaskAwarePlugin {

    public static final String PLUGIN_NAME = "task-decorator-plugin";

    /**
     * Decorators
     */
    @Getter
    private final List<TaskDecorator> decorators = new ArrayList<>();

    /**
     * Get id.
     *
     * @return id
     */
    @Override
    public String getId() {
        return PLUGIN_NAME;
    }

    /**
     * Add a decorator.
     *
     * @param decorator decorator
     */
    public void addDecorator(@NonNull TaskDecorator decorator) {
        decorators.remove(decorator);
        decorators.add(decorator);
    }

    /**
     * Clear all decorators.
     */
    public void clearDecorators() {
        decorators.clear();
    }

    /**
     * Remove decorators.
     */
    public void removeDecorator(TaskDecorator decorator) {
        decorators.remove(decorator);
    }

    /**
     * Callback when task is executed.
     *
     * @param runnable runnable
     * @return tasks to be execute
     * @see ExtensibleThreadPoolExecutor#execute
     */
    @Override
    public Runnable beforeTaskExecute(Runnable runnable) {
        for (TaskDecorator decorator : decorators) {
            runnable = decorator.decorate(runnable);
        }
        return runnable;
    }

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    @Override
    public PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId())
                .addInfo("decorators", decorators);
    }

}
