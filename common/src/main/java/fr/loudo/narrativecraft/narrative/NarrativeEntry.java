package fr.loudo.narrativecraft.narrative;

import net.minecraft.client.gui.screens.Screen;

public abstract class NarrativeEntry {

    protected String name, description;

    public NarrativeEntry(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract void update(String name, String description);
    public abstract void remove();
    public abstract Screen reloadScreen();
}
