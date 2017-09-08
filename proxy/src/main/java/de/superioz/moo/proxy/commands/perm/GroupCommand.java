package de.superioz.moo.proxy.commands.group;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.database.query.DbQueryNode;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.minecraft.chat.formats.InfoListFormat;
import de.superioz.moo.minecraft.chat.formats.PageableListFormat;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

@RunAsynchronous
public class GroupCommand {

    private static final String LABEL = "group";
    private static final String INFO_COMMAND = "info";
    private static final String LIST_COMMAND = "list";
    private static final String MODIFY_COMMAND = "modify";
    private static final String CREATE_COMMAND = "create";
    private static final String DELETE_COMMAND = "delete";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        // subcommands
        helper.react(0, Collections.singletonList(
                LanguageManager.get("available-subcommands",
                        StringUtil.getListToString(helper.getContext().getCommand().getChildrens(), ", ",
                                CommandInstance::getLabel))
        ), LABEL);

        // groups
        helper.react(0, Collections.singletonList(
                LanguageManager.get("available-groups",
                        StringUtil.getListToString(MooQueries.getInstance().listGroups(), ", ", Group::getName))
        ), INFO_COMMAND, MODIFY_COMMAND, DELETE_COMMAND);

        // update syntax
        helper.react(1, Arrays.asList(
                LanguageManager.get("key-value-updates-syntax"),
                LanguageManager.get("available-fields",
                        StringUtil.getListToString(DataResolver.getResolvableFields(Group.class), ", ",
                                field -> "&f" + field.getName() + "&7")),
                LanguageManager.get("available-operators",
                        StringUtil.getListToString(DbQueryNode.Type.values(), ", ",
                                operator -> "&f" + operator.toString() + "&7"))
        ), MODIFY_COMMAND, CREATE_COMMAND);
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        // subcommands
        completor.reactSubCommands(LABEL);

        // groups
        completor.react(2, StringUtil.getStringList(MooQueries.getInstance().listGroups(),
                Group::getName
        ), StringUtil.prefixed(CommandInstance.PATH, INFO_COMMAND, MODIFY_COMMAND, CREATE_COMMAND, DELETE_COMMAND));
    }

    @Command(label = LABEL, usage = "<subCommand>")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
    }

    @Command(label = LIST_COMMAND, parent = LABEL, usage = "[page]", flags = {"h"})
    public void list(BungeeCommandContext context, ParamSet args) {
        // list the page
        int page = args.getInt(0, 0);

        // list the ordered pageable list and check page
        // if flag 'h' exists order the groups after the rank in descending order
        PageableList<Group> pageableList = new PageableList<>(MooQueries.getInstance().listGroups(),
                (Comparator<Group>) (o1, o2) -> args.hasFlag("h")
                        ? o1.getRank().compareTo(o2.getRank()) * -1
                        : o1.getName().compareTo(o2.getName()));

        // sends the pageable list with page as list format
        context.sendDisplayFormat(new PageableListFormat<Group>(pageableList)
                .page(page).doesntExist("error-page-doesnt-exist")
                .emptyList("group-list-empty").header("group-list-header").emptyEntry("group-list-entry-empty")
                .entry("group-list-entry").entry(replacor -> replacor.accept(replacor.get().getName(), replacor.get().getRank()))
                .footer("group-list-next-page", page + 1)
        );
    }

    @Command(label = INFO_COMMAND, parent = LABEL, usage = "<name>")
    public void info(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, LanguageManager.get("group-doesnt-exist", groupName));

        // send info
        context.sendDisplayFormat(new InfoListFormat().header("group-info-header", groupName).entryFormat("group-info-entry")
                .entry("group-info-entry-name", groupName)
                .entryc("group-info-entry-permissions", group.getPermissions().size() != 0,
                        group.getPermissions().size(), groupName)
                .entryc("group-info-entry-parents", group.getParents().size() != 0,
                        group.getParents().size(), StringUtil.getListToString(group.getParents(), "\n", s -> "&8- &7" + s))
                .entry("group-info-entry-prefix", group.getPrefix())
                .entry("group-info-entry-suffix", group.getSuffix())
                .entry("group-info-entry-color", group.getColor())
                .entry("group-info-entry-tabprefix", group.getTabPrefix())
                .entry("group-info-entry-tabsuffix", group.getTabSuffix())
        );
    }

    @Command(label = MODIFY_COMMAND, parent = LABEL, usage = "<name> <updates>")
    public void modify(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, "group-doesnt-exist", groupName);

        // list updates (for modification)
        String rawParam = args.get(1);
        DbQuery updates = DbQuery.fromParameter(Group.class, rawParam);

        // execute modification
        context.sendMessage("group-modify-load", groupName);
        ResponseStatus status = MooQueries.getInstance().modifyGroup(groupName, updates);
        context.sendMessage("group-modify-complete", status);
    }

    @Command(label = CREATE_COMMAND, parent = LABEL, usage = "<name> [updates]")
    public void create(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group != null, "group-already-exists", groupName);

        // create new group object
        // apply updates (optional)
        group = new Group(groupName);
        if(args.size() > 1) {
            String rawParam = args.get(1);
            DbQuery updates = DbQuery.fromParameter(Group.class, rawParam);
            updates.apply(group);
        }

        // execute creation
        context.sendMessage("group-create-load", groupName);
        ResponseStatus status = MooQueries.getInstance().createGroup(group);
        context.sendMessage("group-create-complete", status);
    }

    @Command(label = DELETE_COMMAND, parent = LABEL, usage = "<name>")
    public void delete(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, "group-doesnt-exist", groupName);

        // execute deletion
        context.sendMessage("group-delete-load", groupName);
        ResponseStatus status = MooQueries.getInstance().deleteGroup(groupName);
        context.sendMessage("group-delete-complete", status);
    }

}
