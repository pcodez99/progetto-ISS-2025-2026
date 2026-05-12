package io.github.iss_2025_2026.model;
import java.util.*;

/**
 * Gestisce la collezione di oggetti di un personaggio
 * Implementa incapsulamento e protezione dati
 */
public class Zaino {
    private final List<Collezionabile> collezionabili;
    private final int capacitaMax;
    //Costruttore
    public Zaino(int capacitaMax){
        this.collezionabili=new ArrayList<>();
        this.capacitaMax=Math.max(1,capacitaMax);
    }

    /** Metodi per gestire lo zaino
     * aggiungere collezionabili
     * rimuovere collezionabili
     */
    public boolean aggiungiCollezionabile(Collezionabile c){
        if(c!=null && collezionabili.size()<capacitaMax){
            return collezionabili.add(c);
        }
        return false; //Zaino pieno o oggetto nullo
    }
    public void rimuoviCollezionabile(Collezionabile c){
        collezionabili.remove(c);
    }
    /**
     * Sicurezza: restituisce una vista della lista che non può essere in alcun modo modificata
     * Impedisce che classi esterne aggiungano oggetti
     * evitare venga evitata capacità dello zaino
     */
    public List<Collezionabile> getCollezionabili(){
        return Collections.unmodifiableList(collezionabili);
    }

    public int getCapacitaMax() {
        return capacitaMax;
    }
    public int getNumeroCollezionabili(){
        return collezionabili.size();
    }
}
