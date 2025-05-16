package fr.loudo.narrativecraft.narrative.character;

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
}
