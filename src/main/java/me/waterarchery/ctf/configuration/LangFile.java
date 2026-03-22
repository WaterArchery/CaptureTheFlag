package me.waterarchery.ctf.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class LangFile extends OkaeriConfig {

    // commands
    private String noPermission = "<#CCFFEE>You don't have enough <#47D4FF>permission to execute <#CCFFEE>this command.";
    private String configReloaded = "<#47D4FF>Config and lang files reloaded successfully.";
    private String inGameOnly = "<#CCFFEE>You can only use this command from <#47D4FF>in-game.";
    private String notOnlinePlayer = "<#CCFFEE>Player is not in the server.";
    private String unknownCommand = "<#CCFFEE>There is <#47D4FF>no command <#CCFFEE>with this <#47D4FF>sub command.";
    private String tooManyArgs = "<#CCFFEE>You entered <#47D4FF>too many arguments <#CCFFEE>for this command.";
    private String tooFewArgs = "<#CCFFEE>You entered <#47D4FF>too few arguments <#CCFFEE>for this command.";
    private String invalidArg = "<#CCFFEE>You entered a <#47D4FF>invalid argument <#CCFFEE>for this command.";

    // messages
    private List<String> mainCommandMessages = List.of(
        "<#CCFFEE>Total CTF games: <#47D4FF>%total-games%",
        "<#CCFFEE>Joinable CTF games: <#47D4FF>%joinable-games%"
    );

    private String listMessageTemplate = "<#CCFFEE>%game-id% - %game-status% - %active-players%/%max-players% - %game-time%";
    private String gameStartingSoon = "<#CCFFEE>Game starting in <#47D4FF>%remaining-time% <#CCFFEE>seconds.";
    private String gameStarted = "<#CCFFEE>Game started. Good luck!";
    private String gameStartingBroadcast = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>starting in <#47D4FF>%remaining-time% <#CCFFEE>seconds.";
    private List<String> gameInfoMessages = List.of(
        "<#47D4FF><bold>--- Game Info: %game-id% ---",
        "<#CCFFEE>Status: <#47D4FF>%game-status%",
        "<#CCFFEE>Total Players: <#47D4FF>%total-players%",
        "<#CCFFEE>Red Team: <#47D4FF>%red-players% players <#CCFFEE>| Score: <#47D4FF>%red-score%",
        "<#CCFFEE>Blue Team: <#47D4FF>%blue-players% players <#CCFFEE>| Score: <#47D4FF>%blue-score%",
        "<#CCFFEE>Red Spawn: <#47D4FF>%red-spawn-status%",
        "<#CCFFEE>Blue Spawn: <#47D4FF>%blue-spawn-status%",
        "<#CCFFEE>Red Flag: <#47D4FF>%red-flag-status%",
        "<#CCFFEE>Blue Flag: <#47D4FF>%blue-flag-status%",
        "<#47D4FF><bold>--------------------------"
    );
    private String gameNotProperlySetup = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>is not properly setup. Flags and spawn points must be set.";
    private String gameAlreadyExists = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>already exists.";
    private String gameCreated = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>has been created. Use <#47D4FF>/ctf set-flag <#CCFFEE>and <#47D4FF>/ctf set-location <#CCFFEE>to setup flags and spawn points.";
    private String gameNotFound = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>was not found.";
    private String locationSet = "<#CCFFEE>Location for team <#47D4FF>%team% <#CCFFEE>in game <#47D4FF>%game-id% <#CCFFEE>has been set.";
    private String playerLeftGame = "<#CCFFEE>Player <#47D4FF>%player-name% <#CCFFEE>has left the game.";
    private String playerJoinedBroadcast = "<#CCFFEE>Player <#47D4FF>%player-name% <#CCFFEE>has joined the game.";
    private String playerJoinedSelf = "<#CCFFEE>You have joined game <#47D4FF>%game-id%<#CCFFEE>. Good luck!";
    private String notEnoughPlayers = "<#CCFFEE>Not enough players to start the game. <#47D4FF>Both teams need at least one player.";
    private String gameEnded = "<#CCFFEE>Game over! <#47D4FF>%winner-team% <#CCFFEE>wins! Red: <#47D4FF>%red-score% <#CCFFEE>| Blue: <#47D4FF>%blue-score%";
    private String gameEndedTie = "<#CCFFEE>Game over! It's a <#47D4FF>tie<#CCFFEE>! Red: <#47D4FF>%red-score% <#CCFFEE>| Blue: <#47D4FF>%blue-score%";
    private String gameTimeoutReached = "<#CCFFEE>Time is up! The game has ended.";
    private String cantPickupOwnFlag = "<#CCFFEE>You can't pick up your <#47D4FF>own team's flag<#CCFFEE>!";
    private String cantPickupUntilGameStart = "<#CCFFEE>You can't pick up the flag <#47D4FF>until the game starts<#CCFFEE>!";
    private String cantBlockBreak = "<#CCFFEE>You <#47D4FF>can't break blocks <#CCFFEE>during the game!";
    private String cantBlockPlace = "<#CCFFEE>You <#47D4FF>can't place blocks <#CCFFEE>during the game!";
    private String cantDamageUntilGameStart = "<#CCFFEE>You can't deal damage <#47D4FF>until the game starts<#CCFFEE>!";
    private String flagPickedUp = "<#CCFFEE>You picked up the <#47D4FF>enemy flag<#CCFFEE>! Return to your base!";
    private String flagPickedUpBroadcast = "<#47D4FF>%player-name% <#CCFFEE>has picked up the <#47D4FF>%team% <#CCFFEE>flag!";
    private String flagCapturedBroadcast = "<#47D4FF>%player-name% <#CCFFEE>captured the flag! Score: Red <#47D4FF>%red-score% <#CCFFEE>| Blue <#47D4FF>%blue-score%";
    private String gameIsRunning = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>is already running. You can't join right now.";
    private String notInGame = "<#CCFFEE>You are <#47D4FF>not in a game<#CCFFEE>.";
    private String playerLeftSelf = "<#CCFFEE>You have left the game.";
    private String playerKilledBroadcast = "<#47D4FF>%killer-name% <#CCFFEE>killed <#47D4FF>%victim-name%<#CCFFEE>.";
    private String deathRespawnMessage = "<#CCFFEE>You died! Respawning in <#47D4FF>5 seconds<#CCFFEE>.";
    private String deathRespawnTitle = "<#FF4444>You Died!";
    private String deathRespawnSubtitle = "<#CCFFEE>Respawning in <#47D4FF>5 seconds<#CCFFEE>...";
    private String respawnTitle = "<#47D4FF>Respawned!";
    private String respawnSubtitle = "<#CCFFEE>Good luck!";
    private String scoreMessage = "<#CCFFEE>Score: Red <#47D4FF>%red-score% <#CCFFEE>| Blue <#47D4FF>%blue-score%";
    private String gameNotInProgress = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>is not currently in progress.";
    private String gameIsCleaning = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>is currently cleaning. Please wait.";
    private String gameAlreadyStarting = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>is already starting or in progress.";
    private String gameStopped = "<#CCFFEE>Game <#47D4FF>%game-id% <#CCFFEE>has been stopped. Cleaning arena...";
    private String flagResetBroadcast = "<#47D4FF>%player-name% <#CCFFEE>has returned the <#47D4FF>%team% <#CCFFEE>flag!";
    private String bossBarText = "<red>Red Capture: %red-score% <white><bold>|<reset> <#47D4FF>Blue Capture: %blue-score%";

    private PlaceholderConfiguration placeholders = new PlaceholderConfiguration();

    private ScoreBoardConfiguration scoreBoard = new ScoreBoardConfiguration();

    @Getter
    @Setter
    public static class PlaceholderConfiguration extends OkaeriConfig {

        private String set = "Yes";
        private String notSet = "No";

        private String red = "Red";
        private String blue = "Blue";

        // game status
        private String starting = "Starting";
        private String inProgress = "In Progress";
        private String cleaning = "Cleaning";
        private String waitingForPlayers = "Waiting for Players";
        private String notSetupYet = "Not Setup Yet";
    }

    @Getter
    @Setter
    public static class ScoreBoardConfiguration extends OkaeriConfig {

        private String scoreBoardTitle = "<yellow><bold>Capture the Flag";

        private List<String> scoreBoardLines = List.of(
            "",
            "<yellow>Name: <white>%player-name%",
            "<yellow>Game Id: <white>%game-id%",
            "",
            "<red>Red Capture: <white>%red-score%",
            "<aqua>Blue Capture: <white>%blue-score%",
            "",
            "<yellow>Team: <white>%team%",
            "<yellow>Game State: <white>%game-state%",
            "",
            "<yellow>%game-time%"
        );
    }
}
