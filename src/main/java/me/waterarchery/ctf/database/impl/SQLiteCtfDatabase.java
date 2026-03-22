package me.waterarchery.ctf.database.impl;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.database.AbstractCtfDatabase;
import me.waterarchery.ctf.database.dialect.SQLiteDialect;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// it is much easier to use Hibernate or orm in general to handle but i am sadly avoiding heavy libraries
public class SQLiteCtfDatabase extends AbstractCtfDatabase {

    private final String url;

    public SQLiteCtfDatabase() {
        super(1, new SQLiteDialect());

        File dataFolder = CaptureTheFlag.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            boolean mkdirs = dataFolder.mkdirs();
            if (!mkdirs) throw new IllegalStateException("Failed to create data folder");
        }

        url = "jdbc:sqlite:" + new File(dataFolder, "ctf.db").getAbsolutePath();

        createTables();
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
