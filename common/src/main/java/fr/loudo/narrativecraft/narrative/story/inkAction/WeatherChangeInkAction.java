package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class WeatherChangeInkAction extends InkAction{

    public WeatherChangeInkAction() {}

    public WeatherChangeInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.WEATHER, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 2) return InkActionResult.ERROR;
        name = command[2];
        switch (name) {
            case "clear" -> NarrativeCraftMod.server.overworld().setWeatherParameters(999999, 0, false, false);
            case "rain" -> NarrativeCraftMod.server.overworld().setWeatherParameters(0, 999999, true, false);
            case "thunder" -> NarrativeCraftMod.server.overworld().setWeatherParameters(0, 999999, true, true);
            case null, default -> {
                return InkActionResult.ERROR;
            }
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.weather_change", name),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 2) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_name").getString(),
                    lineText
            );
        }
        String name = command[2];
        if(!name.equals("clear") && !name.equals("rain") && !name.equals("thunder")) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.weather.value", name).getString(),
                    lineText
            );
        }
        return null;
    }
}
