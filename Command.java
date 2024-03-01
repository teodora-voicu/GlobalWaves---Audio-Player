package Commands;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class Command {
    private String command;
    private String username;
    private Integer timestamp;
    private String type;
    private Filter filters;
    private Integer itemNumber;
    private Integer seed;
    private Integer playlistId;
    private String playlistName;
}

