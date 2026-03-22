package me.waterarchery.ctf.database;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.database.dialect.SQLDialect;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.GameLog;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class AbstractCtfDatabase implements CtfDatabase {

    protected final ExecutorService threadPool;
    protected final Logger logger;
    protected final SQLDialect dialect;

    protected AbstractCtfDatabase(int threadCount, SQLDialect dialect) {
        this.logger = CaptureTheFlag.getInstance().getSLF4JLogger();
        this.dialect = dialect;
        ThreadFactory factory = Thread.ofVirtual().name("ctf-database-worker-", 0L)
            .uncaughtExceptionHandler((thread, throwable) -> logger.error(throwable.getMessage(), throwable))
            .factory();
        threadPool = Executors.newFixedThreadPool(threadCount, factory);
    }

    protected abstract Connection getConnection() throws SQLException;

    @Override
    public void saveGameLog(String gameId, int redScore, int blueScore, long duration) {
        threadPool.submit(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(dialect.insertGameLog())) {
                statement.setString(1, gameId);
                statement.setInt(2, redScore);
                statement.setInt(3, blueScore);
                statement.setLong(4, duration);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to save game log for game {}", gameId, e);
            }
        });
    }

    @Override
    public void saveGame(String gameId, GameLocation redFlag, GameLocation blueFlag, GameLocation redSpawn, GameLocation blueSpawn) {
        threadPool.submit(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(dialect.insertGame())) {
                statement.setString(1, gameId);
                setLocationParams(statement, 2, redFlag);
                setLocationParams(statement, 8, blueFlag);
                setLocationParams(statement, 14, redSpawn);
                setLocationParams(statement, 20, blueSpawn);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to save game {}", gameId, e);
            }
        });
    }

    @Override
    public void saveGame(CtfGame ctfGame) {
        saveGame(ctfGame.getId(), ctfGame.getRedTeam().getFlagLocation(), ctfGame.getBlueTeam().getFlagLocation(),
            ctfGame.getRedTeam().getSpawnLocation(), ctfGame.getBlueTeam().getSpawnLocation());
    }

    @Override
    public void incrementPlayerStats(UUID uuid, int kills, int deaths, int captures) {
        if (kills == 0 && deaths == 0 && captures == 0) return;

        threadPool.submit(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(dialect.upsertPlayerStats())) {
                statement.setString(1, uuid.toString());
                statement.setInt(2, kills);
                statement.setInt(3, deaths);
                statement.setInt(4, captures);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to increment player stats for {}", uuid, e);
            }
        });
    }

    @Override
    public CompletableFuture<List<GameLog>> loadGameLogs() {
        return CompletableFuture.supplyAsync(() -> {
            List<GameLog> logs = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(dialect.selectGameLogs());
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(parseGameLog(resultSet));
                }
            } catch (SQLException e) {
                logger.error("Failed to load game logs", e);
            }

            return logs;
        }, threadPool);
    }

    @Override
    public CompletableFuture<List<GameLog>> loadGameLogs(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            List<GameLog> logs = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(dialect.selectGameLogsByGameId())) {
                statement.setString(1, gameId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        logs.add(parseGameLog(resultSet));
                    }
                }
            } catch (SQLException e) {
                logger.error("Failed to load game logs for game {}", gameId, e);
            }

            return logs;
        }, threadPool);
    }

    @Override
    public List<CtfGame> loadGames() {
        List<CtfGame> games = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(dialect.selectGames());
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String gameId = resultSet.getString("game_id");

                GameLocation redFlag = parseLocation(resultSet, "red_flag");
                GameLocation blueFlag = parseLocation(resultSet, "blue_flag");
                GameLocation redSpawn = parseLocation(resultSet, "red_spawn");
                GameLocation blueSpawn = parseLocation(resultSet, "blue_spawn");

                CtfGame game = new CtfGame(gameId, redFlag, blueFlag, redSpawn, blueSpawn);
                games.add(game);
            }
        } catch (SQLException e) {
            logger.error("Failed to load games", e);
        }
        return games;
    }

    @Override
    public void close() {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in time, forcing shutdown...");
                List<Runnable> droppedTasks = threadPool.shutdownNow();
                logger.warn("Dropped {} tasks", droppedTasks.size());
            }
        } catch (Exception e) {
            logger.error("Failed to close database connection", e);
        }
    }

    protected void createTables() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute(dialect.createTableGames());
            conn.createStatement().execute(dialect.createTableGameLogs());
            conn.createStatement().execute(dialect.createTablePlayerLogs());
            conn.createStatement().execute(dialect.createTablePlayerStats());
        } catch (SQLException e) {
            logger.error("Failed to create database tables", e);
        }
    }

    protected void setLocationParams(PreparedStatement statement, int startIndex, GameLocation location) throws SQLException {
        statement.setString(startIndex, location.world());
        statement.setDouble(startIndex + 1, location.x());
        statement.setDouble(startIndex + 2, location.y());
        statement.setDouble(startIndex + 3, location.z());
        statement.setFloat(startIndex + 4, location.yaw());
        statement.setFloat(startIndex + 5, location.pitch());
    }

    protected GameLocation parseLocation(ResultSet resultSet, String prefix) throws SQLException {
        return new GameLocation(
            resultSet.getString(prefix + "_world"),
            resultSet.getDouble(prefix + "_x"),
            resultSet.getDouble(prefix + "_y"),
            resultSet.getDouble(prefix + "_z"),
            resultSet.getFloat(prefix + "_yaw"),
            resultSet.getFloat(prefix + "_pitch")
        );
    }

    protected GameLog parseGameLog(ResultSet resultSet) throws SQLException {
        int redScore = resultSet.getInt("team_red_score");
        int blueScore = resultSet.getInt("team_blue_score");

        return new GameLog(resultSet.getString("game_id"), redScore, blueScore, resultSet.getLong("duration_seconds"));
    }
}
