package fr.loudo.narrativecraft.narrative.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubsceneManager {

    private List<Subscene> subscenes;

    public SubsceneManager() {
        this.subscenes = new ArrayList<>();
    }

    public boolean addSubscene(Subscene subscene) {
        if(subscenes.contains(subscene)) return false;
        try {
            subscenes.add(subscene);
            NarrativeCraftFile.saveSubscene();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean removeSubscene(Subscene subscene) {
        if(!subscenes.contains(subscene)) return false;
        try {
            subscenes.remove(subscene);
            NarrativeCraftFile.saveSubscene();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<Subscene> getSubscenes() {
        return subscenes;
    }
}
