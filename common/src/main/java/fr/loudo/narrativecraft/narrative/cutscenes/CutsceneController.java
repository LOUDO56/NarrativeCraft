package fr.loudo.narrativecraft.narrative.cutscenes;

public class CutsceneController {

    private Cutscene cutscene;
    private boolean isPlaying;
    private int currentTick;

    public CutsceneController(Cutscene cutscene) {
        this.cutscene = cutscene;
        this.isPlaying = false;
        this.currentTick = 0;
    }
}
