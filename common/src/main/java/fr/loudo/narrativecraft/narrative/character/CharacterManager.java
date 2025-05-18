package fr.loudo.narrativecraft.narrative.character;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private List<CharacterStory> characterStories;

    public CharacterManager() {
        this.characterStories = new ArrayList<>();
    }
    
    public void setCharacters(List<CharacterStory> characterStories) {
        this.characterStories = characterStories;
    }

    public LivingEntity getCharacterByName(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory.getEntity();
            }
        }
        return null;
    }

}
