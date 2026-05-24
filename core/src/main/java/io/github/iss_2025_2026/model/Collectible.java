package io.github.iss_2025_2026.model;

/** Represents an item that can be collected and put into the backpack */
public class Collectible {
    private String id;
    private String name;
    private String description;
    private String effectType;  //tipo di oggetto:damage, heal, buff
    private boolean aoe;        //true se bersaglio multiplo, false per bersaglio singolo
    private int effectValue;    //quanto vale il danno la cura o il buff

    //Costruttore vuoto che serve spesso a Jackson per leggere gli YAML
    public Collectible() {}

    public Collectible(String id, String name, String description, String effectType, boolean aoe, int effectValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.effectType = effectType;
        this.aoe = aoe;
        this.effectValue = effectValue;
    }

    //------Getters----
    public String getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getEffectType(){
        return effectType;
    }
    public boolean getAoe(){
        return aoe;
    }
    public int getEffectValue(){
        return effectValue;
    }

    //---------Setters-----------
    public void setId(String id){
        this.id=id;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public void setEffectType(String effectType){
        this.effectType=effectType;
    }
    public void setAoe(boolean aoe){
        this.aoe =aoe;
    }
    public void setEffectValue(int effectValue){
        this.effectValue=effectValue;
    }

}
