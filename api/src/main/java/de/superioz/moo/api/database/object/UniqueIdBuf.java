package de.superioz.moo.api.database.object;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class UniqueIdBuf extends SimpleSerializable {

    /**
     * Name of the player
     */
    @DbKey
    public String name;

    /**
     * Unique Id of the player
     */
    @DbKey
    public UUID uuid;

    /**
     * The texture value (skin)
     */
    @DbKey(key = "textures.textureValue")
    public String textureValue;

    /**
     * The texture signature (skin)
     */
    @DbKey(key = "textures.textureSignature")
    public String textureSignature;

}
