package org.acme.battle.model;


import lombok.Data;

import java.util.UUID;

@Data
public class BattleRequest {
    public UUID id;
    public PartyMember partyMember;
    public Long gameId;
    public Boolean isVictory;
}
