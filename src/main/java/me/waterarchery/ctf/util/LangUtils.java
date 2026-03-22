package me.waterarchery.ctf.util;

import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.LangFile;
import me.waterarchery.ctf.model.game.constant.GameStatus;

public class LangUtils {

    public static String parseGameStatus(GameStatus status) {
        LangFile lang = CaptureTheFlag.getLang();
        return switch (status) {
            case STARTING -> lang.getPlaceholders().getStarting();
            case CLEANING -> lang.getPlaceholders().getCleaning();
            case WAITING_FOR_PLAYERS -> lang.getPlaceholders().getWaitingForPlayers();
            case IN_PROGRESS -> lang.getPlaceholders().getInProgress();
            case NOT_SETUP_YET -> lang.getPlaceholders().getNotSetupYet();
        };
    }

    public static String parseTime(long time) {
        time = time / 1000;
        return String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60);
    }
}
