package de.superioz.moo.api.exceptions;

import de.superioz.moo.api.module.Module;
import lombok.Getter;

public class ModuleInitializeException extends RuntimeException {

    @Getter
    private Module module;

    public ModuleInitializeException(String message, Module module) {
        super(message);
        this.module = module;
    }
}
