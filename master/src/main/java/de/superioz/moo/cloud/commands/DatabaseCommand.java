package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandFlag;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.util.Operator;
import de.superioz.moo.api.util.Procedure;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.protocol.common.Queries;
import de.superioz.moo.protocol.common.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DatabaseCommand {

    @ArgumentHelp
    public void argumentHelp(ArgumentHelper helper) {
        helper.react(0, () -> {
            String dbTypes = "Available types: {%s}";
            dbTypes = String.format(dbTypes, String.join(", ", StringUtil.getStringList(DatabaseType.values(),
                    type1 -> type1.ordinal() + ": " + type1.name().toLowerCase())));
            helper.setHelpMessages(dbTypes);
        });

        // .
        helper.react(1, () -> {
            DatabaseType type = helper.getContext().getParamSet().getEnum(0, DatabaseType.class);

            String syntax = "Syntax for filter: $field$operator$value(,|$value);[...];[...] or $primKey";
            String availableFields = "Available fields: "
                    + (type == null ? "none" : "{%s}");
            if(type != null) {
                availableFields = String.format(availableFields,
                        String.join(", ", StringUtil.getStringList(DataResolver.getResolvableFields(type.getWrappedClass()),
                                field -> "§f" + field.getName() + "§r")));
            }
            String availableOperators = String.format("Available operator: {%s}",
                    String.join(", ", StringUtil.getStringList(Operator.values(), operator -> "§f" + operator.getSymbol() + "§r")));

            helper.setHelpMessages(syntax, availableFields, availableOperators);
        }, "dbmodify", "dbinfo", "dbdelete");

        // .
        Procedure updatesProcedure = () -> {
            DatabaseType type = (DatabaseType) helper.getParam(DatabaseType.class);

            String syntax = "Syntax for updates: $field$operator$value(,$value);[...];[...]";
            String availableFields = "Available fields: "
                    + (type == null ? "none" : "{%s}");
            if(type != null) {
                availableFields = String.format(availableFields,
                        String.join(", ", StringUtil.getStringList(DataResolver.getResolvableFields(type.getWrappedClass()),
                                field -> "§f" + field.getName() + "§r")));
            }
            String availableOperators = String.format("Available operator: {%s}",
                    String.join(", ", StringUtil.getStringList(DbQueryNode.Type.values(), operator -> "§f" + operator.toString() + "§r")));

            helper.setHelpMessages(syntax, availableFields, availableOperators);
        };

        helper.react(2, updatesProcedure, "dbmodify");
        helper.react(1, updatesProcedure, "dbcreate");
    }

    @TabCompletion
    public void tabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(DatabaseType.values(), type -> type.name().toLowerCase()));
    }

    @Command(label = "dbmodify", usage = "<database> <filter> <updates>",
            flags = {"l"})
    public void dbmodify(CommandContext context, ParamSet set) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = set.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(set.get(0));
        else queries.scope(type);

        // get the filter
        String rawFilter = set.get(1);
        DbFilter filter = DbFilter.fromParameter(type == null ? null : type.getWrappedClass(), rawFilter);
        queries.filter(filter);

        // set the limit
        if(set.hasFlag("l")) {
            CommandFlag flag = set.getFlag("l");
            int limit = flag.getInt(0, -1);
            queries.limit(limit);
        }

        // get the updates
        String rawUpdates = set.get(2);
        Class<?> updateClass = type == null ? null : type.getWrappedClass();
        DbQuery query = DbQuery.fromParameter(updateClass, rawUpdates);
        queries.update(query);

        context.sendMessage(StringUtil.format("Modify database entries of {0} ... (Filtersize: {1}) (Updatessize: {2})",
                queries.getDatabase(), queries.getFilter().getSize(), queries.getQuery().getSize()));

        // send response
        Response response = queries.execute();

        context.sendMessage("Modification complete. (" + response.getMessageAsList() + ")");
    }

    @Command(label = "dbinfo", usage = "<database> <filter> [page]",
            flags = {"l", "s"})
    public void dbinfo(CommandContext context, ParamSet args) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = args.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(args.get(0));
        else queries.scope(type);

        // get the filter
        String rawFilter = args.get(1);
        DbFilter filter = DbFilter.fromParameter(type == null ? null : type.getWrappedClass(), rawFilter);
        queries.filter(filter);

        // set the limit
        if(args.hasFlag("l")) {
            CommandFlag flag = args.getFlag("l");
            int limit = flag.getInt(0, -1);
            queries.limit(limit);
        }

        context.sendMessage(StringUtil.format("Retrieve database entries for {0} ... (Filtersize: {1})",
                queries.getDatabase(), queries.getFilter().getSize()));

        // get response
        String k = args.get(0) + ":" + rawFilter;
        Response response = (Response) context.get(k);
        if(response == null) {
            response = queries.execute();
            context.setExpireAfterAccess(k, response, 15, TimeUnit.SECONDS);
        }

        // display data
        List<String> data = response.getMessageAsList();
        context.sendMessage("Received data(" + data.size() + "):");

        int page = args.getInt(2, 0);
        int sizePerPage = args.hasFlag("s") ? args.getFlag("s").getInt(0, -1) : -1;
        PageableList<String> pageableList = new PageableList<>(data, sizePerPage);
        List<String> entryPage = pageableList.getPage(page);

        if(entryPage == null) {
            context.sendMessage("§cThis page doesn't exist! (" + page + ")");
            return;
        }

        DisplayFormats.sendPageableList(() -> {
                    context.sendMessage(DisplayFormats.getListSeperation("=", 20,
                            "[", "]",
                            "Data List(" + (page + 1) + "/" + (pageableList.getMaxPages() + 1) + ")"));
                },
                entryPage, new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        if(s == null) {
                            context.sendMessage("#");
                        }
                        else {
                            context.sendMessage("# " + s);
                        }
                    }
                }, () -> {
                    if(page < pageableList.getMaxPages()) {
                        context.sendMessage("");
                        context.sendMessage("Next page: /[...] " + (page + 1));
                    }
                });
    }

    @Command(label = "dbcount", usage = "<database>")
    public void dbcount(CommandContext context, ParamSet args) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = args.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(args.get(0));
        else queries.scope(type);
        queries.count(false);

        context.sendMessage(StringUtil.format("Retrieve database's entry count of {0} ...", queries.getDatabase()));

        // send response
        Response response = queries.execute();

        int count = Integer.parseInt(response.getMessage());
        context.sendMessage(StringUtil.format("Count: {0} {1}", count,
                DisplayFormats.getPluralOrSingular(count, "entry", "entries")));
    }

    @Command(label = "dblist", usage = "<database> [page]",
            flags = {"l", "s"})
    public void dblist(CommandContext context, ParamSet args) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = args.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(args.get(0));
        else queries.scope(type);
        queries.count(true);
        queries.limit(args.hasFlag("l") ? args.getFlag("l").getInt(0, -1) : -1);

        context.sendMessage(StringUtil.format("List entries of {0} ...", queries.getDatabase()));

        // get response
        String k = args.get(0);
        Response response = (Response) context.get(k);
        if(response == null) {
            response = queries.execute();
            context.setExpireAfterCreation(k, response, 15, TimeUnit.SECONDS);
        }

        // display data
        List<String> data = response.getMessageAsList();
        context.sendMessage("Received data(" + data.size() + "):");

        int page = args.getInt(2, 0);
        int sizePerPage = args.hasFlag("s") ? args.getFlag("s").getInt(0, -1) : -1;
        PageableList<String> pageableList = new PageableList<>(data, sizePerPage);
        List<String> entryPage = pageableList.getPage(page);

        if(entryPage == null) {
            context.sendMessage("§cThis page doesn't exist! (" + page + ")");
            return;
        }

        DisplayFormats.sendPageableList(() -> {
                    context.sendMessage(DisplayFormats.getListSeperation("=", 20,
                            "[", "]",
                            "Database List(" + (page + 1) + "/" + (pageableList.getMaxPages() + 1) + ")"));
                },
                entryPage, new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        if(s == null) {
                            context.sendMessage("#");
                        }
                        else {
                            context.sendMessage("# " + s);
                        }
                    }
                }, () -> {
                    if(page < pageableList.getMaxPages()) {
                        context.sendMessage("");
                        context.sendMessage("Next page: /[...] " + (page + 1));
                    }
                });
    }

    @Command(label = "dbdelete", usage = "<database> <filter>",
            flags = {"l"})
    public void dbdelete(CommandContext context, ParamSet set) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = set.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(set.get(0));
        else queries.scope(type);

        // get the filter
        String rawFilter = set.get(1);
        DbFilter filter = DbFilter.fromParameter(type == null ? null : type.getWrappedClass(), rawFilter);
        queries.filter(filter);
        queries.deletion();

        // set the limit
        if(set.hasFlag("l")) {
            CommandFlag flag = set.getFlag("l");
            int limit = flag.getInt(0, -1);
            queries.limit(limit);
        }

        context.sendMessage(StringUtil.format("Delete database entries of {0} ... (Filtersize: {1})",
                queries.getDatabase(), queries.getFilter().getSize()));

        // send response
        Response response = queries.execute();

        context.sendMessage("Deletion complete. (" + response.getMessageAsList() + ")");
    }

    @Command(label = "dbcreate", usage = "<database> [updates]",
            flags = {"l"})
    public void dbcreate(CommandContext context, ParamSet args) {
        Queries queries = Queries.newInstance();

        // get the database
        DatabaseType type = args.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(args.get(0));
        else queries.scope(type);

        // the object to be created
        Object toCreate = null;
        if(type != null) {
            toCreate = ReflectionUtil.getInstance(type.getWrappedClass());
        }
        if(toCreate == null){
            context.sendMessage("Couldn't initiate instance of " + (type != null ? type.getWrappedClass() : "null"));
            return;
        }

        // if he uses argument updates
        if(args.size() > 1) {
            try {
                DbQuery query = DbQuery.fromParameter(type.getWrappedClass(), args.get(1));
                if(query != null) query.apply(toCreate);
            }
            catch(InvalidArgumentException ex) {
                context.sendHelp();
            }
        }
        queries.creation(toCreate);

        context.sendMessage(StringUtil.format("Create database entries for {0} ...", queries.getDatabase()));

        // send response
        Response response = queries.execute();

        context.sendMessage("Creation complete. (" + response.getMessageAsList() + ")");
    }

}
