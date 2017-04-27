package de.superioz.moo.api.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.exceptions.ModuleInitializeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Module {

    /**
     * Enabled/Disabled state of the module
     */
    private boolean enabled = false;

    /**
     * When does the module attempted to start?
     */
    @Getter
    private long timeStarted;

    /**
     * When does the module finished starting?
     */
    @Getter(value = AccessLevel.PRIVATE)
    private long timeFinished;

    /**
     * If the module couldn't be started, here is the reason
     */
    @Setter
    private Throwable errorReason;

    public abstract String getName();

    protected abstract void onEnable();

    /**
     * Returns the difference between the finished stamp and the attempting to start stamp
     *
     * @return The difference as long (ms)
     */
    public long getEnableTime() {
        if(timeFinished < timeStarted) {
            // wtf? that shouldn't happen, just return zero ..
            return 0;
        }
        return timeFinished - timeStarted;
    }

    /**
     * Finished the onEnable ..
     */
    protected void finished(boolean result) {
        this.enabled = result;
        this.timeFinished = System.currentTimeMillis();

        synchronized(this) {
            this.notifyAll();
        }
    }

    /**
     * Check for the dependencies to be enabled
     *
     * @return The result
     */
    public boolean checkDependencies(ModuleRegistry registry) {
        return registry.checkDependency(getClass());
    }

    /**
     * Get the dependencies of this module
     *
     * @return The dependencies as array
     */
    public List<String> getDependencies() {
        if(getClass().isAnnotationPresent(ModuleDependency.class)) {
            return new ArrayList<>(Arrays.asList(getClass().getAnnotation(ModuleDependency.class).modules()));
        }
        return new ArrayList<>();
    }

    /**
     * Checks if this class should be enabled async
     *
     * @return The result
     */
    public boolean isRunningAsync() {
        return getClass().isAnnotationPresent(RunAsynchronous.class);
    }

    /**
     * Enables this module
     */
    public void enable(ModuleRegistry registry) {
        this.timeStarted = System.currentTimeMillis();
        if(!checkDependencies(registry)) {
            throw new ModuleInitializeException("Couldn't resolve modules dependencies!", this);
        }
        this.onEnable();
        this.finished(true);
    }

    /**
     * Disables this module
     */
    public void disable() {
        this.onDisable();
        this.enabled = false;
    }

    protected abstract void onDisable();

}
