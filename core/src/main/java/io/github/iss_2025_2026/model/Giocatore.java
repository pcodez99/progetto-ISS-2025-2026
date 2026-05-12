package io.github.iss_2025_2026.model;

/** Rappresenta un personaggio selezionabile dal giocatore
 * classe astratta che verrà estesa dai personaggi specifici(Nonno,Papà,Mamma e Bambino)
 */
public abstract class Giocatore extends Personaggio{
    private int livello;
    private int karma; //da -50 a +50(egoismo e altruismo)
    private Zaino zaino;
    private AbilitaSpeciale abilita;

    public Giocatore(String nome, int hpMax,int dannoBase,AbilitaSpeciale abilita) {
        super(nome, hpMax, dannoBase);
        this.livello = 1;
        this.karma = 0;
        this.zaino = new Zaino(10);
        this.abilita = abilita;
    }
    /** Modifica il karma in modo sicuro, senza mai uscire dal range
     * @param variazione Punti karma da aggiungere/sottrarre
     */
    public void modificaKarma(int variazione){
        this.karma +=variazione;
        if(this.karma>50) {
            this.karma = 50;
        } else if (this.karma < -50) {
            this.karma = -50;
        }
    }
    /** Utilizza la mossa speciale del personaggio
     */
    public void usaAbilitaSpeciale(Personaggio bersaglio){
        if(this.abilita!=null){
            this.abilita.esegui(this,bersaglio,this.livello);
        }
    }
    /**
     * Metodo astratto con cui ogni personaggio dirà al sistema
     * quanto aumenteranno hp e danni quando aumenta di livello
     */
    public abstract void saliDiLivello();
    // GETTERS e SETTERS sicuri
    public int getLivello(){
        return livello;
    }
    /** metodo che modifica il livello da usare nelle sottoclassi in saliDiLivello
     */
    protected void aumentoLivello(){
        this.livello++;
    }
    public int getKarma(){
        return karma;
    }
    public Zaino getZaino(){
        return zaino;
    }
    public AbilitaSpeciale getAbilita(){
    return abilita;
    }
}
