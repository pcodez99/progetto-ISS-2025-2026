package io.github.iss_2025_2026.model;

/** Classe base per tutti gli oggetti collezionabili che possono trovarsi nel gioco.
 * Definisce il contratto che ogni oggetto deve seguire
 */
public abstract class Collezionabile {
    private final String nome;
    private final String descrizione;

    /**costruttore con criterio di sicurezza per quanto riguarda il valore null,
     in caso di valore null vengono assegnati delle stringhe specifiche così che
     il gioco nomn crashi.
     */
    public Collezionabile(String nome, String descrizione){
        this.nome=(nome != null)? nome : "Oggetto Ignoto";
        this.descrizione=(descrizione!=null)? descrizione: "";
    }
    /** definiamo un metodo astratto che definisce effetto dell'oggetto
     * ogni sottoclasse implementerà poi la sua logica
     * @param bersaglio Il personaggio su cui viene usato l'oggetto.
     */
    public abstract void usa(Personaggio bersaglio);
    //GETTER
    public String getNome(){
        return nome;
    }
    public String getDescrizione(){
        return descrizione;
    }
}
