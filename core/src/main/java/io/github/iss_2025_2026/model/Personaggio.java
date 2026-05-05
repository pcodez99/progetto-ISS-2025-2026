package io.github.iss_2025_2026.model;
/** Rappresenta un personaggio generico nel gioco(Eroe, Nemico o Boss */
public abstract class Personaggio {
    private String nome;
    private int hp;
    private int hpMax;
    private int dannoBase;
    /** costruttore della classe con verifiche di sicurezza su hp(minimo 1) e dannoBase(minimo 1) */
    public Personaggio(String nome,int hpMax,int dannoBase) {
        this.nome = nome;
        //verificare parametri per evitare possibili bug
        this.hpMax = Math.max(1, hpMax);
        this.hp = this.hpMax;
        this.dannoBase = Math.max(1, dannoBase);
    }
    /** metodo sicuro per ricevere danni(evita HP scendono sotto lo zero)
     * @param quantita La quantità di danno da subire
     */
    public void riceviDanno(int quantita){
        if(quantita>0){
            this.hp-=quantita;
            if(this.hp<0){
                this.hp=0;
            }
        }
    }
    /** metodo per controllare se un personaggio è ancora in vita
     */
    public boolean isVivo(){
        return this.hp>0;
    }
    // GETTER(solo lettura per mantenere incapsulamento)
    public String getNome(){
        return nome;
    }
    public int getHp(){
        return hp;
    }
    public int getHpMax(){
        return hpMax;
    }
    public int getDannoBase(){
        return dannoBase;
    }
}
