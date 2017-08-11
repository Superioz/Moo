package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.DataResolver;
import de.superioz.moo.api.database.DbQuery;
import de.superioz.moo.api.database.DbQueryNode;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.chat.ClickEvent;

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
                        StringUtil.getListToString(MooQueries.getInstance().listGroups(), ", ", group -> group.name))
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
                group -> group.name
        ), StringUtil.prefixed(CommandInstance.PATH, INFO_COMMAND, MODIFY_COMMAND, CREATE_COMMAND, DELETE_COMMAND));
    }

    @Command(label = LABEL, usage = "<subCommand>")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
    }

    @Command(label = LIST_COMMAND, parent = LABEL, usage = "[page]", flags = {"h"})
    public void list(BungeeCommandContext context, ParamSet args) {
        // get the page
        int page = args.getInt(0, 0);

        // get the ordered pageable list and check page
        // if flag 'h' exists order the groups after the rank in descending order
        PageableList<Group> pageableList = new PageableList<>(MooQueries.getInstance().listGroups(),
                (Comparator<Group>) (o1, o2) -> args.hasFlag("h")
                        ? o1.rank.compareTo(o2.rank) * -1
                        : o1.name.compareTo(o2.name));
        context.invalidArgument(pageableList.isEmpty(), LanguageManager.get("group-list-empty"));
        context.invalidArgument(!pageableList.checkPage(page), LanguageManager.get("page-doesnt-exist", page));

        // sends the pageable list with page as list format
        String entryFormat = LanguageManager.get("group-list-entry");
        DisplayFormats.sendPageableList(context, pageableList, page,
                LanguageManager.get("group-list-empty"),
                LanguageManager.get("group-list-header"), LanguageManager.get("group-list-entry-empty"), group -> {
                    String command = "/group info " + group.name;

                    context.sendEventMessage(
                            LanguageManager.format(entryFormat, group.name, command, group.rank, command),
                            ClickEvent.Action.RUN_COMMAND
                    );
                }, () -> {
                    String command = "/group list " + (page + 1);
                    context.sendEventMessage(
                            LanguageManager.get("group-list-next-page", command, command),
                            ClickEvent.Action.RUN_COMMAND
                    );
                });
    }

    @Command(label = INFO_COMMAND, parent = LABEL, usage = "<name>")
    public void info(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, LanguageManager.get("group-doesnt-exist", groupName));

        // send list of all group information
        String entryFormat = LanguageManager.get("group-info-entry");
        String permCommand = "/perm list -g " + groupName;

        // send info
        DisplayFormats.sendList(context,
                LanguageManager.get("group-info-header", groupName),
                context.getFormatSender(entryFormat)
                        .addTranslated("group-info-entry-name", groupName)
                        .addTranslated("group-info-entry-permissions", "&c" + group.permissions.size(), permCommand, permCommand)
                        .addTranslated("group-info-entry-parents", "&c" + group.parents.size(),
                                StringUtil.getListToString(group.parents, "\n", s -> "&8- &7" + s))
                        .addTranslated("group-info-entry-prefix", group.prefix)
                        .addTranslated("group-info-entry-suffix", group.suffix)
                        .addTranslated("group-info-entry-color", group.color)
                        .addTranslated("group-info-entry-tabprefix", group.tabPrefix)
                        .addTranslated("group-info-entry-tabsuffix", group.tabSuffix)
        );
    }

    @Command(label = MODIFY_COMMAND, parent = LABEL, usage = "<name> <updates>")
    public void modify(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, LanguageManager.get("group-doesnt-exist", groupName));

        // get updates (for modification)
        String rawParam = args.get(1);
        DbQuery updates = DbQuery.fromParameter(Group.class, rawParam);

        // execute modification
        context.sendMessage(LanguageManager.get("group-modify-load", groupName));
        ResponseStatus status = MooQueries.getInstance().modifyGroup(groupName, updates);
        context.sendMessage(LanguageManager.get("group-modify-complete", status));
    }

    @Command(label = CREATE_COMMAND, parent = LABEL, usage = "<name> [updates]")
    public void create(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group != null, LanguageManager.get("group-already-exists", groupName));

        // create new group object
        // apply updates (optional)
        group = new Group(groupName);
        if(args.size() > 1) {
            String rawParam = args.get(1);
            DbQuery updates = DbQuery.fromParameter(Group.class, rawParam);
            updates.apply(group);
        }

        // execute creation
        context.sendMessage(LanguageManager.get("group-create-load", groupName));
        ResponseStatus status = MooQueries.getInstance().createGroup(group);
        context.sendMessage(LanguageManager.get("group-create-complete", status));
    }

    @Command(label = DELETE_COMMAND, parent = LABEL, usage = "<name>")
    public void delete(BungeeCommandContext context, ParamSet args) {
        String groupName = args.get(0);
        Group group = args.get(0, Group.class);
        context.invalidArgument(group == null, LanguageManager.get("group-doesnt-exist", groupName));

        // execute deletion
        context.sendMessage(LanguageManager.get("group-delete-load", groupName));
        ResponseStatus status = MooQueries.getInstance().deleteGroup(groupName);
        context.sendMessage(LanguageManager.get("group-delete-complete", status));
    }

}
