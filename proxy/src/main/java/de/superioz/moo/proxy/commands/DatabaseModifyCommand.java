package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandFlag;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.database.query.DbQueryNode;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Procedure;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.protocol.common.Queries;
import de.superioz.moo.protocol.common.ResponseStatus;

import java.util.Collections;

@RunAsynchronous
public class DatabaseModifyCommand {

    @ArgumentHelp
    public void argumentHelp(ArgumentHelper helper) {
        helper.react(0, Collections.singletonList(
                LanguageManager.get("available-types",
                        String.join(", ", StringUtil.getStringList(DatabaseType.values(),
                                type1 -> type1.ordinal() + ": " + type1.name().toLowerCase())))
        ));

        //.
        helper.react(1, (Procedure) () -> {
            DatabaseType type = helper.getContext().getParamSet().getEnum(0, DatabaseType.class);

            helper.setHelpMessages(
                    LanguageManager.get("key-value-updates-syntax"),
                    LanguageManager.get("available-fields",
                            type != null ? StringUtil.getListToString(DataResolver.getResolvableFields(type.getWrappedClass()), ", ",
                                    field -> "&f" + field.getName() + "&7") : LanguageManager.get("no-fields-available")),
                    LanguageManager.get("available-operators",
                            StringUtil.getListToString(DbQueryNode.Type.values(), ", ",
                                    operator -> "&f" + operator.toString() + "&7"))
            );
        });

        // .
        helper.react(2, (Procedure) () -> {
            DatabaseType type = helper.getContext().getParamSet().getEnum(0, DatabaseType.class);

            helper.setHelpMessages(
                    LanguageManager.get("key-value-filter-syntax"),
                    LanguageManager.get("available-fields",
                            type != null ? StringUtil.getListToString(DataResolver.getResolvableFields(type.getWrappedClass()), ", ",
                                    field -> "&f" + field.getName() + "&7") : LanguageManager.get("no-fields-available")),
                    LanguageManager.get("available-operators",
                            StringUtil.getListToString(DbQueryNode.Type.values(), ", ",
                                    operator -> "&f" + operator.toString() + "&7"))
            );
        });
    }

    @TabCompletion
    public void tabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(DatabaseType.values(), type -> type.name().toLowerCase()));
    }

    @Command(label = "dbmodify", usage = "<database> <filter> <updates>",
            flags = {"l"})
    public void dbmodify(CommandContext context, ParamSet set) {
        Queries queries = Queries.newInstance();

        // list the database
        DatabaseType type = set.getEnum(0, DatabaseType.class);
        if(type == null) queries.scope(set.get(0));
        else queries.scope(type);

        // list the filter
        String rawFilter = set.get(1);
        DbFilter filter = DbFilter.fromParameter(type == null ? null : type.getWrappedClass(), rawFilter);
        queries.filter(filter);

        // set the limit
        if(set.hasFlag("l")) {
            CommandFlag flag = set.getFlag("l");
            int limit = flag.getInt(0, -1);
            queries.limit(limit);
        }

        // list the updates
        String rawUpdates = set.get(2);
        Class<?> updateClass = type == null ? null : type.getWrappedClass();
        DbQuery query = DbQuery.fromParameter(updateClass, rawUpdates);
        queries.update(query);

        // execute modification
        context.sendMessage(LanguageManager.get("dbmodify-load"), queries.getDatabase(), queries.getFilter().getSize(), queries.getQuery().getSize());
        ResponseStatus status = queries.execute().getStatus();
        context.sendMessage(LanguageManager.get("dbmodify-complete", status));
    }

}
