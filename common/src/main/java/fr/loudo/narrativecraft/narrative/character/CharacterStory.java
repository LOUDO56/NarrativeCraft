package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;

public class CharacterStory extends NarrativeEntry {

    private transient LivingEntity entity;
    private int age;
    private String birthdate;
    //TODO: custom skin, one string attr to means either a player name, mineskin url or local skin path.
    //TODO: Custom dialogue of character options here?
    //TODO: CharacterLayer class to enable certain part of body of the character


    public CharacterStory(String name) {
        super(name, "");
    }

    public CharacterStory(String name, String description, int age, String birthdate) {
        super(name, description);
        this.name = name;
        this.description = description;
        this.age = age;
        this.birthdate = birthdate;
    }

    public int getAge() {
        return age;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void update(String name, String description) {

    }

    @Override
    public void remove() {

    }

    @Override
    public Screen reloadScreen() {
        return null;
    }
}
