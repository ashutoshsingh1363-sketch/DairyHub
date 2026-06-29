package utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Centralizes portable relative paths for DAIRY HUB. */
public final class AppPaths {
    public static final Path ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    public static final Path DATABASE = ROOT.resolve("Database");
    public static final Path IMAGES = ROOT.resolve("Images");
    public static final Path FARMER_IMAGES = IMAGES.resolve("Farmers");
    public static final Path QR_IMAGES = IMAGES.resolve("QR");
    public static final Path LOGO_IMAGES = IMAGES.resolve("Logo");
    public static final Path RECEIPTS = ROOT.resolve("Receipts");
    public static final Path REPORTS = ROOT.resolve("Reports");
    public static final Path BACKUP = ROOT.resolve("Backup");
    public static final Path DOCUMENTS = ROOT.resolve("Documents");
    public static final Path TOOLS = ROOT.resolve("Tools");
    public static final Path LIB = ROOT.resolve("Lib");

    private AppPaths() {}

    public static void ensureApplicationFolders() {
        ensure(DATABASE);
        ensure(IMAGES);
        ensure(FARMER_IMAGES);
        ensure(QR_IMAGES);
        ensure(LOGO_IMAGES);
        ensure(RECEIPTS);
        ensure(REPORTS);
        ensure(BACKUP);
        ensure(DOCUMENTS);
        ensure(TOOLS);
        ensure(LIB);
    }

    public static String relative(Path path) {
        return ROOT.relativize(path.toAbsolutePath().normalize()).toString().replace(File.separatorChar, '/');
    }

    private static void ensure(Path path) {
        File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
