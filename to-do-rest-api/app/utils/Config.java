package utils;

import java.util.*;
import java.util.stream.Collectors;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public enum Option {
        LDAP_BASE_DOMAIN, //
        LDAP_BASE_PARTITION_NAME, //
        LDAP_TLS_PRIVATE_KEY, //
        LDAP_TLS_CERTIFICATE, //
        LDAP_PLAIN_PORT, //
        LDAP_TLS_PORT, //
        LDAP_GROUPS_SCOPE, //
        MONGODB_ENABLE, //
        MONGODB_DATABASE, //
        MONGODB_HOSTNAME, //
        MONGODB_USERNAME, //
        MONGODB_PASSWORD, //
        MONGODB_DISABLE_TLS, //
        MONGODB_ENCRYPTION_KEY, //
        OPENID_CLIENT_ID, //
        OPENID_SECRET, //
        OPENID_URL_REALM, //
        OPENID_URL_TOKEN, //
        OPENID_URL_AUTH, //
        OPENID_URL_LOGOUT, //
        SERVICES, //
        USER_EXPIRES, //
        USER_SESSION_EXPIRES, //
        USER_ACCESS_TOKEN_EXPIRES; //

        public String get() {
            return Config.get(this);
        }

        public Integer getInteger() {
            return Config.getInteger(this);
        }

        public Long getLong() {
            return Config.getLong(this);
        }


        public List<String> getStringList() {
            return Config.getStringList(this);
        }

        public Boolean getBoolean() {
            return Config.getBoolean(this);
        }
    }

    private static final Map<Config.Option, String> config;

    static {
        Map<String, String> env = System.getenv();

        Map<Config.Option, String> cfg = new HashMap<>();

        // Set some defaults which mostly help with running tests
        cfg.put(Option.MONGODB_ENABLE, dotenv.get("MONGODB_ENABLE", "false"));
        cfg.put(Option.MONGODB_DATABASE, dotenv.get("MONGODB_DATABASE", "ToDoDatabase"));
        cfg.put(Option.MONGODB_HOSTNAME, dotenv.get("MONGODB_HOSTNAME", "localhost"));
        cfg.put(Option.MONGODB_USERNAME, dotenv.get("MONGODB_USERNAME", ""));
        cfg.put(Option.MONGODB_PASSWORD, dotenv.get("MONGODB_PASSWORD", ""));
        cfg.put(Option.USER_EXPIRES, dotenv.get("USER_EXPIRES", "" + 60L * 60L * 24L * 90L));
        cfg.put(Option.USER_SESSION_EXPIRES, dotenv.get("USER_SESSION_EXPIRES", "" + 60L * 60L * 24L));
        cfg.put(Option.USER_ACCESS_TOKEN_EXPIRES, dotenv.get("USER_ACCESS_TOKEN_EXPIRES", "" + 60L * 60L));


        for (Option o : Option.values()) {
            String envValue = dotenv.get(o.name());
            if (envValue != null) {
                cfg.put(o, envValue);
            }
        }

        config = Collections.unmodifiableMap(cfg);
    }

    public static String get(Option o) {
        return config.get(o);
    }

    public static Integer getInteger(Option o) {
        if (!config.containsKey(o)) {
            return null;
        }
        try {
            return Integer.parseInt(config.get(o));
        } catch (Exception e) {
            throw new RuntimeException("Can't parse option " + o + ": " + e.getMessage());
        }
    }

    public static Long getLong(Option o) {
        if (!config.containsKey(o)) {
            return null;
        }
        try {
            return Long.parseLong(config.get(o));
        } catch (Exception e) {
            throw new RuntimeException("Can't parse option " + o + ": " + e.getMessage());
        }
    }

    public static List<String> getStringList(Option o) {
        if (!config.containsKey(o)) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.get(o).split(",")).stream().map(String::trim).collect(Collectors.toList());
    }

    public static boolean getBoolean(Option o) {
        return Boolean.parseBoolean(get(o));
    }
}

