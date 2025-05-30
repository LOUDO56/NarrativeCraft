package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class CharacterStory extends NarrativeEntry {

    private transient LivingEntity entity;
    private String birthdate;
    private CharacterType characterType;
    //TODO: custom skin, one string attr to means either a player name, mineskin url or local skin path.
    //TODO: Custom dialogue of character options here?
    //TODO: CharacterLayer class to enable certain part of body of the character


    public CharacterStory(String name) {
        super(name, "");
        characterType = CharacterType.MAIN;
    }

    public CharacterStory(String name, String description, String day, String month, String year) {
        super(name, description);
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        characterType = CharacterType.MAIN;
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

    public void update(String name, String description, String day, String month, String year) {
        NarrativeCraftFile.removeCharacterFile(this);
        String oldName = this.name;
        String oldDescription = this.description;
        String oldBirthdate = this.birthdate;
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        if(!NarrativeCraftFile.updateCharacterFile(this)) {
            this.name = oldName;
            this.description = oldDescription;
            this.birthdate = oldBirthdate;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    @Override
    public void update(String name, String description) {}

    @Override
    public void remove() {
        NarrativeCraftMod.getInstance().getCharacterManager().removeCharacter(this);
        NarrativeCraftFile.removeCharacterFile(this);
    }

    @Override
    public Screen reloadScreen() {
        return new CharactersScreen();
    }

    public void kill() {
        if(entity != null) {
            entity.remove(Entity.RemovalReason.KILLED);
            if(entity instanceof FakePlayer fakePlayer) {
                NarrativeCraftMod.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID())));
            }
        }
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public enum CharacterType {
        MAIN,
        NPC
    }
}
