package fr.loudo.narrativecraft.screens.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListElementScreen extends OptionsSubScreen {

    private final List<StoryElementList.StoryEntryData> entries;
    private StoryElementList storyElementList;

    public ListElementScreen(Screen lastScreen, List<StoryElementList.StoryEntryData> entries) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("List Element Screen"));
        this.entries = entries;
    }

    @Override
    protected void addContents() {
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    @Override
    protected void addOptions() {}
}
