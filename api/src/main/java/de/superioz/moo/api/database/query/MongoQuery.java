package de.superioz.moo.api.database.query;

import de.superioz.moo.api.keyvalue.KeyMultiValue;
import de.superioz.moo.api.keyvalue.Keyable;
import de.superioz.moo.api.utils.StringUtil;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.keyvalue.KeyValue;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mongodb query (as {@link Document}).
 */
@NoArgsConstructor
@SuppressWarnings("varargs")
public class MongoQuery {

    /**
     * The map of document with key
     */
    private Map<String, Document> documentMap = new HashMap<>();

    /**
     * Appends an operator with simple pairs
     *
     * @param operator The operator
     * @param pairs    The pairs of key/value for this operator
     * @return This
     */
    public MongoQuery append(Operator operator, Keyable... pairs) {
        String key = "$" + operator.getName();
        Document document = documentMap.containsKey(key) ? documentMap.get(key) : new Document();

        for(Keyable p : pairs) {
            String pKey = p.getKey();

            if(p instanceof KeyMultiValue) {
                for(Object o : ((KeyMultiValue) p).getValues()) {
                    document.append(pKey, o);
                }
            }
            else if(p instanceof KeyValue) {
                document.append(pKey, ((KeyValue) p).getVal());
            }
        }
        documentMap.put(key, document);
        return this;
    }

    /**
     * Builds this query to a document
     *
     * @return The document
     */
    public Document build() {
        Document document = new Document();
        for(String s : documentMap.keySet()) {
            document.append(s, documentMap.get(s));
        }
        return document;
    }

    public enum Operator {

        // field operators
        INC,
        MUL,
        RENAME,
        SET_ON_INSERT,
        SET,
        UNSET,
        MIN,
        MAX,
        CURRENT_DATE,

        // array operators
        ADD_TO_SET,
        POP,
        PULL_ALL,
        PULL,
        PUSH_ALL,
        PUSH;

        /**
         * Gets the name as nice string
         *
         * @return The name
         */
        public String getName() {
            String[] split = name().toLowerCase().split("_");

            if(split.length == 1) {
                return split[0];
            }
            else {
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < split.length; i++) {
                    if(i != 0) {
                        builder.append(StringUtil.upperFirstLetter(split[i]));
                    }
                    else {
                        builder.append(split[i]);
                    }
                }
                return builder.toString();
            }
        }

    }

}
