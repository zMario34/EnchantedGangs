package tech.zmario.enchantedgangs.api.objects;

import lombok.Data;

import java.util.UUID;

@Data
public class User {

    private final UUID uuid;

    private String gangName;
    private boolean gangChatEnabled;

}