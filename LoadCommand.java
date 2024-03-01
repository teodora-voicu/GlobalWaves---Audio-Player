package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import static Users.User.getMessageObject;

@Getter
@Setter
public final class LoadCommand {


    /**
     * Implements the Load command.
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyLoad(final Command command, final User user) {

        user.setSelect(null);
        user.setPlaying(true);
        PlayPauseCommand playpauseCommand;
        if (user.getCurrentPodcast() != null) {
            if (user.getPodcastsPlayTime().containsKey(user.getCurrentPodcast())) {
                playpauseCommand = new PlayPauseCommand(
                        command.getTimestamp()
                                -
                                user.getPodcastsPlayTime().get(
                                        user.getCurrentPodcast()));
            } else {
                playpauseCommand = new PlayPauseCommand(command.getTimestamp());
            }
        } else {
            playpauseCommand = new PlayPauseCommand(command.getTimestamp());
        }
        user.setPlayPause(playpauseCommand);
        user.setPlaying(true);
        return getMessageObject("load", user.getUsername(),
                command.getTimestamp(), "Playback loaded successfully.");


    }
}

