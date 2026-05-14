package io.github.iss_2025_2026.model;

/** Rappresenta un personaggio selezionabile dal giocatore
 * classe astratta che verrà estesa dai personaggi specifici(Nonno,Papà,Mamma e Bambino)
 */
public abstract class Player extends Character {
    private int level;
    private int karma; //da -50 a +50(egoismo e altruismo)
    private Backpack backpack;
    private SpecialAbility ability;

    public Player(String name, int hpMax, int baseDamage, SpecialAbility ability) {
        super(name, hpMax, baseDamage);
        this.level = 1;
        this.karma = 0;
        this.backpack = new Backpack(10);
        this.ability = ability;
    }
    /** Modifica il karma in modo sicuro, senza mai uscire dal range
     * @param variation Punti karma da aggiungere/sottrarre
     */
    public void modificaKarma(int variation){
        this.karma +=variation;
        if(this.karma>50) {
            this.karma = 50;
        } else if (this.karma < -50) {
            this.karma = -50;
        }
    }
    /** Utilizza la mossa speciale del personaggio
     */
    public void useSpecialAbility(Character target){
        if(this.ability !=null){
            this.ability.use(this,target,this.level);
        }
    }

    // GETTERS e SETTERS sicuri
    public int getLevel(){
        return level;
    }
    /** metodo che modifica il livello da usare nelle sottoclassi in levelUp
     */
    protected void levelUp(){
        this.level++;
    }
    public int getKarma(){
        return karma;
    }
    public Backpack getBackpack(){
        return backpack;
    }
    public SpecialAbility getAbility(){
    return ability;
    }
}
