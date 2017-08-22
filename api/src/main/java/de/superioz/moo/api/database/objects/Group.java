package de.superioz.moo.api.database.objects;

import de.superioz.moo.api.logging.ConsoleColor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.object.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import de.superioz.moo.api.util.Validation;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Group extends SimpleSerializable {

    public static final String DEFAULT_NAME = "default";

    /**
     * Name of the group
     */
    @DbKey
    public String name;

    /**
     * Rank of the group. Higher group means more important. It's like a hirarchy. Yup.
     */
    @DbKey
    public Integer rank = 0;

    /**
     * Permissions of the group
     *
     * @see Validation#PERMISSION
     */
    @DbKey
    public List<String> permissions = new ArrayList<>();

    /**
     * Parents of the group. That means which groups does this group inherit
     */
    @DbKey
    public List<String> parents = new ArrayList<>();

    /**
     * Prefix of the group (Can be used in any context)
     */
    @DbKey
    public String prefix = "";

    /**
     * Suffix of the group (Can be used in any context)
     */
    @DbKey
    public String suffix = "";

    /**
     * Color of the group<br>
     * {@link ConsoleColor} is similar to this color format
     */
    @DbKey
    public String color = "";

    /**
     * Other prefix, but only used in the tablist
     */
    @DbKey
    public String tabPrefix = "";

    /**
     * Other suffix, but only used in the tablist
     */
    @DbKey
    public String tabSuffix = "";

    /**
     * Checks if the group is the default group (rank = 0)
     *
     * @return The result
     */
    public boolean isDefault() {
        return rank == 0;
    }

    //
    public Group(String name) {
        this.name = name;
    }

    public Group(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }
}
