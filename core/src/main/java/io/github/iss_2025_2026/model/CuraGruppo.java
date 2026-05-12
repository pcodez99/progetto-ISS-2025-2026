package io.github.iss_2025_2026.model;

public class CuraGruppo implements AbilitaSpeciale{
    @Override public String getNome(){
        return "Cura di gruppo";
    }

    @Override
    public String getDescrizione() {
        return "Cura tutti i giocatori contemporaneamente";
    }
    @Override public void esegui(Personaggio u,Personaggio b, int l){
        //lascio vuoto perchè ancora non discusso combattimento
    }
}
