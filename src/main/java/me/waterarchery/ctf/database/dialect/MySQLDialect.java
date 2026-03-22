package me.waterarchery.ctf.database.dialect;

import me.waterarchery.ctf.database.query.MySQLQueries;

public class MySQLDialect implements SQLDialect {

    @Override
    public String createTableGames() {
        return MySQLQueries.CREATE_TABLE_GAMES.getQuery();
    }

    @Override
    public String createTableGameLogs() {
        return MySQLQueries.CREATE_TABLE_GAME_LOGS.getQuery();
    }

    @Override
    public String createTablePlayerLogs() {
        return MySQLQueries.CREATE_TABLE_PLAYER_LOGS.getQuery();
    }

    @Override
    public String createTablePlayerStats() {
        return MySQLQueries.CREATE_TABLE_PLAYER_STATS.getQuery();
    }

    @Override
    public String insertGame() {
        return MySQLQueries.INSERT_GAME.getQuery();
    }

    @Override
    public String insertGameLog() {
        return MySQLQueries.INSERT_GAME_LOG.getQuery();
    }

    @Override
    public String upsertPlayerStats() {
        return MySQLQueries.UPSERT_PLAYER_STATS.getQuery();
    }

    @Override
    public String selectGames() {
        return MySQLQueries.SELECT_GAMES.getQuery();
    }

    @Override
    public String selectGameLogs() {
        return MySQLQueries.SELECT_GAME_LOGS.getQuery();
    }

    @Override
    public String selectGameLogsByGameId() {
        return MySQLQueries.SELECT_GAME_LOGS_BY_GAME_ID.getQuery();
    }
}