package de.superioz.moo.api;

import de.superioz.moo.api.io.LanguageManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;

public class LanguageManagerTest {

    @Test
    public void testFormatting() {
        LanguageManager languageManager = new LanguageManager(
                new File("C:\\Users\\tobia\\Documents\\3. Minecraft (Server)\\Bungee-Networks\\Newest\\bc\\plugins\\MooThunder")
        );
        languageManager.load(Locale.US);

        System.out.println("File: " + languageManager.getFile().getName());
        System.out.println("Message: " + LanguageManager.get("help-command-not-leaf-entry", 1, 2, 3));
    }

}
