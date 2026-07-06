package com.example.dungeonrpg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dungeonrpg.model.Enemy;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EncounterServiceTests {

    @Test
    void encountersEnemyWhenRandomValueIsBelowThirtyPercent() {
        EncounterService service = new EncounterService(new FixedRandom(0.29, 1));

        Optional<Enemy> enemy = service.findEncounter();

        assertThat(enemy).isPresent();
        assertThat(enemy.orElseThrow().getName()).isEqualTo("Skeleton");
    }

    @Test
    void doesNotEncounterEnemyWhenRandomValueIsThirtyPercentOrMore() {
        EncounterService service = new EncounterService(new FixedRandom(0.30, 0));

        assertThat(service.findEncounter()).isEmpty();
    }

    /** テスト結果を毎回同じにするためのRandomです。 */
    private static class FixedRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final double doubleValue;
        private final int intValue;

        FixedRandom(double doubleValue, int intValue) {
            this.doubleValue = doubleValue;
            this.intValue = intValue;
        }

        @Override
        public double nextDouble() {
            return doubleValue;
        }

        @Override
        public int nextInt(int bound) {
            return intValue;
        }
    }
}
