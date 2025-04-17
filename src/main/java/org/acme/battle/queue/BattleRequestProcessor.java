package org.acme.battle.queue;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.battle.model.Battle;
import org.acme.battle.model.BattleEnd;
import org.acme.battle.model.BattleRequest;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.UUID;
import java.util.random.RandomGenerator;

@ApplicationScoped
public class BattleRequestProcessor {

//    @Incoming("battles-run")
//    @Outgoing("battles-end")
//    public Message<BattleEnd> processBattleRequest(Message<BattleRequest> battleRequest) {
//
//        Log.info("Processing battle request: " + battleRequest.getPayload().partyMember);
//        Battle battle = new Battle();
//        battle.userId = battleRequest.getPayload().partyMember.userId;
//        battle.partyMemberId = battleRequest.getPayload().partyMember.id;
//        battle.villain = battleRequest.getPayload().partyMember.villain;
//        battle.won = RandomGenerator.getDefault().nextBoolean();
//        battle.persist();
//        Log.debug("Battle persisted for user: " + battle.userId);
//        Log.info("Battle done " + battleRequest.getPayload().partyMember.heroName + " vs " + battle.villain);
//        if (battle.won) {
//            battleRequest.getPayload().partyMember.health += 10L;
//            battleRequest.getPayload().isVictory = true;
//        } else {
//            battleRequest.getPayload().partyMember.health -= 10L;
//            battleRequest.getPayload().isVictory = false;
//        }
//        Log.info("isVictory: " + battleRequest.getPayload().isVictory);
//
//        BattleEnd end = new BattleEnd();
//        end.setId(UUID.randomUUID());
//        end.setPartyMember(battleRequest.getPayload().partyMember);
//        end.setGameId(battleRequest.getPayload().gameId);
//        end.setIsVictory(battleRequest.getPayload().isVictory);
//        return Message.of(end, () -> battleRequest.ack());
//
//    }

    /**
     * Processes the incoming battle request, simulates a battle between a party member and a villain,
     * updates the health of the party member based on the battle outcome, and sends a `BattleEnd` message.
     * The battle result is persisted to the database in a reactive manner.
     *
     * @param battleRequest the incoming message containing the {@link BattleRequest} payload with details
     *                      about the party member, the game ID, and other battle parameters.
     * @return a {@link Uni} emitting a {@link Message} containing the {@link BattleEnd} payload with the
     * result of the battle including victory status, game ID, and updated party member details.
     */
    @Incoming("battles-run")
    @Outgoing("battles-end")
    public Uni<Message<BattleEnd>> processBattleRequest(Message<BattleRequest> battleRequest) {
        Log.info("Processing battle request: " + battleRequest.getPayload().partyMember);

        Battle battle = new Battle();
        battle.userId = battleRequest.getPayload().partyMember.userId;
        battle.partyMemberId = battleRequest.getPayload().partyMember.id;
        battle.villain = battleRequest.getPayload().partyMember.villain;
        battle.won = calculateVictory(battleRequest.getPayload().partyMember.level); //RandomGenerator.getDefault().nextBoolean();

        // reactive
        return battle.persist()
                .onItem().transform(ignore -> {
                    Log.debug("Battle persisted for user: " + battle.userId);
                    Log.info("Battle done " + battleRequest.getPayload().partyMember.heroName + " vs " + battle.villain);
                    if (battle.won) {
                        battleRequest.getPayload().partyMember.health += 10L;
                        battleRequest.getPayload().isVictory = true;
                    } else {
                        battleRequest.getPayload().partyMember.health -= 10L;
                        battleRequest.getPayload().isVictory = false;
                    }
                    Log.info("isVictory: " + battleRequest.getPayload().isVictory);

                    // Build the BattleEnd message
                    BattleEnd end = new BattleEnd();
                    end.setId(UUID.randomUUID());
                    end.setPartyMember(battleRequest.getPayload().partyMember);
                    end.setGameId(battleRequest.getPayload().gameId);
                    end.setIsVictory(battleRequest.getPayload().isVictory);

                    // Return the message
                    return Message.of(end, () -> battleRequest.ack());
                });
    }

    public static boolean calculateVictory(int partyMemberLevel) {

        if (partyMemberLevel < 1) {
            throw new IllegalArgumentException("invalid level: " + partyMemberLevel + " - must be greater than 0");
        }

        //  50% -> 69%
        double baseWinProbability = 50 + (partyMemberLevel - 1) * (69 - 50) / 9.0;

        int winProbability = (int) Math.round(baseWinProbability);
        
        // rnd 1 - 100
        int randomValue = RandomGenerator.getDefault().nextInt(1, 101);

        return randomValue <= baseWinProbability;
    }


}