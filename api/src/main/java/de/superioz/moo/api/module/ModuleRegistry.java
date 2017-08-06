package de.superioz.moo.api.module;

import com.google.common.base.Strings;
import de.superioz.moo.api.exceptions.InvalidConfigException;
import de.superioz.moo.api.logging.Loogger;
import de.superioz.moo.api.utils.NumberUtil;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

public class ModuleRegistry {

    /**
     * The thread-safe map for modules (key = name)
     */
    private final ConcurrentMap<String, Module> moduleByName = new ConcurrentHashMap<>();

    /**
     * The executor service for async execution
     */
    @Setter @Getter
    private ExecutorService service;

    @Getter
    private Loogger logger;

    public ModuleRegistry(Loogger logger) {
        this.logger = logger;
    }

    /**
     * Sends a module summary (similar to the Maven Reactor Summary)
     */
    public void sendModuleSummary() {
        if(logger == null) return;
        logger.info(Strings.repeat("-", 75));
        logger.info("Module Summary: ");
        logger.info(" ");

        // for averages
        double successCount = 0;
        double totalTime = 0;

        for(Module m : getModules()) {
            String name = Strings.padEnd(m.getName() + " ", 25, '.');
            String state = m.isEnabled() ? "SUCCESS" : "FAILURE";
            double time = (double) m.getEnableTime() / 1000;

            // averages
            if(m.isEnabled()) successCount++;
            totalTime += time;

            String timeString = "[ " + Strings.padStart(StringUtil.applyDecimalLength(time, 3),
                    6, ' ') + " s]";
            String error = m.isEnabled() ? "" : " ("
                    + (m.getErrorReason() != null ? m.getErrorReason().getClass().getSimpleName() : "Not Finished Yet")
                    + ")";
            logger.info(name + " " + state + " " + timeString);
        }
        logger.info(" ");
        logger.info("Success rate: " + NumberUtil.round(successCount / getModules().size() * 100, 4) + "% " +
                "| Average time: " + NumberUtil.round(totalTime / getModules().size(), 4) + "s");
        logger.info(Strings.repeat("-", 75));
    }

    public void sendModuleSummaryAsync() {
        if(service != null) {
            service.execute(() -> {
                for(Module m : getModules()) {
                    while(!m.isEnabled()){
                        try {
                            Thread.sleep(5);
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sendModuleSummary();
            });
        }
        else {
            sendModuleSummary();
        }
    }

    /**
     * Disables all modules
     */
    public void disableAll() {
        moduleByName.forEach((s, module) -> module.disable());
        moduleByName.clear();
    }

    /**
     * Checks if module with given label is enabled
     *
     * @param dependency The annotation
     * @return The result
     */
    private boolean checkDependency(ModuleDependency dependency) {
        String[] modules = dependency.modules();
        boolean r = false;

        for(String module : modules) {
            r = contains(module) && get(module).isEnabled();
        }
        return r;
    }

    public boolean checkDependency(Class<?> clazz) {
        return !clazz.isAnnotationPresent(ModuleDependency.class)
                || checkDependency(clazz.getAnnotation(ModuleDependency.class));
    }

    public boolean checkDependency(Method method) {
        return method.isAnnotationPresent(ModuleDependency.class)
                && checkDependency(method.getAnnotation(ModuleDependency.class));
    }

    /**
     * Gets all modules from {@link #moduleByName} map<br>
     * The list will be sorted by the time the module started
     *
     * @return The list of modules
     */
    public List<Module> getModules() {
        List<Module> moduleList = new ArrayList<>(moduleByName.values());
        moduleList.sort(Comparator.comparingLong(Module::getTimeStarted));

        return moduleList;
    }

    /**
     * Gets the module with given label
     *
     * @param label The label
     * @return The result
     */
    public <T extends Module> T get(String label) {
        return (T) moduleByName.get(label);
    }

    /**
     * Checks if the map contains a module with given label
     *
     * @param label The label
     * @return The result
     */
    public boolean contains(String label) {
        return moduleByName.containsKey(label);
    }

    /**
     * Registers given modules to the map
     *
     * @param modules The modules
     */
    public <T extends Module> T register(boolean autoEnable, Module... modules) {
        List<Module> moduleList = new ArrayList<>(Arrays.asList(modules));

        // sort modules after order of dependencies
        // (should work, but with looped dependencies probably not :/)
        moduleList.sort((o1, o2) -> o1.getDependencies().contains(o2.getName()) ? -1 :
                o2.getDependencies().contains(o1.getName()) ? +1 : 0);

        for(Module m : modules) {
            // is the module already enabled
            // AND is auto enable activated?
            if(!m.isEnabled() && autoEnable) {
                if(m.isRunningAsync() && getService() != null) {
                    m.setFuture(getService().submit(() -> enableModule(m)));

                    // wait for module to be finished
                    /*if(m.isRunningAsync() && getService() != null) {
                        synchronized(m) {
                            try {
                                m.wait();
                            }
                            catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }*/
                }
                else {
                    enableModule(m);
                }
            }

            // put module to map
            moduleByName.put(m.getName(), m);
        }
        if(modules.length > 0) {
            return (T) modules[0];
        }
        return null;
    }

    public <T extends Module> T register(Module... modules) {
        return register(true, modules);
    }

    /**
     * Unregisters module with given label
     *
     * @param label The label
     */
    public void unregister(String label) {
        moduleByName.remove(label);
    }

    /**
     * Enables a module
     *
     * @param m The module
     */
    private Module enableModule(Module m) {
        Throwable error = null;

        try {
            m.enable(this);
        }
        catch(Exception e) {
            error = e;
        }

        if(error != null) {
            // couldn't load module
            m.finished(false);
            m.setErrorReason(error);

            logger.debug("Couldn't load module '" + m.getName() + "': "
                    + error.getMessage() + " (:" + error.getClass().getSimpleName() + ")");
            if(!(error instanceof InvalidConfigException)) {
                error.printStackTrace();
            }
        }

        return m;
    }

}
