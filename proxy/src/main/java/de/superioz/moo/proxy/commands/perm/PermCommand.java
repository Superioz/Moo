package de.superioz.moo.proxy.commands.perm;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandFlag;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.GroupPermission;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.console.format.PageableListFormat;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import javafx.util.Pair;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

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

        completor.react("g", StringUtil.getStringList(
                MooQueries.getInstance().listGroups(), Group::getName
        ), LIST_COMMAND, ADD_COMMAND, REMOVE_COMMAND, CLEAR_COMMAND);

        completor.react("p", StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName
        ), LIST_COMMAND, ADD_COMMAND, REMOVE_COMMAND, CLEAR_COMMAND);
    }

    @Command(label = LABEL, usage = "<subCommand>", aliases = "permission")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
    }

    @Command(label = SYNTAX_COMMAND, parent = LABEL)
    public void syntax(BungeeCommandContext context, ParamSet args) {
        context.sendEventMessage(LanguageManager.get("permission-syntax"));
    }

    @Command(label = LIST_COMMAND, parent = LABEL, usage = "[page]", flags = {"g", "p", "s", "b"})
    public void list(BungeeCommandContext context, ParamSet args) {
        // list permissions (either from group or player)
        HashSet<String> permissions = getPermissions(context, args, "g", "p").getValue();

        // list group permissions out of strings
        List<GroupPermission> groupPermissions = new ArrayList<>();
        permissions.forEach(s -> {
            if(args.hasFlag("s") && !s.startsWith("s")) return;
            if(args.hasFlag("b") && !s.startsWith("b")) return;
            groupPermissions.add(new GroupPermission(s));
        });

        // list pageable list for the permissions
        int page = args.getInt(0, 0);
        PageableList<GroupPermission> pageableList = new PageableList<>(groupPermissions, 10,
                (Comparator<GroupPermission>) (o1, o2) -> o1.getRawPerm().compareTo(o2.getRawPerm()));
        context.invalidArgument(pageableList.isEmpty(), LanguageManager.get("permission-list-empty"));
        context.invalidArgument(!pageableList.checkPage(page), LanguageManager.get("error-page-doesnt-exist", page));

        // sends the pageable list with page as list format
        context.sendDisplayFormat(new PageableListFormat<GroupPermission>(pageableList)
                .page(page).doesntExist("error-page-doesnt-exist")
                .emptyList("permission-list-empty").header("permission-list-header")
                .emptyEntry("permission-list-entry-empty")
                .entryFormat("permission-list-entry")
                .entry(replacor -> {
                    GroupPermission groupPermission = replacor.get();
                    String prefix = groupPermission.isProxied() ? "&bb" : (groupPermission.isStar() ? "&9*" : "&es");

                    replacor.accept(prefix, groupPermission.getPerm()
                            .replace("*", "&9*&7")
                            .replace("-", "&c-&7"));
                })
                .footer("permission-list-next-page", "/perm list " + (page + 1) + " " + (args.hasFlag("g")
                        ? args.getFlag("g").getRawCommandline() : args.hasFlag("p")
                        ? args.getFlag("p").getRawCommandline() : ""))
        );
    }

    @Command(label = ADD_COMMAND, parent = LABEL, usage = "<permission>", flags = {"g", "p"})
    public void add(BungeeCommandContext context, ParamSet args) {
        // list permissions (either from group or player)
        Pair<Object, HashSet<String>> permissions = getPermissions(context, args, "g", "p");
        HashSet<String> permissionsValues = permissions.getValue();
        Object primKey = permissions.getKey();

        // list permissions from arguments to be added
        String rawArg = args.get(0);
        List<String> argPermissions = StringUtil.find(Validation.PERMISSION.getRawRegex(), rawArg);
        context.invalidArgument(argPermissions.isEmpty(), LanguageManager.get("permission-format-invalid"));

        // check if any permission already exists
        for(String s : argPermissions) {
            if(permissionsValues.contains(s)) {
                context.sendMessage(LanguageManager.get("permission-already-exists", s));
                return;
            }
        }

        // execute modification
        context.sendMessage(LanguageManager.get("permission-add-load"));
        ResponseStatus status;
        if(primKey instanceof String) {
            status = MooQueries.getInstance().modifyGroup((String) primKey,
                    DbQueryUnbaked.newInstance(DbModifier.GROUP_PERMISSIONS, argPermissions));
        }
        else {
            status = MooQueries.getInstance().modifyPlayerData(primKey,
                    DbQueryUnbaked.newInstance(DbModifier.PLAYER_EXTRA_PERMS, argPermissions));
        }
        context.sendMessage(LanguageManager.get("permission-add-complete", status));
    }

    @Command(label = REMOVE_COMMAND, parent = LABEL, usage = "<permission>", flags = {"g", "p"})
    public void remove(BungeeCommandContext context, ParamSet args) {
        // list permissions (either from group or player)
        Pair<Object, HashSet<String>> permissions = getPermissions(context, args, "g", "p");
        HashSet<String> permissionsValues = permissions.getValue();
        Object primKey = permissions.getKey();

        // list permissions from arguments to be removed
        String rawArg = args.get(0);
        List<String> argPermissions = StringUtil.find(Validation.PERMISSION.getRawRegex(), rawArg);
        context.invalidArgument(argPermissions.isEmpty(), LanguageManager.get("permission-format-invalid"));

        // check if any permission already exists
        for(String s : argPermissions) {
            if(!permissionsValues.contains(s)) {
                context.sendMessage(LanguageManager.get("permission-doesnt-exist", s));
                return;
            }
        }

        // execute modification
        context.sendMessage(LanguageManager.get("permission-remove-load"));
        ResponseStatus status;
        if(primKey instanceof String) {
            status = MooQueries.getInstance().modifyGroup((String) primKey,
                    DbQueryUnbaked.newInstance().subtract(DbModifier.GROUP_PERMISSIONS, argPermissions));
        }
        else {
            status = MooQueries.getInstance().modifyPlayerData(primKey,
                    DbQueryUnbaked.newInstance().subtract(DbModifier.PLAYER_EXTRA_PERMS, argPermissions));
        }
        context.sendMessage(LanguageManager.get("permission-remove-complete", status));
    }

    @Command(label = CLEAR_COMMAND, parent = LABEL, flags = {"g", "p"})
    public void clear(BungeeCommandContext context, ParamSet args) {
        // list permissions (either from group or player)
        Pair<Object, HashSet<String>> permissions = getPermissions(context, args, "g", "p");
        HashSet<String> permissionsValues = permissions.getValue();
        Object primKey = permissions.getKey();

        // execute modification
        context.sendMessage(LanguageManager.get("permission-clear-load"));
        ResponseStatus status;
        if(primKey instanceof String) {
            status = MooQueries.getInstance().modifyGroup((String) primKey,
                    DbQueryUnbaked.newInstance().subtract(DbModifier.GROUP_PERMISSIONS, permissionsValues));
        }
        else {
            status = MooQueries.getInstance().modifyPlayerData(primKey,
                    DbQueryUnbaked.newInstance().subtract(DbModifier.PLAYER_EXTRA_PERMS, permissionsValues));
        }
        context.sendMessage(LanguageManager.get("permission-remove-complete", status));
    }

    /**
     * Helper method for the command<br>
     * Get permissions from values out of command context
     *
     * @param context The command context
     * @param args    The argument
     * @return The list of permissions
     * @throws InvalidArgumentException If the flag arguments are invalid
     */
    private Pair<Object, HashSet<String>> getPermissions(CommandContext context, ParamSet args, String groupFlag, String playerFlag) throws InvalidArgumentException {
        HashSet<String> permissions = new HashSet<>();
        Object primKey = null;

        // permissions from group
        if(args.hasFlag(groupFlag)) {
            CommandFlag flag = args.getFlag(groupFlag);
            Group group = flag.get(0, Group.class);
            context.invalidArgument(group == null || group.getName() == null, LanguageManager.get("group-doesnt-exist", flag.get(0)));

            permissions = new HashSet<>(group.getPermissions());
            primKey = group.getName();
        }
        // permissions from player
        else if(args.hasFlag(playerFlag)) {
            CommandFlag flag = args.getFlag(playerFlag);
            PlayerData playerData = flag.get(0, PlayerData.class);
            context.invalidArgument(playerData == null || playerData.getUuid() == null, LanguageManager.get("", flag.get(0)));

            permissions = MooQueries.getInstance().getPlayerPermissions(playerData, true);
            primKey = playerData.getUuid();
        }
        // permissions from command sender
        else {
            if(context.getCommandSender() instanceof ProxiedPlayer) {
                UUID uuid = ((ProxiedPlayer) context.getCommandSender()).getUniqueId();

                permissions = MooQueries.getInstance().getPlayerPermissions(MooQueries.getInstance().getPlayerData(uuid), true);
                primKey = uuid;
            }
        }

        return new Pair<>(primKey, permissions);
    }

}
