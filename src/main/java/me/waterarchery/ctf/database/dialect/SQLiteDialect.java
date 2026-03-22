package me.waterarchery.ctf.database.dialect;

import me.waterarchery.ctf.database.query.SQLQueries;

public class SQLiteDialect implements SQLDialect {

    @Override
    public String createTableGames() {
        return SQLQueries.CREATE_TABLE_GAMES.getQuery();
    }

    @Override
    public String createTableGameLogs() {
        return SQLQueries.CREATE_TABLE_GAME_LOGS.getQuery();
    }

    @Override
    public String createTablePlayerLogs() {
        return SQLQueries.CREATE_TABLE_PLAYER_LOGS.getQuery();
    }

    @Override
    public String createTablePlayerStats() {
        return SQLQueries.CREATE_TABLE_PLAYER_STATS.getQuery();
    }

    @Override
    public String insertGame() {
        return SQLQueries.INSERT_GAME.getQuery();
    }

    @Override
    public String insertGameLog() {
        return SQLQueries.INSERT_GAME_LOG.getQuery();
    }

    @Override
    public String upsertPlayerStats() {
        return SQLQueries.UPSERT_PLAYER_STATS.getQuery();
    }

    @Override
    public String selectGames() {
        return SQLQueries.SELECT_GAMES.getQuery();
    }

    @Override
    public String selectGameLogs() {
        return SQLQueries.SELECT_GAME_LOGS.getQuery();
    }

    @Override
    public String selectGameLogsByGameId() {
        return SQLQueries.SELECT_GAME_LOGS_BY_GAME_ID.getQuery();
    }
}