package io.github.iss_2025_2026.model;

public class PioggiaPietre implements AbilitaSpeciale{
    @Override public String getNome(){
        return "Pioggia di pietre";
    }

    @Override
    public String getDescrizione() {
        return "Lancia dalla fionda tantissime pietre contemporaneamente che colpiscono tutti i nemici";
    }
    @Override public void esegui(Personaggio u,Personaggio b, int l){
        //lascio vuoto perchè ancora non discusso combattimento
    }
}

