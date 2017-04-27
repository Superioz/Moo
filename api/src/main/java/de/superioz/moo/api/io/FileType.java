package de.superioz.moo.api.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {

    YAML("yml"),
    TEXT("txt"),
    PROPERTIES("properties"),
    XML("xml"),
    JSON("json");

    private String name;

}
