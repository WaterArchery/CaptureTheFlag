package me.waterarchery.ctf.database.query;

import lombok.Getter;

@Getter
public enum SQLQueries {

    CREATE_TABLE_GAMES("CREATE TABLE IF NOT EXISTS games (" +
        "game_id TEXT PRIMARY KEY, " +
        "red_flag_world TEXT, red_flag_x REAL, red_flag_y REAL, red_flag_z REAL, red_flag_yaw REAL, red_flag_pitch REAL, " +
        "blue_flag_world TEXT, blue_flag_x REAL, blue_flag_y REAL, blue_flag_z REAL, blue_flag_yaw REAL, blue_flag_pitch REAL, " +
        "red_spawn_world TEXT, red_spawn_x REAL, red_spawn_y REAL, red_spawn_z REAL, red_spawn_yaw REAL, red_spawn_pitch REAL, " +
        "blue_spawn_world TEXT, blue_spawn_x REAL, blue_spawn_y REAL, blue_spawn_z REAL, blue_spawn_yaw REAL, blue_spawn_pitch REAL" +
        ")"),

    CREATE_TABLE_GAME_LOGS("CREATE TABLE IF NOT EXISTS game_logs (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "game_id TEXT NOT NULL, " +
        "team_red_score INTEGER NOT NULL, " +
        "team_blue_score INTEGER NOT NULL, " +
        "duration_seconds INTEGER NOT NULL, " +
        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")"),

    CREATE_TABLE_PLAYER_LOGS("CREATE TABLE IF NOT EXISTS player_logs (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "game_id TEXT NOT NULL, " +
        "player_uuid TEXT NOT NULL, " +
        "team TEXT NOT NULL, " +
        "kills INTEGER NOT NULL, " +
        "deaths INTEGER NOT NULL, " +
        "captures INTEGER NOT NULL, " +
        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")"),

    INSERT_GAME("INSERT OR REPLACE INTO games (game_id, " +
        "red_flag_world, red_flag_x, red_flag_y, red_flag_z, red_flag_yaw, red_flag_pitch, " +
        "blue_flag_world, blue_flag_x, blue_flag_y, blue_flag_z, blue_flag_yaw, blue_flag_pitch, " +
        "red_spawn_world, red_spawn_x, red_spawn_y, red_spawn_z, red_spawn_yaw, red_spawn_pitch, " +
        "blue_spawn_world, blue_spawn_x, blue_spawn_y, blue_spawn_z, blue_spawn_yaw, blue_spawn_pitch" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),

    INSERT_GAME_LOG("INSERT INTO game_logs (game_id, team_red_score, team_blue_score, duration_seconds) VALUES (?, ?, ?, ?)"),

    INSERT_PLAYER_LOG("INSERT INTO player_logs (game_id, player_uuid, team, kills, deaths, captures) VALUES (?, ?, ?, ?, ?, ?)"),

    SELECT_GAMES("SELECT * FROM games"),

    SELECT_GAME_LOGS("SELECT * FROM game_logs ORDER BY timestamp DESC"),

    SELECT_GAME_LOGS_BY_GAME_ID("SELECT * FROM game_logs WHERE game_id = ? ORDER BY timestamp DESC"),

    SELECT_TOP_PLAYERS("SELECT player_uuid, SUM(kills) AS total_kills, SUM(deaths) AS total_deaths FROM player_logs GROUP BY player_uuid ORDER BY total_kills DESC LIMIT ?"),

    SELECT_PLAYER_LOGS("SELECT * FROM player_logs WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ?"),

    CREATE_TABLE_PLAYER_STATS("CREATE TABLE IF NOT EXISTS player_stats (" +
        "player_uuid TEXT PRIMARY KEY, " +
        "kills INTEGER NOT NULL DEFAULT 0, " +
        "deaths INTEGER NOT NULL DEFAULT 0, " +
        "captures INTEGER NOT NULL DEFAULT 0" +
        ")"),

    UPSERT_PLAYER_STATS("INSERT INTO player_stats (player_uuid, kills, deaths, captures) VALUES (?, ?, ?, ?) " +
        "ON CONFLICT(player_uuid) DO UPDATE SET " +
        "kills = kills + excluded.kills, " +
        "deaths = deaths + excluded.deaths, " +
        "captures = captures + excluded.captures");

    private final String query;

    SQLQueries(String query) {
        this.query = query;
    }
}