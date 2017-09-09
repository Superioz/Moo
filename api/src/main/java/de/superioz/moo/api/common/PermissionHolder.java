package de.superioz.moo.api.common;

public interface PermissionHolder<T> {

    T addPermission(String... permissions);

    T removePermission(String... permissions);

    T clearPermissions();

}
