package me.waterarchery.ctf.listener;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.configuration.SoundsFile;
import me.waterarchery.ctf.manager.CacheManager;
import me.waterarchery.ctf.manager.GameManager;
import me.waterarchery.ctf.model.game.CtfGame;
import me.waterarchery.ctf.model.game.GameLocation;
import me.waterarchery.ctf.model.game.constant.GameStatus;
import me.waterarchery.ctf.model.player.CtfPlayer;
import me.waterarchery.ctf.model.player.constant.GameTeam;
import me.waterarchery.ctf.util.ChatUtils;
import me.waterarchery.ctf.util.SoundUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class CaptureListeners implements Listener {

    private final CacheManager cacheManager = CacheManager.getInstance();
    private final GameManager gameManager = GameManager.getInstance();
    private final ConfigFile config;
    private final LangFile lang;
    private final SoundsFile sounds;

    public CaptureListeners() {
        config = CaptureTheFlag.getPluginConfig();
        lang = CaptureTheFlag.getLang();
        sounds = CaptureTheFlag.getSounds();
    }

    @EventHandler
    public void onPickUpFlag(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (block.getType() != config.getRedTeamFlag() && block.getType() != config.getBlueTeamFlag()) return;

        Player player = event.getPlayer();
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) return;

        if ((ctfPlayer.getTeam() == GameTeam.BLUE && block.getType() == config.getBlueTeamFlag()) ||
            (ctfPlayer.getTeam() == GameTeam.RED && block.getType() == config.getRedTeamFlag())) {
            ChatUtils.sendPrefixedMessage(player, lang.getCantPickupOwnFlag());
            SoundUtils.sendSoundRaw(player, sounds.getCantPickupOwnFlag());
            return;
        }

        CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());
        if (ctfGame.getGameStatus() != GameStatus.IN_PROGRESS) {
            ChatUtils.sendPrefixedMessage(player, lang.getCantPickupUntilGameStart());
            SoundUtils.sendSoundRaw(player, sounds.getCantPickupUntilGameStart());
            return;
        }

        GameTeam enemyTeam = ctfPlayer.getTeam() == GameTeam.BLUE ? GameTeam.RED : GameTeam.BLUE;
        GameLocation flagLocation = ctfGame.getTeam(enemyTeam).getFlagLocation();
        if ((int) flagLocation.x() != block.getX() || (int) flagLocation.y() != block.getY() || (int) flagLocation.z() != block.getZ()) return;

        ItemStack flagItem = new ItemStack(block.getType());
        player.getInventory().addItem(flagItem);
        block.setType(Material.AIR);

        broadcastEnemyTeamFlagPickup(player, ctfPlayer, ctfGame);
    }

    @EventHandler
    public void onBaseEnter(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        CacheManager cacheManager = CacheManager.getInstance();
        CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
        if (ctfPlayer == null) return;

        CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());
        if (ctfGame.getGameStatus() != GameStatus.IN_PROGRESS) return;

        Material captureItem = ctfPlayer.getTeam() == GameTeam.BLUE ? config.getRedTeamFlag() : config.getBlueTeamFlag();
        if (!player.getInventory().contains(captureItem)) return;

        GameLocation returnBaseLocation = ctfGame.getTeam(ctfPlayer.getTeam()).getFlagLocation();
        GameLocation playerLocation = GameLocation.fromLocation(player.getLocation());

        if (playerLocation.distance(returnBaseLocation) <= config.getBaseCaptureRadius()) {
            GameManager gameManager = GameManager.getInstance();
            gameManager.handleCaptureScore(ctfGame, ctfPlayer);
        }
    }

    @EventHandler
    public void onPickupDroppedFlag(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            CtfPlayer ctfPlayer = cacheManager.getPlayers().get(player.getUniqueId());
            if (ctfPlayer == null) return;

            ItemStack pickedUpItem = event.getItem().getItemStack();
            boolean isFlag = pickedUpItem.getType() == config.getRedTeamFlag() || pickedUpItem.getType() == config.getBlueTeamFlag();
            if (!isFlag) return;

            boolean isEnemyTeamFlag = ctfPlayer.getTeam() == GameTeam.BLUE ?
                pickedUpItem.getType() == config.getRedTeamFlag() :
                pickedUpItem.getType() == config.getBlueTeamFlag();
            CtfGame ctfGame = cacheManager.getGames().get(ctfPlayer.getGameId());
            if (isEnemyTeamFlag) {
                broadcastEnemyTeamFlagPickup(player, ctfPlayer, ctfGame);
            } else {
                event.setCancelled(true);
                event.getItem().remove();

                gameManager.resetFlag(ctfGame, ctfPlayer.getTeam());

                String teamPlaceholder = ctfPlayer.getTeam() == GameTeam.BLUE ? lang.getPlaceholders().getBlue() : lang.getPlaceholders().getRed();
                ctfGame.getPlayers().forEach(loopCtfPlayer -> {
                    Player bukkitPlayer = loopCtfPlayer.getBukkitPlayer();
                    String resetMessage = lang.getFlagResetBroadcast()
                        .replace("%player-name%", player.getName())
                        .replace("%team%", teamPlaceholder);
                    ChatUtils.sendPrefixedMessage(bukkitPlayer, resetMessage);
                    SoundUtils.sendSoundRaw(bukkitPlayer, sounds.getFlagReset());
                });
            }
        }
    }

    private void broadcastEnemyTeamFlagPickup(Player player, CtfPlayer ctfPlayer, CtfGame ctfGame) {
        ChatUtils.sendPrefixedMessage(player, lang.getFlagPickedUp());
        SoundUtils.sendSoundRaw(player, sounds.getFlagPickedUp());

        String enemyTeamPlaceholder = ctfPlayer.getTeam() == GameTeam.BLUE ? lang.getPlaceholders().getRed() : lang.getPlaceholders().getBlue();
        ctfGame.getPlayers().forEach(loopCtfPlayer -> {
            Player bukkitPlayer = loopCtfPlayer.getBukkitPlayer();
            String broadcastMessage = lang.getFlagPickedUpBroadcast()
                .replace("%player-name%", player.getName())
                .replace("%team%", enemyTeamPlaceholder);
            ChatUtils.sendPrefixedMessage(bukkitPlayer, broadcastMessage);
        });
    }
}
