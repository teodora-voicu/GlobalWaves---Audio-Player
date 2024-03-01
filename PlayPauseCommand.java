package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import static Users.User.getMessageObject;

@Getter
@Setter
public final class PlayPauseCommand {

    private Integer startTime;
    private Integer pausedTime;
    private Integer stopTime;

    public PlayPauseCommand(final Integer startTime) {
        this.startTime = startTime;
        this.pausedTime = 0;
        this.stopTime = -1;
    }

    /**
     * Implements the playPause command.
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyPlayPause(final Command command, final User user) {
        if (user.getLoad() == null) {
            return getMessageObject("playPause", user.getUsername(),
                    command.getTimestamp(),
                    "Please load a source before attempting to pause or resume playback.");
        } else if (!user.isPlaying()) {
            user.setPlaying(true);
            if (this.stopTime != -1) { // adica a fost data pauza inainte
                this.pausedTime += command.getTimestamp() - this.stopTime;
            }
            return getMessageObject("playPause", user.getUsername(),
                    command.getTimestamp(), "Playback resumed successfully.");

        } else {
            user.setPlaying(false);
            this.stopTime = command.getTimestamp();
            return getMessageObject("playPause", user.getUsername(),
                    command.getTimestamp(), "Playback paused successfully.");

        }
    }

    /**
     * Calculates the total amount of playing time.
     *
     * @param currentTime the current time.
     * @param user the user who requested a command.
     * @return the total amount of playing time.
     */
    public Integer getTotalPlaytime(final Integer currentTime, final User user) {
        if (user.isPlaying()) {
            return currentTime - startTime - pausedTime;
        } else {
            return stopTime - startTime - pausedTime;
        }
    }


}
