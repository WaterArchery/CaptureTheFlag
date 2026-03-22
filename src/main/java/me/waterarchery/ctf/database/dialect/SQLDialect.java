package me.waterarchery.ctf.database.dialect;

// again, it is not a best approach
public interface SQLDialect {

    String createTableGames();

    String createTableGameLogs();

    String createTablePlayerLogs();

    String createTablePlayerStats();

    String insertGame();

    String insertGameLog();

    String upsertPlayerStats();

    String selectGames();

    String selectGameLogs();

    String selectGameLogsByGameId();
}