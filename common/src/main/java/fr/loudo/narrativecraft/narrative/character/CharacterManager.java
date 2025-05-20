package fr.loudo.narrativecraft.narrative.character;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private List<CharacterStory> characterStories;

    public CharacterManager() {
        this.characterStories = new ArrayList<>();
    }

    public void init() {
        characterStories = new ArrayList<>();
        File characterDirectory = NarrativeCraftFile.characterDirectory;
        File[] characterFiles = characterDirectory.listFiles();
        if(characterFiles != null) {
            for(File characterFile : characterFiles) {
                try {
                    String content = Files.readString(characterFile.toPath());
                    CharacterStory characterStory = new Gson().fromJson(content, CharacterStory.class);
                    characterStories.add(characterStory);
                } catch (IOException e) {
                    throw new RuntimeException("Characters couldn't be loaded!: " + e);
                }
            }
        }
    }

    public List<CharacterStory> getCharacterStories() {
        return characterStories;
    }

    public void setCharacters(List<CharacterStory> characterStories) {
        this.characterStories = characterStories;
    }

    public LivingEntity getCharacterEntityByName(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory.getEntity();
            }
        }
        return null;
    }

    public boolean characterExists(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void addCharacter(CharacterStory characterStory) {
        characterStories.add(characterStory);
    }

    public void removeCharacter(CharacterStory characterStory) {
        characterStories.remove(characterStory);
    }
}
