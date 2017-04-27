package de.superioz.moo.api.database;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.util.Operator;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Just one node of the dbFilter map {@link DbFilter}
 */
@Getter
public class DbFilterNode {

    /**
     * Parent of this node
     */
    @Setter
    private DbFilterNode parent;

    /**
     * List of all childrens
     */
    private List<DbFilterNode> childrens = new ArrayList<>();

    /**
     * Key of the node
     */
    private String key;

    /**
     * Value of the node
     */
    @Setter
    private Object value;

    /**
     * Path as "$1.$2.$?" format
     */
    private String path;

    /**
     * On which layer does the dbFilter exist? (layer = parentsSize + 1)
     */
    private int layer;

    @Setter
    private boolean result = false;

    public DbFilterNode(String key, Object value, String path) {
        this.key = key;
        this.value = value;
        this.path = path;
        this.layer = path.split("\\.").length - 1;
    }

    public DbFilterNode addChildren(DbFilterNode node) {
        childrens.add(node);
        return this;
    }

    public DbFilterNode removeChildren(DbFilterNode node) {
        childrens.remove(node);
        return this;
    }

    public boolean hasChildren() {
        return childrens.size() != 0;
    }

    /**
     * Checks if the node is an operator
     *
     * @return The result
     */
    public boolean isOperator() {
        return key.startsWith("$");
    }

    /**
     * Checks if the node is a leaf of the tree structure
     *
     * @return The result
     */
    public boolean isLeaf() {
        return !(getContent() instanceof JSONObject) && !isOperator();
    }

    /**
     * Get the key of the node within the conditional and not the json structure
     *
     * @return The key as string
     */
    public String getNodeKey() {
        if(isOperator()) {
            return getParent().getKey();
        }
        return getKey();
    }

    /**
     * Get the content of the node within the conditional and not the json structure
     *
     * @return The content as object
     */
    public Object getContent() {
        Object v = value;
        if(value instanceof JSONObject) {
            if(!key.startsWith("$")) {
                JSONObject object = (JSONObject) value;
                v = object.get(object.keySet().toArray(new String[]{})[0]);
            }
        }
        if((v + "").equalsIgnoreCase("null")) return null;
        if(v instanceof String) v = ReflectionUtil.safeCast((String) v);
        return v;
    }

    /**
     * Get the operator of the condition
     *
     * @return The operator
     */
    public Operator getOperator() {
        if(isLeaf() && !isOperator()) {
            if(hasChildren()) {
                return Operator.UNKNOWN;
            }
            return Operator.EQUALS;
        }
        return Operator.fromOperator(getKey());
    }

    /**
     * Resolves all conditions and returns a result
     *
     * @param onCondition The function to determine wether a condition is true or not
     * @return The result
     */
    public boolean resolve(Function<DbFilterNode, Boolean> onCondition) {
        boolean b = false;
        Operator operator = getOperator();

        if(isLeaf()) {
            return result;
        }

        for(int i = 0; i < getChildrens().size(); i++) {
            DbFilterNode child = getChildrens().get(i);

            boolean f;
            if(child.isLeaf()) {
                if(child.hasChildren()) {
                    boolean r = true;
                    for(DbFilterNode node : child.getChildrens()) {
                        r = r & onCondition.apply(node);
                    }
                    child.setResult(r);
                }
                else {
                    child.setResult(onCondition.apply(child));
                }
                f = child.result;
            }
            else {
                f = child.resolve(onCondition);
            }

            if(i == 0) {
                b = f;
            }
            else {
                if(operator == Operator.AND || operator == Operator.UNKNOWN) {
                    b = b && f;
                }
                else if(operator == Operator.OR) {
                    b = b || f;
                }
            }
        }
        return b;
    }

    /**
     * Returns this node as json
     *
     * @return The json string
     */
    public String toJson() {
        return "{\"" + key + "\":" + getValue() + "}";
    }

    @Override
    public String toString() {
        return "ConditionalNode{" +
                "key='" + key + '\'' +
                ", val='" + (!isOperator() ? getContent() : "") + '\'' +
                ", level=" + layer +
                '}';
    }
}
