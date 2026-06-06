package com.everest.soundwave.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public final class AuthManager {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final String ADMIN_LOGIN = "admin";
    public static final String ADMIN_PASSWORD = "admin1234";
    public static final String ADMIN_USER_ID = "__admin__";

    private static final String PREFS = "auth_prefs";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_CURRENT = "current_user_id";

    private final SharedPreferences prefs;

    private AuthManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static volatile AuthManager instance;

    public static AuthManager get(Context ctx) {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) instance = new AuthManager(ctx);
            }
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return prefs.getString(KEY_CURRENT, null) != null;
    }

    public String currentUserId() {
        return prefs.getString(KEY_CURRENT, ADMIN_USER_ID);
    }

    public String currentUserName() {
        String id = currentUserId();
        if (ADMIN_USER_ID.equals(id)) return "Администратор";
        return id;
    }

    public boolean isAdmin() {
        return ADMIN_USER_ID.equals(currentUserId());
    }

    public String role() {
        return isAdmin() ? ROLE_ADMIN : ROLE_USER;
    }

    public void logout() {
        prefs.edit().remove(KEY_CURRENT).apply();
    }

    public boolean register(String username, String password) {
        if (username == null || password == null) return false;
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) return false;
        if (ADMIN_LOGIN.equalsIgnoreCase(username)) return false;

        JSONObject users = loadUsers();
        if (users.has(username)) return false;
        try {
            users.put(username, password);
        } catch (JSONException e) {
            return false;
        }
        prefs.edit()
                .putString(KEY_USERS, users.toString())
                .putString(KEY_CURRENT, username)
                .apply();
        return true;
    }

    // null = успех, иначе текст ошибки
    public String login(String username, String password) {
        if (username == null || password == null) return "Введите логин и пароль";
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) return "Введите логин и пароль";

        if (ADMIN_LOGIN.equalsIgnoreCase(username)) {
            if (ADMIN_PASSWORD.equals(password)) {
                prefs.edit().putString(KEY_CURRENT, ADMIN_USER_ID).apply();
                return null;
            }
            return "Неверный пароль администратора";
        }

        JSONObject users = loadUsers();
        String stored = users.optString(username, null);
        if (stored == null) return "Пользователь не найден";
        if (!stored.equals(password)) return "Неверный пароль";

        prefs.edit().putString(KEY_CURRENT, username).apply();
        return null;
    }

    private JSONObject loadUsers() {
        String raw = prefs.getString(KEY_USERS, null);
        if (raw == null) return new JSONObject();
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
