package io.github.iss_2025_2026.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class PersonaggioTest {

        // Creiamo una classe concreta solo per il test,
        // dato che Personaggio è astratta e non si può istanziare direttamente.
        class PersonaggioFinto extends Personaggio {
            public PersonaggioFinto(String nome, int hpMax, int dannoBase) {
                super(nome, hpMax, dannoBase);
            }
        }

        @Test
        void testCreazioneSicura() {
            // Proviamo a creare un personaggio con HP negativi e danno negativo (cosa illegale)
            Personaggio p = new PersonaggioFinto("Cavia", -50, -10);

            // Verifichiamo che la nostra programmazione difensiva abbia corretto i valori
            assertEquals(1, p.getHpMax(), "Gli HP massimi dovrebbero essere forzati a 1");
            assertEquals(1, p.getDannoBase(), "Il danno dovrebbe essere forzato a 1");
        }

        @Test
        void testRiceviDannoNormale() {
            Personaggio p = new PersonaggioFinto("Cavia", 100, 10);
            p.riceviDanno(30);

            assertEquals(70, p.getHp(), "Gli HP dovrebbero scendere a 70");
            assertTrue(p.isVivo(), "Il personaggio dovrebbe essere ancora vivo");
        }

        @Test
        void testRiceviDannoLetale() {
            Personaggio p = new PersonaggioFinto("Cavia", 100, 10);
            p.riceviDanno(150); // Danno maggiore degli HP massimi

            assertEquals(0, p.getHp(), "Gli HP non dovrebbero mai scendere sotto lo 0");
            assertFalse(p.isVivo(), "Il personaggio dovrebbe essere morto");
        }

        @Test
        void testDannoNegativoIgnorato() {
            Personaggio p = new PersonaggioFinto("Cavia", 100, 10);
            p.riceviDanno(-20); // Danno negativo (bug del gioco)

            assertEquals(100, p.getHp(), "Il danno negativo deve essere ignorato, gli HP restano 100");
        }
    }
