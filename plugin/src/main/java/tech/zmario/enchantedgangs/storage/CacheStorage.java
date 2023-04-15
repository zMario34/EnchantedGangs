package tech.zmario.enchantedgangs.storage;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.api.enums.RankingType;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;

import java.util.*;

@Getter
public class CacheStorage {

    private final TreeMap<String, Gang> loadedGangs = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, User> users = Maps.newHashMap();

    private final Map<UUID, UUID> acceptRequests = Maps.newHashMap();

    private final Map<Integer, String> ranks = Maps.newHashMap();

    public void addGang(@NotNull Gang gang) {
        Objects.requireNonNull(gang, "gang cannot be null! (CacheStorage#addGang)");

        loadedGangs.put(gang.getName(), gang);
    }

    public void removeGang(Gang gang) {
        loadedGangs.remove(gang.getName());
    }

    public Gang getGang(@NotNull String gangName) {
        Objects.requireNonNull(gangName, "gangName cannot be null! (CacheStorage#getGang)");

        return loadedGangs.get(gangName);
    }

    public Gang getGang(RankingType rankingType, int position) {
        if (position < 1 || position > loadedGangs.size()) {
            return null;
        }
        Gang[] gangs = loadedGangs.values().toArray(new Gang[0]);

        Arrays.sort(gangs, Comparator.comparingDouble(gang -> {
            if (rankingType == RankingType.BALANCE) {
                return gang.getBalance();
            } else {
                return gang.getKills();
            }
        }));

        position -= 1;

        if (gangs.length < position) {
            return null;
        }

        return gangs[position];
    }

    public void addUser(@NotNull User user) {
        Objects.requireNonNull(user, "user cannot be null! (CacheStorage#addUser)");

        users.put(user.getUuid(), user);
    }

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public void addAcceptRequest(UUID from, UUID to) {
        acceptRequests.put(from, to);
    }

    public void removeAcceptRequest(UUID from, UUID to) {
        acceptRequests.remove(from, to);
    }

    public UUID getAcceptRequest(UUID from) {
        return acceptRequests.get(from);
    }

    public boolean hasAcceptRequest(UUID from) {
        return acceptRequests.containsKey(from);
    }

    public boolean hasReceivedAcceptRequest(UUID uniqueId) {
        return acceptRequests.containsValue(uniqueId);
    }

    public int getLowestRank() {
        return ranks.size();
    }

    public void addRank(int rank, String name) {
        ranks.put(rank, name);
    }

    public boolean rankExists(int rank) {
        return ranks.containsKey(rank);
    }

    public String getRankName(int newRank) {
        return ranks.get(newRank);
    }
}
