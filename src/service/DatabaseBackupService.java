package service;

import utils.AppLogger;
import utils.AppPaths;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Creates MySQL dump backups in the portable Backup folder. */
public class DatabaseBackupService {
    private static final Logger LOGGER = AppLogger.getLogger(DatabaseBackupService.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    public File createBackup(String mysqlUser, String mysqlPassword, String databaseName) throws Exception {
        AppPaths.ensureApplicationFolders();
        String fileName = "backup_" + LocalDateTime.now().format(FORMAT) + ".sql";
        File backupFile = AppPaths.BACKUP.resolve(fileName).toFile();
        ProcessBuilder builder = new ProcessBuilder("mysqldump", "-u" + mysqlUser, "-p" + mysqlPassword, databaseName);
        builder.redirectOutput(backupFile);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Database backup failed. Make sure mysqldump is installed and available in PATH.");
        }
        LOGGER.log(Level.INFO, "Database backup created: {0}", backupFile.getAbsolutePath());
        return backupFile;
    }
}
