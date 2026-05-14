package io.github.iss_2025_2026.model;
import java.util.*;

/**
 * Gestisce la collezione di oggetti di un personaggio
 * Implementa incapsulamento e protezione dati
 */
public class Backpack {
    private final List<Collectible> collectibles;
    private final int maxCapacity;
    //Costruttore
    public Backpack(int maxCapacity){
        this.collectibles =new ArrayList<>();
        this.maxCapacity =Math.max(1, maxCapacity);
    }

    /** Metodi per gestire lo zaino
     * aggiungere collezionabili
     * rimuovere collezionabili
     */
    public boolean addCollectible(Collectible c){
        if(c!=null && collectibles.size()< maxCapacity){
            return collectibles.add(c);
        }
        return false; //Backpack pieno o oggetto nullo
    }
    public void removeCollectible(Collectible c){
        collectibles.remove(c);
    }
    /**
     * Sicurezza: restituisce una vista della lista che non può essere in alcun modo modificata
     * Impedisce che classi esterne aggiungano oggetti
     * evitare venga evitata capacità dello zaino
     */
    public List<Collectible> getCollectibles(){
        return Collections.unmodifiableList(collectibles);
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
    public int getCollectiblesNumber(){
        return collectibles.size();
    }
}
