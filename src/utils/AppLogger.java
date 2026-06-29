package utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Configures simple file logging for production troubleshooting. */
public final class AppLogger {
    private static boolean configured;

    private AppLogger() {}

    public static Logger getLogger(Class<?> type) {
        configure();
        return Logger.getLogger(type.getName());
    }

    public static void configure() {
        if (configured) {
            return;
        }
        try {
            AppPaths.ensureApplicationFolders();
            FileHandler handler = new FileHandler(AppPaths.ROOT.resolve("dairyhub.log").toString(), true);
            handler.setFormatter(new SimpleFormatter());
            Logger root = Logger.getLogger("");
            root.addHandler(handler);
            root.setLevel(Level.INFO);
            configured = true;
        } catch (IOException ex) {
            Logger.getLogger(AppLogger.class.getName()).log(Level.WARNING, "Unable to initialize file logger", ex);
        }
    }
}
