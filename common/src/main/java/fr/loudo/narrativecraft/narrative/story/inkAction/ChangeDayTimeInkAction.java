package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

public class ChangeDayTimeInkAction extends InkAction {

    private boolean isPaused;
    private double waitSeconds;
    private long startTime, firstTick, secondTick, pauseStartTime;
    private double t;
    private String subCommand;
    private Easing easing;

    public ChangeDayTimeInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.DAYTIME, command);
        isPaused = false;
        pauseStartTime = 0;
    }

    @Override
    public InkActionResult execute() {
        subCommand = command[1];
        firstTick = 0;
        secondTick = 0;
        t = 0;
        easing = Easing.SMOOTH;
        if(command.length > 2) {
            String firstTickString = command[2];
            firstTick = getTickFromString(firstTickString);
            if(firstTick == -1) {
                return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
            }
            if(command.length >= 8 &&  subCommand.equals("set")) {
                String secondTickString = command[4];
                secondTick = getTickFromString(secondTickString);
                try {
                    waitSeconds = Double.parseDouble(command[6]);
                    if (command[7].contains("minute")) {
                        waitSeconds *= 60;
                    } else if (command[7].contains("hour")) {
                        waitSeconds *= 60 * 60;
                    }
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[6]).getString());
                }
                if(command.length >= 9 && !command[command.length - 1].equals("times")) {
                    try {
                        easing = Easing.valueOf(command[8].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return InkActionResult.error(this.getClass(), Translation.message("validation.easing", command[8], Easing.getEasingsString()).getString());
                    }
                }
                startTime = System.currentTimeMillis();
                storyHandler.getInkActionList().add(this);
                if(secondTick == -1) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[4]).getString());
                }
            }
            if(command.length == 3) {
                if(subCommand.equals("set")) {
                    for(ServerLevel serverlevel : NarrativeCraftMod.server.getAllLevels()) {
                        serverlevel.setDayTime(firstTick);
                    }
                } else if(subCommand.equals("add")) {
                    for(ServerLevel serverlevel : NarrativeCraftMod.server.getAllLevels()) {
                        serverlevel.setDayTime(serverlevel.getDayTime() + firstTick);
                    }
                }
                NarrativeCraftMod.server.forceTimeSynchronization();
            }
            sendDebugDetails();
        }
        return InkActionResult.pass();
    }

    public void interpolateTime() {
        long now = System.currentTimeMillis();
        if(Minecraft.getInstance().isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if(!Minecraft.getInstance().isPaused() && isPaused) {
            startTime += now - pauseStartTime;
            isPaused = false;
        }
        double easedT = Easing.getInterpolation(easing, t);

        long interpolatedTick = (long) Mth.lerp(easedT, firstTick, secondTick);

        for (ServerLevel serverLevel : NarrativeCraftMod.server.getAllLevels()) {
            serverLevel.setDayTime(interpolatedTick);
        }
        NarrativeCraftMod.server.forceTimeSynchronization();

        double elapsed = (now - startTime) / 1000.0;
        if(!isPaused) {
            t = Math.min(elapsed / waitSeconds, 1.0);
        }
    }


    public boolean isFinished() {
        return t >= 1.0;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Component message = null;
            if(subCommand.equals("set") && secondTick == 0) {
                message = Translation.message("debug.change_time.one_set", firstTick);
            } else if(subCommand.equals("add") && secondTick == 0) {
                message = Translation.message("debug.change_time.one_add", firstTick);
            } else if(subCommand.equals("set") && secondTick > 0) {
                message = Translation.message("debug.change_time.interpolate", firstTick, secondTick, waitSeconds);
            }
            Minecraft.getInstance().player.displayClientMessage(
                    message,
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(getTickFromString(command[2]) == -1) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.number", command[2]).getString(),
                    lineText,
                    false
            );
        }
        if(command.length > 3) {
            if(getTickFromString(command[4]) == -1) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[4]).getString(),
                        lineText,
                        false
                );
            }
            if(command.length == 5) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.change_time.missing_time").getString(),
                        lineText,
                        false
                );
            }
            try {
                Double.parseDouble(command[6]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[6]).getString(),
                        lineText,
                        false
                );
            }
            if(command.length == 9) {
                try {
                    Easing.valueOf(command[8]);
                } catch (IllegalArgumentException e) {
                    return new ErrorLine(
                            line,
                            scene,
                            Translation.message("validation.easing", command[8], Easing.getEasingsString()).getString(),
                            lineText,
                            false
                    );
                }
            }
        }
        return null;
    }

    private long getTickFromString(String dayTime) {
        switch (dayTime) {
            case "day" -> {
                return 1000L;
            }
            case "midnight" -> {
                return 18000L;
            }
            case "night" -> {
                return 13000L;
            }
            case "noon" -> {
                return 6000L;
            }
            case null, default -> {
                try {
                    return Long.parseLong(dayTime);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
    }
}
