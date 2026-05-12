package io.github.iss_2025_2026.model;

public class LancioViddano implements AbilitaSpeciale{
    @Override public String getNome(){
        return "Lancio del viddano";
    }

    @Override
    public String getDescrizione() {
        return "La zappa viene lanciata come se fosse un boomerang e colpisce tutti i nemici";
    }
    @Override public void esegui(Personaggio u,Personaggio b, int l){
        //lascio vuoto perchè ancora non discusso combattimento
    }
}
