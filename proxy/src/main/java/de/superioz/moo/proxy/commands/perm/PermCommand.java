package de.superioz.moo.proxy.commands.perm;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.GroupPermission;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.console.format.PageableListFormat;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.PermissionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.command.BungeeParamSet;

import java.util.Collections;
import java.util.List;

@RunAsynchronous
public class PermCommand {

    private static final String LABEL = "perm";
    private static final String SYNTAX_COMMAND = "syntax";
    private static final String LIST_COMMAND = "list";
    private static final String ADD_COMMAND = "add";
    private static final String REMOVE_COMMAND = "remove";
    private static final String CLEAR_COMMAND = "clear";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.react(0, Collections.singletonList(
                LanguageManager.get("available-subcommands",
                        StringUtil.getListToString(helper.getContext().getCommand().getChildrens(), ", ",
                                CommandInstance::getLabel))
        ), LABEL);
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(completor.getCommand().getChildrens(),
                CommandInstance::getLabel
        ), LABEL);
    }

    @Command(label = LABEL, usage = "<subCommand>", aliases = "permission")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
    }

    @Command(label = SYNTAX_COMMAND, parent = LABEL)
    public void syntax(BungeeCommandContext context, BungeeParamSet args) {
        context.sendEventMessage(LanguageManager.get("permission-syntax"));
    }

    @Command(label = LIST_COMMAND, parent = LABEL, usage = "<player> [page]")
    public void list(BungeeCommandContext context, BungeeParamSet args) {
        // get moo player
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // get permissions
        List<GroupPermission> permissions = PermissionUtil.getPermissions(player.getAllPermissions());

        // list pageable list for the permissions
        int page = args.getInt(0, 0);
        PageableList<GroupPermission> pageableList = new PageableList<>(permissions, 10, GroupPermission.PERMISSION_COMPARATOR);

        // sends the pageable list with page as list format
        context.sendDisplayFormat(new PageableListFormat<GroupPermission>(pageableList)
                .page(page).doesntExist("error-page-doesnt-exist")
                .emptyList("permission-list-empty").header("permission-list-header")
                .emptyEntry("permission-list-entry-empty").entryFormat("permission-list-entry")
                .entryf(groupPermission -> new Object[]{
                        groupPermission.isStar() ? "&9*" : groupPermission.isProxied() ? "&bb" : "&es",
                        groupPermission.getPerm().replace("*", "&9*&7").replace("-", "&c-&7")
                })
                .footer("permission-list-next-page", "/perm list " + playerName + " " + (page + 1))
        );
    }

    @Command(label = ADD_COMMAND, parent = LABEL, usage = "<player> <permission>")
    public void add(BungeeCommandContext context, BungeeParamSet args) {
        // get moo player
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // list permissions from arguments to be added
        String rawArg = args.get(1);
        List<String> argPermissions = StringUtil.find(Validation.PERMISSION.getRawRegex(), rawArg);
        context.invalidArgument(argPermissions.isEmpty(), LanguageManager.get("permission-format-invalid"));

        // set permissions
        context.sendMessage(LanguageManager.get("permission-add-load"));
        ResponseStatus status = player.addPermission(argPermissions);
        context.sendMessage(LanguageManager.get("permission-add-complete", status));
    }

    @Command(label = REMOVE_COMMAND, parent = LABEL, usage = "<player> <permission>")
    public void remove(BungeeCommandContext context, BungeeParamSet args) {
        // get moo player
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // list permissions from arguments to be added
        String rawArg = args.get(1);
        List<String> argPermissions = StringUtil.find(Validation.PERMISSION.getRawRegex(), rawArg);
        context.invalidArgument(argPermissions.isEmpty(), LanguageManager.get("permission-format-invalid"));

        // set permissions
        context.sendMessage(LanguageManager.get("permission-remove-load"));
        ResponseStatus status = player.removePermission(argPermissions);
        context.sendMessage(LanguageManager.get("permission-remove-complete", status));
    }

    @Command(label = CLEAR_COMMAND, parent = LABEL, usage = "<player>")
    public void clear(BungeeCommandContext context, BungeeParamSet args) {
        // get moo player
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // set permissions
        context.sendMessage(LanguageManager.get("permission-clear-load"));
        ResponseStatus status = player.clearPermission();
        context.sendMessage(LanguageManager.get("permission-remove-complete", status));
    }

}
