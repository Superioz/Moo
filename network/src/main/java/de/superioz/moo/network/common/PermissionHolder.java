package de.superioz.moo.network.common;

import de.superioz.moo.network.queries.ResponseStatus;

import java.util.List;

public interface PermissionHolder {

    ResponseStatus addPermission(List<String> l);

    ResponseStatus removePermission(List<String> l);

    ResponseStatus clearPermission();

}
