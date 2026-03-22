package me.waterarchery.ctf.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class SoundsFile extends OkaeriConfig {

    private String dingDing = Sound.BLOCK_NOTE_BLOCK_PLING.name();
    private String helpMessage = Sound.ENTITY_PLAYER_LEVELUP.name();
    private String gameNotFound = Sound.ENTITY_VILLAGER_NO.name();
    private String locationSet = Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name();
    private String playerLeftGame = Sound.BLOCK_NOTE_BLOCK_BASS.name();
    private String playerJoined = Sound.ENTITY_PLAYER_LEVELUP.name();
    private String notEnoughPlayers = Sound.ENTITY_VILLAGER_NO.name();
    private String gameEnded = Sound.UI_TOAST_CHALLENGE_COMPLETE.name();
    private String cantPickupOwnFlag = Sound.ENTITY_VILLAGER_NO.name();
    private String cantPickupUntilGameStart = Sound.ENTITY_VILLAGER_NO.name();
    private String cantBlockBreak = Sound.ENTITY_VILLAGER_NO.name();
    private String cantBlockPlace = Sound.ENTITY_VILLAGER_NO.name();
    private String cantDamageUntilGameStart = Sound.ENTITY_VILLAGER_NO.name();
    private String flagPickedUp = Sound.ENTITY_ITEM_PICKUP.name();
    private String flagCaptured = Sound.ENTITY_FIREWORK_ROCKET_BLAST.name();
    private String gameNotProperlySetup = Sound.ENTITY_VILLAGER_NO.name();
    private String gameAlreadyExists = Sound.ENTITY_VILLAGER_NO.name();
    private String gameCreated = Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name();
    private String gameIsRunning = Sound.ENTITY_VILLAGER_NO.name();
    private String notInGame = Sound.ENTITY_VILLAGER_NO.name();
    private String playerLeftSelf = Sound.BLOCK_NOTE_BLOCK_BASS.name();
    private String playerKilled = Sound.ENTITY_PLAYER_HURT.name();
    private String playerRespawn = Sound.ENTITY_PLAYER_LEVELUP.name();
    private String configReloaded = Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name();
    private String gameNotInProgress = Sound.ENTITY_VILLAGER_NO.name();
    private String gameIsCleaning = Sound.ENTITY_VILLAGER_NO.name();
    private String gameAlreadyStarting = Sound.ENTITY_VILLAGER_NO.name();
    private String gameStopped = Sound.BLOCK_NOTE_BLOCK_BASS.name();
    private String flagReset = Sound.BLOCK_NOTE_BLOCK_PLING.name();
}
