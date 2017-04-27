package de.superioz.moo.api.command;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Wrapper class for {@link Command#usage()}
 */
public class CommandUsage {

    /**
     * The pattern of a valid {@link Command#usage()}
     */
    public static final Pattern USAGE_PATTERN = Pattern.compile("((<|\\[)[a-zA-Z_0-9:()-]+(>|])( )?)+");

    /**
     * This pattern defines a parameter which needs to be passed
     */
    private static final Pattern PARAM_NEEDED = Pattern.compile("<[0-9a-zA-Z_:()-]+>");

    /**
     * The raw command usage
     */
    @Getter
    private String base;

    /**
     * The parameter of the usage
     */
    @Getter
    private List<String> params = new ArrayList<>();

    /**
     * Map of parameter as key and as value if the param is needed
     */
    private Map<String, Boolean> paramMap = new HashMap<>();

    public CommandUsage(String base) {
        this.base = base;

        if(USAGE_PATTERN.matcher(base).matches()) {
            for(String s : base.split(" ")) {
                boolean needed = PARAM_NEEDED.matcher(s).matches();
                s = needed ? s.replaceAll("<|>", "") : s.contains("[") ? s.replaceAll("\\[|]", "") : s;

                this.params.add(s);
                this.paramMap.put(s, needed);
            }
        }
    }

    /**
     * Get the needed size of arguments
     *
     * @return The size as int
     */
    public int getNeededSize() {
        int i = 0;
        for(String s : params) {
            if(paramMap.get(s)) i++;
            else break;
        }
        return i;
    }

    /**
     * Get the parameter at given index
     *
     * @param index The index
     * @return The parameter
     */
    public String getParam(int index) {
        if(index >= params.size() || index < 0) return null;
        return params.get(index);
    }

    /**
     * Checks if the given key is needed by checking the bool inside the paramMap
     * {@literal <}{@literal >} = needed; [] = optional
     *
     * @param key The key
     * @return The result
     */
    public boolean isNeeded(String key) {
        return paramMap.containsKey(key) && paramMap.get(key);
    }

    public boolean isNeeded(int index) {
        String key = getParam(index);
        return key != null && isNeeded(key);
    }

}
