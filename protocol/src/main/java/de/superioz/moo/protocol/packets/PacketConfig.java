package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This packet is for sending config information across the network<br>
 * This is used to either change the cloud's config or to get information from
 * the cloud's config or to inform other instances about changes.
 *
 * @see Type
 * @see Command
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketConfig extends AbstractPacket {

    /**
     * The command of the packet. Either modifies or fetches an entry
     */
    public Command command;

    /**
     * Type of the config
     */
    public Type type;

    /**
     * Meta informations (can be the new entry data)
     */
    public String meta;

    public PacketConfig(Command command, Type type) {
        this(command, type, "");
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.command = buf.readEnumValue(Command.class);
        this.type = buf.readEnumValue(Type.class);
        this.meta = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(command);
        buf.writeEnumValue(type);
        buf.writeString(meta);
    }

    public enum Command {

        /**
         * Modifies a config entry
         */
        CHANGE,

        /**
         * Informs about a config entry
         */
        INFO

    }

    /**
     * Type of Cloud-Config
     */
    public enum Type {

        ALL,
        MOTD("minecraft.", ""),
        PLAYER_COUNT(0),
        MAX_PLAYERS("minecraft.", 0),
        MAINTENANCE("minecraft.", false),
        MAINTENANCE_MOTD("minecraft.", ""),
        MAINTENANCE_RANK("minecraft.", 0),
        PUNISHMENT_SUBTYPES("minecraft.", new ArrayList<>()),
        PUNISHMENT_REASONS("minecraft.", new ArrayList<>());

        @Getter
        String key;

        Object defaultValue = null;

        Type() {
            key = name().toLowerCase().replace("_", "-");
        }

        Type(Object defaultValue) {
            this();
            this.defaultValue = defaultValue;
        }

        Type(String prefix, Object defaultValue) {
            this(defaultValue);
            this.key = prefix + key;
        }

        public String getDefaultValue() {
            return defaultValue == null ? "null" : defaultValue.toString();
        }

        /**
         * Get the keys of the enum value. If the value is {@link #ALL} then every key will be used
         *
         * @return The list of keys (normally only one value, but with ALL multiple ones)
         */
        public List<Type> getKeys() {
            List<Type> l = new ArrayList<>();

            if(this == ALL) {
                for(Type t : values()) {
                    if(t == ALL) continue;
                    l.add(t);
                }
            }
            else {
                l.add(this);
            }
            return l;
        }


    }

}
