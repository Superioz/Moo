package de.superioz.moo.api.utils;

import de.superioz.moo.api.database.object.UniqueIdBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Inspired by @jofkos
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MojangAPIFetcher {

    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final String UUID_URL = "https://object.mojang.com/users/profiles/minecraft/%s";

    /*public static void main(String[] args){
        System.out.println(UUID.fromString("2d30be411c6f4758911ec60912cd16ca".replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
    }*/

    /**
     * Fetches SkinData from given uniqueId
     *
     * @param uuid The uniqueId
     * @return The result
     * @throws IOException Exceptional
     */
    public static UniqueIdBuf fetch(UUID uuid) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(SKIN_URL, fromUUID(uuid))).openConnection();
        connection.setReadTimeout(5000);

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String json = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            JSONObject object = new JSONObject(json);

            UniqueIdBuf buf = new UniqueIdBuf();
            if(object.has("id")) buf.uuid = fromString(object.getString("id"));
            if(object.has("name")) buf.name = object.getString("name");
            if(object.has("properties")) {
                JSONArray l = object.getJSONArray("properties");
                if(l.length() != 0) {
                    JSONObject prop = l.getJSONObject(0);

                    if(prop.has("signature")) buf.textureSignature = prop.getString("signature");
                    if(prop.has("value")) buf.textureValue = prop.getString("value");
                }
            }
        }
        return null;
    }

    /**
     * Fetches a uniqueId from given name
     *
     * @param name The name
     * @return The uniqueId
     * @throws Exception Exceptional
     */
    public static UUID fetch(String name) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name)).openConnection();
        connection.setReadTimeout(5000);

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String json = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            JSONObject object = new JSONObject(json);

            if(object.has("id")) return fromString(object.getString("id"));
        }
        return null;
    }

    // string from uuid
    public static String fromUUID(UUID value) {
        return value.toString().replace("-", "");
    }

    // string to uuid
    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

}

