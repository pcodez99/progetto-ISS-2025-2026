package io.github.iss_2025_2026.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class PersonaggioTest {

        // Creiamo una classe concreta solo per il test,
        // dato che Character è astratta e non si può istanziare direttamente.
        class CharacterFinto extends Character {
            public CharacterFinto(String nome, int hpMax, int dannoBase) {
                super(nome, hpMax, dannoBase);
            }
        }

        @Test
        void testCreazioneSicura() {
            // Proviamo a creare un personaggio con HP negativi e danno negativo (cosa illegale)
            Character p = new CharacterFinto("Cavia", -50, -10);

            // Verifichiamo che la nostra programmazione difensiva abbia corretto i valori
            assertEquals(1, p.getHpMax(), "Gli HP massimi dovrebbero essere forzati a 1");
            assertEquals(1, p.getBaseDamage(), "Il danno dovrebbe essere forzato a 1");
        }

        @Test
        void testRiceviDannoNormale() {
            Character p = new CharacterFinto("Cavia", 100, 10);
            p.takeDamage(30);

            assertEquals(70, p.getHp(), "Gli HP dovrebbero scendere a 70");
            assertTrue(p.isAlive(), "Il personaggio dovrebbe essere ancora vivo");
        }

        @Test
        void testRiceviDannoLetale() {
            Character p = new CharacterFinto("Cavia", 100, 10);
            p.takeDamage(150); // Danno maggiore degli HP massimi

            assertEquals(0, p.getHp(), "Gli HP non dovrebbero mai scendere sotto lo 0");
            assertFalse(p.isAlive(), "Il personaggio dovrebbe essere morto");
        }

        @Test
        void testDannoNegativoIgnorato() {
            Character p = new CharacterFinto("Cavia", 100, 10);
            p.takeDamage(-20); // Danno negativo (bug del gioco)

            assertEquals(100, p.getHp(), "Il danno negativo deve essere ignorato, gli HP restano 100");
        }
    }
