package de.superioz.moo.protocol.common;

import lombok.Getter;
import de.superioz.moo.api.database.object.DataArchitecture;
import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.protocol.exception.MooInputException;
import de.superioz.moo.protocol.packets.PacketRespond;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Response {

    /**
     * The pattern for a deserialized object inside the response
     */
    private static final Pattern DESERIALIZED_PATTERN = Pattern.compile("[^{}¶Þþ]*([¶Þþ][^{}¶Þþ]*)*");

    /**
     * The handle of the response (inherited from a packet)
     */
    @Getter
    private PacketRespond handle;

    /**
     * The header title of the packet (e.g. "databaseInfo")
     */
    @Getter
    private String header;

    /**
     * The response status of the task from the packet. Similar to http(s) responses
     */
    @Getter
    private ResponseStatus status;

    /**
     * The message of the {@link #handle} as list
     */
    private List<String> message;

    /**
     * The element map (of complex ones) if the message has already been converted
     */
    @Getter
    private Map<Class, List> complexElementMap = new HashMap<>();

    /**
     * The element map (of primitive ones) if the message has already been converted
     */
    @Getter
    private Map<Class, List> primitiveElementMap = new HashMap<>();

    public Response(PacketRespond respond) {
        if(respond == null) return;
        this.handle = respond;
        this.header = respond.header;
        this.status = respond.status;
        this.message = respond.message;
    }

    public Response(ResponseStatus status) {
        this(new PacketRespond(status));
    }

    public Response(List<String> msg) {
        this(new PacketRespond("", msg, ResponseStatus.OK));
    }

    /**
     * Checks the state
     *
     * @throws MooInputException If the state is not OK
     */
    public void checkState() throws MooInputException {
        if(status.isCritically()) {
            throw new IllegalStateException("Received critical state: " + status);
        }
        if(!isOk()) throw new MooInputException(this);
    }

    /**
     * Checks if the status is OK
     *
     * @return The result as boolean
     */
    public boolean isOk() {
        return getStatus() == ResponseStatus.OK;
    }

    /**
     * Checks if the status is NBOK
     *
     * @return The result as boolean
     */
    public boolean isNotOk() {
        return getStatus() == ResponseStatus.NOK;
    }

    /**
     * Gets the message as list
     *
     * @return The list of strings
     */
    public List<String> getMessageAsList() {
        if(message == null || message.isEmpty()) return new ArrayList<>();
        return message;
    }

    /**
     * Gets the message of the response
     *
     * @return The message
     */
    public String getMessage() {
        if(message.isEmpty()) return "";
        return message.get(0);
    }

    /**
     * Gets the string from the response with given index
     *
     * @param index The index
     * @return The string
     */
    public String get(int index) {
        List<String> l = getMessageAsList();
        if(index < 0 || index >= l.size()) return null;
        return l.get(index);
    }

    /**
     * Converts the list of messages into a list of elements of given class
     * This method is only for complex classes like {@link PlayerData}<br>
     * <p>
     * A really common pitfall is using the class for the same index of the respond list for this method,
     * because this method counts different.
     * e.g.: Respond list is: PlayerData, Group, PlayerData
     * Means the index is:    0           0      1
     *
     * @param eClass The element's class
     * @param <E>    The element type
     * @return The list
     */
    public <E> List<E> toComplexes(Class<E> eClass) throws MooInputException {
        this.checkState();
        if(complexElementMap.containsKey(eClass)) return complexElementMap.get(eClass);

        List<E> l = new ArrayList<>();
        DataArchitecture architecture = DataArchitecture.fromClass(eClass);
        DataResolver dataResolver = new DataResolver(architecture);

        for(String msg : getMessageAsList()) {
            E e;
            if(DESERIALIZED_PATTERN.matcher(msg).matches()) {
                e = ReflectionUtil.deserialize(msg, eClass);
            }
            else {
                // get the document from the message
                // create object from this
                Document document = Document.parse(msg);

                e = dataResolver.doc(document).complete(eClass);
            }
            if(e != null) l.add(e);
        }
        complexElementMap.put(eClass, l);
        return l;
    }

    /**
     * @see #toComplexes(Class)
     */
    public <E> E toComplex(Class<E> eClass, int index) throws MooInputException {
        List<E> l = toComplexes(eClass);
        if(l.isEmpty()) return null;
        return l.get(index);
    }

    /**
     * @see #toComplexes(Class)
     */
    public <E> E toComplex(Class<E> eClass) throws MooInputException {
        return toComplex(eClass, 0);
    }

    /**
     * Casts the response message into a list of given eClass's objects
     * This method is only for primitive types like {@link Integer}
     *
     * @param eClass The element class
     * @param <E>    The element type
     * @return The list of elements
     */
    public <E> List<E> toPrimitives(Class<E> eClass) throws MooInputException {
        this.checkState();
        if(primitiveElementMap.containsKey(eClass)) return primitiveElementMap.get(eClass);
        List<E> l = new ArrayList<>();

        for(String msg : getMessageAsList()) {
            Object o = ReflectionUtil.safeCast(msg);

            if(o != null && eClass.isAssignableFrom(o.getClass())) {
                l.add((E) o);
            }
        }
        primitiveElementMap.put(eClass, l);
        return l;
    }

    public <E> E toPrimitive(Class<E> eClass) throws MooInputException {
        this.checkState();
        List<E> l = toPrimitives(eClass);
        if(l.isEmpty()) return null;
        return l.get(0);
    }

}
