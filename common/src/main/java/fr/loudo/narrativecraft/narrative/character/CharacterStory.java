package fr.loudo.narrativecraft.narrative.character;

import com.google.common.io.Files;
import com.mojang.blaze3d.platform.NativeImage;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterStory extends NarrativeEntry {

    private transient LivingEntity entity;
    private transient CharacterSkinController characterSkinController;
    private PlayerSkin.Model model;
    private String birthdate;
    private CharacterType characterType;
    //TODO: custom skin, one string attr to means either a player name, mineskin url or local skin path.
    //TODO: Custom dialogue of character options here?
    //TODO: CharacterLayer class to enable certain part of body of the character

    public CharacterStory(String name) {
        super(name, "");
        characterType = CharacterType.MAIN;
        characterSkinController = new CharacterSkinController(this);
    }

    public CharacterStory(String name, String description, PlayerSkin.Model model, String day, String month, String year) {
        super(name, description);
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        this.model = model;
        characterType = CharacterType.MAIN;
        characterSkinController = new CharacterSkinController(this);
    }

    public void update(String name, String description, String day, String month, String year) {
        String oldName = this.name;
        String oldDescription = this.description;
        String oldBirthdate = this.birthdate;
        this.name = name;
        this.description = description;
        this.birthdate = day + "/" + month + "/" + year;
        if(!NarrativeCraftFile.updateCharacterFolder(oldName, name)) {
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
        NarrativeCraftFile.removeCharacterFolder(this);
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

    public String getBirthdate() {
        return birthdate;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public PlayerSkin.Model getModel() {
        return model;
    }

    public void setModel(PlayerSkin.Model model) {
        this.model = model;
    }

    public CharacterSkinController getCharacterSkinController() {
        return characterSkinController;
    }

    public void setCharacterSkinController(CharacterSkinController characterSkinController) {
        this.characterSkinController = characterSkinController;
    }

    public enum CharacterType {
        MAIN,
        NPC
    }
}
