package de.superioz.moo.api.module;

import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.exceptions.ModuleInitializeException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * With this Module you can split up program parts so that the code looks clearer.
 * Also, you can use {@link ModuleDependency} and {@link RunAsynchronous} to modify the
 * enabling behaviour. Remember, if the module runs async no other module can depend on it via
 * the {@link ModuleDependency}, because the dependency system is only for determining the order
 * of enabling the modules.
 */
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

    /**
     * Future only if started async
     */
    @Getter @Setter
    private Future<? extends Module> future;

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
     * Waiting for the module to be finished.
     *
     * @return The module afterr finished
     */
    public <M extends Module> M waitFor() {
        if(future == null) return (M) this;
        synchronized(this) {
            try {
                wait(3000L);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (M) this;
    }

    public <M extends Module> void waitFor(Consumer<M> onFinished) {
        waitFor();
        onFinished.accept((M) this);
    }

    public <M extends Module> void waitForAsync(Consumer<M> onFinished) {
        new Thread(() -> onFinished.accept(waitFor())).start();
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
