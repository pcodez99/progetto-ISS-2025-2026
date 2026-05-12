package io.github.iss_2025_2026.model;

public class SaltoConCarrozzina implements AbilitaSpeciale{
    @Override public String getNome(){
        return "Salto con la carrozzina";
    }

    @Override
    public String getDescrizione() {
        return "Un salto che genera una potente onda d'urto che dannegga tutti i nemici";
    }
    @Override public void esegui(Personaggio u,Personaggio b, int l){
        //lascio vuoto perchè ancora non discusso combattimento
    }
}
