package de.superioz.moo.protocol.packet;

import de.superioz.moo.protocol.Protocol;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketKeepalive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.protocol.events.PacketQueueEvent;
import de.superioz.moo.protocol.packets.PacketRespond;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

/**
 * Abstract class of a netty packet which can be sent across the network
 */
@Getter
@NoArgsConstructor
public abstract class AbstractPacket {

    /**
     * The regex of one part of the json representation<br>
     *
     * @see #toString()
     */
    public static final String JSON_PART_REGEX = "\"[a-zA-Z]*\": (\"[^\"]*\"|\\[[^}]*]|[^\",}]*)";

    /**
     * The version of the {@link Protocol}
     */
    @Setter
    protected int protocolVersion = 0;

    /**
     * The id of the packet inside the protocol<br>
     * e.g.: {@link Protocol#KEEPALIVE} would be 0
     */
    @Setter
    protected int protocolId = -1;

    /**
     * The unique query id (to determine request-response system)
     */
    @Setter
    protected UUID queryUid;

    /**
     * The timestamp of the time of the sending
     */
    @Setter
    protected long stamp = -1;

    /**
     * The channel of the packet being
     */
    @Setter
    protected Channel channel;

    /**
     * The byte buf (originally the raw form of the packet)
     */
    @Setter
    protected ByteBuf buf;

    /**
     * The interception (for packet simulation)
     */
    protected Consumer<AbstractPacket> interception;

    /**
     * If the packet has already been responded
     */
    private boolean responded = false;

    public void interceptRespond(Consumer<AbstractPacket> interception) {
        this.interception = interception;
    }

    /**
     * Converts given bytebuffer into this packets
     *
     * @param buf The byte buffer
     * @throws IOException If something goes wrong lel
     */
    public abstract void read(PacketBuffer buf) throws IOException;

    /**
     * Converts this packets into given bytebuffer
     *
     * @param buf The byte buffer
     * @throws IOException If something goes wrong lel
     */
    public abstract void write(PacketBuffer buf) throws IOException;

    /**
     * Respond to the packets
     *
     * @param packet    The packets to send as respond
     * @param callbacks The callbacks?
     */
    public void respond(AbstractPacket packet, Consumer<AbstractPacket>... callbacks) {
        if(!responded) responded = true;
        packet.setQueryUid(getQueryUid());

        if(interception != null) {
            interception.accept(packet);
            return;
        }
        EventExecutor.getInstance().execute(new PacketQueueEvent(getChannel(), packet, callbacks));
    }

    public void respond(List<String> messages, Consumer<AbstractPacket>... callbacks) {
        this.respond(new PacketRespond(getName().toLowerCase(), messages, ResponseStatus.OK), callbacks);
    }

    public void respond(String message, Consumer<AbstractPacket>... callbacks) {
        this.respond(Arrays.asList(message), callbacks);
    }

    public final void respond(ResponseStatus status) {
        this.respond(new PacketRespond(getName().toLowerCase(), "", status));
    }

    /**
     * Copies this packet
     *
     * @param <T> .
     * @return .
     */
    public <T extends AbstractPacket> T deepCopy() {
        Object instance = ReflectionUtil.getInstance(getClass());
        if(instance == null) return (T) this;

        Field[] fields = ReflectionUtil.getFieldsNonStatic(instance.getClass()).toArray(new Field[]{});
        for(int i = 0; i < fields.length; i++) {
            Field current = fields[i];

            if(!Modifier.isStatic(current.getModifiers())) {
                ReflectionUtil.setFieldObject(i, instance, ReflectionUtil.getFieldObject(i, this));
            }
        }

        ((AbstractPacket) instance).setQueryUid(null);
        ((AbstractPacket) instance).setStamp(0L);
        return (T)(instance == null ? this : instance);
    }

    /**
     * Gets the name of the class (the packets)
     *
     * @return The name as string
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get the address of the channel
     *
     * @return The address
     */
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) getChannel().remoteAddress();
    }

    /**
     * Gets a packet instance (with class {@code pClass}) from json representation {@code s}<br>
     * Opposite of {@link #toString()}
     *
     * @param pClass The class of the abstract packet (e.g. {@link PacketKeepalive})
     * @param s      The json representation of the packet
     * @param <P>    The type
     * @return The instance
     * @see #JSON_PART_REGEX
     * @see #toString()
     */
    public static <P extends AbstractPacket> P fromString(Class<P> pClass, String s) {
        Map<String, String> keyFieldMap = new HashMap<>();

        // find parts of key-value pairs inside the string
        for(String found : StringUtil.find(JSON_PART_REGEX, s)) {
            if(found.contains("{")) continue;
            found = found.replace("}", "");

            String[] spl = found.split(": ", 2);
            keyFieldMap.put(spl[0].replace("\"", ""), spl[1].replace("\"", ""));
        }

        // list the new instance
        P object = (P) ReflectionUtil.getInstance(pClass);
        if(object == null) return null;

        // loop through map and apply them onto the instance
        keyFieldMap.forEach((s1, s2) -> {
            Field f = ReflectionUtil.getFieldRecursively(s1, pClass);
            if(f == null) return;

            // No need for safe cast if the value is definitely a string
            // and replace the json-string-indicators
            boolean isString = s2.startsWith("\"") && s2.endsWith("\"");
            if(isString) s2 = s2.replaceAll("\\{|}", "");

            // safe cast or taking the value
            // and set the field object
            Object value = isString ? s2 : ReflectionUtil.safeCast(s2, f);
            ReflectionUtil.setFieldObject(f, object, value);
        });
        return object;
    }

    /**
     * Overridden method toString which converts the packets's content into a json string
     *
     * @return The json formatted string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<String> content = new ArrayList<>();

        // print json format with common values
        builder.append("{\n");
        builder.append(StringUtil.getJsonPart("protocolVersion", protocolVersion)).append(", \n");
        builder.append(StringUtil.getJsonPart("protocolId", protocolId)).append(", \n");
        builder.append(StringUtil.getJsonPart("queryUid", queryUid)).append(", \n");
        builder.append(StringUtil.getJsonPart("stamp", stamp)).append(", \n");

        // print payload which is packet specific
        builder.append(StringUtil.getJsonKey("payload")).append("{\n");
        for(Field field : ReflectionUtil.getFieldsNonStatic(getClass())) {
            if(Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                content.add(StringUtil.getJsonPart(field.getName(), field.get(this)));
            }
            catch(IllegalAccessException e) {
                //
            }
        }
        builder.append(StringUtil.join(", \n", content.toArray()));

        // finish the string
        builder.append("}").append("}");
        return builder.toString().replace("\n", "").replace("\t", "");
    }

}
