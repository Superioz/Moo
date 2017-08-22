package de.superioz.moo.api.module;

import de.superioz.moo.api.common.RunAsynchronous;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleWaitingTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    void consumerShouldWaitForFinishing() {
        TestModule module = new TestModule();
        ModuleRegistry moduleRegistry = new ModuleRegistry(null);
        moduleRegistry.setService(executorService);

        Assertions.assertTimeout(Duration.ofMillis(2535), new Executable() {
            @Override
            public void execute() throws Throwable {
                moduleRegistry.register(module);
                module.waitFor();
            }
        });
        System.out.println("Done.");
    }

    @RunAsynchronous
    class TestModule extends Module {
        @Override
        public String getName() {
            return "test1";
        }

        @Override
        protected void onEnable() {
            for(int i = 0; i < 50; i++) {
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
