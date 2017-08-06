package de.superioz.moo.api.module;

import de.superioz.moo.api.common.RunAsynchronous;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleWaitingTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void consumerShouldWaitForFinishing() {
        TestModule module = new TestModule();
        ModuleRegistry moduleRegistry = new ModuleRegistry(null);
        moduleRegistry.setService(executorService);

        moduleRegistry.register(module);
        module.waitFor();
        System.out.println("Done.");
    }

    @RunAsynchronous
    public class TestModule extends Module {
        @Override
        public String getName() {
            return "test1";
        }

        @Override
        protected void onEnable() {
            for(int i = 0; i < 50; i++) {
                System.out.println("System.out: " + i);
                try {
                    Thread.sleep(50L);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onDisable() {
            //
        }
    }


}
