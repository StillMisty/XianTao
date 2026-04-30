package top.stillmisty.xiantao.domain.monster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Team {

    private final Long ownerId;
    private final List<Combatant> members;
    private final String name;

    public Team(Long ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
        this.members = new ArrayList<>();
    }

    public void addMember(Combatant combatant) {
        members.add(combatant);
    }

    public List<Combatant> members() {
        return members;
    }

    public List<Combatant> aliveMembers() {
        return members.stream().filter(Combatant::isAlive).toList();
    }

    public boolean isAllDead() {
        return aliveMembers().isEmpty();
    }

    public int aliveCount() {
        return aliveMembers().size();
    }

    public Long ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public Combatant selectTargetForPVE() {
        List<Combatant> alive = aliveMembers();
        if (alive.isEmpty()) return null;
        return alive.stream()
                .min(Comparator.comparingDouble(c -> (double) c.getHp() / c.getMaxHp()))
                .orElse(alive.getFirst());
    }

    public Combatant selectTargetRandom() {
        List<Combatant> alive = aliveMembers();
        if (alive.isEmpty()) return null;
        return alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
    }
}
