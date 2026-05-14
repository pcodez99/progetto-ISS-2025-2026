package io.github.iss_2025_2026.model;

/** Classe base per tutti gli oggetti collezionabili che possono trovarsi nel gioco.
 * Definisce il contratto che ogni oggetto deve seguire
 */
public abstract class Collectible {
    private final String name;
    private final String description;

    /**costruttore con criterio di sicurezza per quanto riguarda il valore null,
     in caso di valore null vengono assegnati delle stringhe specifiche così che
     il gioco nomn crashi.
     */
    public Collectible(String name, String description){
        this.name =(name != null)? name : "Unknown Item";
        this.description =(description !=null)? description : "";
    }
    /** definiamo un metodo astratto che definisce effetto dell'oggetto
     * ogni sottoclasse implementerà poi la sua logica
     * @param target Il personaggio su cui viene usato l'oggetto.
     */
    public abstract void use(Character target);
    //GETTER
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
}
