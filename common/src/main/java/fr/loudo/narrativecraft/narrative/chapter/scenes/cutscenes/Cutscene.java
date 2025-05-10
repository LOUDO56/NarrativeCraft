package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cutscene {

    private List<KeyframeGroup> keyframeGroupList;
    private List<Subscene> subsceneList;
    //private Subscene defaultSubcene;

    public Cutscene() {
        this.keyframeGroupList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
    }

    public List<KeyframeGroup> getKeyframeGroupList() {
        return keyframeGroupList;
    }

    public void setKeyframePathList(List<KeyframeGroup> keyframeGroupList) {
        this.keyframeGroupList = keyframeGroupList;
    }

    public List<Subscene> getSubsceneList() {
        return subsceneList;
    }

    public void setSubsceneList(List<Subscene> subsceneList) {
        this.subsceneList = subsceneList;
    }

}
