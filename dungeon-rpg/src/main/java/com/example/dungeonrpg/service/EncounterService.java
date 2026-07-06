package com.example.dungeonrpg.service;

import com.example.dungeonrpg.model.Enemy;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/** 敵とのランダム遭遇を担当します。 */
@Service
public class EncounterService {
    private static final double ENCOUNTER_RATE = 0.30;

    // Supplierを使い、遭遇するたびに新しいEnemyを作ります。
    private static final List<Supplier<Enemy>> ENEMY_FACTORIES = List.of(
            () -> new Enemy("Goblin", 18, 6, 2, 8),
            () -> new Enemy("Skeleton", 22, 7, 3, 12),
            () -> new Enemy("Slime", 14, 4, 1, 5)
    );

    private final Random random;

    public EncounterService() {
        this(new Random());
    }

    // テストでは結果を固定できるRandomを渡せます。
    EncounterService(Random random) {
        this.random = random;
    }

    public Optional<Enemy> findEncounter() {
        if (random.nextDouble() >= ENCOUNTER_RATE) {
            return Optional.empty();
        }

        int enemyIndex = random.nextInt(ENEMY_FACTORIES.size());
        return Optional.of(ENEMY_FACTORIES.get(enemyIndex).get());
    }
}
