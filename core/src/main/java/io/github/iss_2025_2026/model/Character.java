package io.github.iss_2025_2026.model;
/** Rappresenta un personaggio generico nel gioco(Eroe, Nemico o Boss */
public abstract class Character {
    private String name;
    private int hp;
    private int hpMax;
    private int baseDamage;
    protected int level=1;
    /** costruttore della classe con verifiche di sicurezza su hp(minimo 1) e dannoBase(minimo 1) */
    public Character(String name, int hpMax, int baseDamage) {
        this.name = name;
        //verificare parametri per evitare possibili bug
        this.hpMax = Math.max(1, hpMax);
        this.hp = this.hpMax;
        this.baseDamage = Math.max(1, baseDamage);
    }
    /** metodo sicuro per ricevere danni(evita HP scendono sotto lo zero)
     * @param amount La quantità di danno da subire
     */
    public void takeDamage(int amount){
        if(amount>0){
            this.hp-=amount;
            if(this.hp<0){
                this.hp=0;
            }
        }
    }

    public void heal(int amount){
        this.hp+=amount;
        if(this.hp>hpMax) {
            this.hp = hpMax;
        }
    }
    /** metodo per controllare se un personaggio è ancora in vita
     */
    public boolean isAlive(){
        return this.hp>0;
    }
    // GETTER(solo lettura per mantenere incapsulamento)
    public String getName(){
        return name;
    }
    public int getHp(){
        return hp;
    }
    public int getHpMax(){
        return hpMax;
    }
    public int getBaseDamage() {
        return baseDamage;
    }
    public int getLevel(){
        return this.level;
    }
    public void setLevel(int level){
        this.level=level;
    }
}
