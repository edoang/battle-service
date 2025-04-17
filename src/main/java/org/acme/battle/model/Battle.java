package org.acme.battle.model;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Uni;
import lombok.Data;

import java.util.List;

@Data
public class Battle extends /*PanacheMongoEntity*/ ReactivePanacheMongoEntity {

    public String userId;
    public Long partyMemberId;
    public Boolean won;
    public String villain;

    // sync version with PanacheMongoEntity
//    public static List<Battle> findByUserAndWon(String userId, Boolean won) {
//        return find("userId = ?1 and won = ?2",
//                userId, won)
//                .list();
//    }

    // Reactive method to find battles by userId and won status
    public static Uni<List<Battle>> findByUserAndWon(String userId, Boolean won) {
        return find("userId = ?1 and won = ?2", userId, won).list();
    }

}
