package org.acme.battle.model;

import lombok.Data;

@Data
public class PartyMember {
    public Long id;
    public String userId;
    public Long heroId;
    public String heroName;
    public String villain;
    public Boolean fighting;
    public Long health;
    public Integer level;
}
