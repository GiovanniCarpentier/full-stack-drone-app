package be.howest.ti.mars.logic.data;

import io.vertx.core.json.JsonObject;

public class Repositories {
    private static MarsH2Repository h2Repo = null;

    private Repositories() {
    }

    public static MarsH2Repository getH2Repo() {
        return h2Repo;
    }

    public static void configure(JsonObject dbProps) {
        h2Repo = new MarsH2Repository(dbProps.getString("url"),
                dbProps.getString("username"),
                dbProps.getString("password"),
                dbProps.getInteger("webconsole.port"));
    }

    public static void shutdown() {
        if (h2Repo != null)
            h2Repo.cleanUp();

        h2Repo = null;
    }
}
