package fr.loudo.narrativecraft.screens.mainScreen.options;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.NarrativeUserOptions;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.credits.CreditsScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class MainScreenOptionsScreen extends OptionsSubScreen {

    private final NarrativeUserOptions narrativeUserOptions = NarrativeCraftMod.getInstance().getNarrativeUserOptions();
    private long textSpeed;
    private Checkbox autoSkipCheck;

    public MainScreenOptionsScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal(""));
        textSpeed = narrativeUserOptions.TEXT_SPEED;
    }

    @Override
    public void onClose() {
        super.onClose();
        narrativeUserOptions.TEXT_SPEED = textSpeed;
        narrativeUserOptions.AUTO_SKIP = autoSkipCheck.selected();
        NarrativeCraftFile.updateUserOptions();
    }

    @Override
    protected void addContents() {
        LinearLayout linearlayout = this.layout.addToContents(LinearLayout.vertical()).spacing(8);
        AbstractSliderButton abstractSliderButton = new AbstractSliderButton(
                50,
                20,
                200,
                20,
                Translation.message("screen.main_screen.options.dialog_speed", String.format("%.2f", narrativeUserOptions.TEXT_SPEED / 1000.0)),
                (400.0 - narrativeUserOptions.TEXT_SPEED) / 400.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message(
                        "screen.main_screen.options.dialog_speed",
                        String.format("%.2f", (400 - (this.value * 400)) / 1000.0)
                ));
            }

            @Override
            protected void applyValue() {
                textSpeed = (long) (400 - (this.value * 400));
            }
        };
        linearlayout.addChild(abstractSliderButton);
        autoSkipCheck = Checkbox.builder(Translation.message("screen.main_screen.options.auto_skip"), minecraft.font).selected(narrativeUserOptions.AUTO_SKIP).build();
        linearlayout.addChild(autoSkipCheck);

        linearlayout.addChild(Button.builder(Translation.message("screen.main_screen.minecraft_options"), button -> {
            OptionsScreen screen = new OptionsScreen(this, minecraft.options);
            minecraft.setScreen(screen);
        }).width(200).build());

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null || storyHandler.getStory() == null) {
            linearlayout.addChild(Button.builder(Component.literal("Credits"), button -> {
                CreditsScreen screen = new CreditsScreen(true, false);
                minecraft.setScreen(screen);
            }).width(200).build());
        }

    }

    @Override
    protected void addOptions() {}
}
