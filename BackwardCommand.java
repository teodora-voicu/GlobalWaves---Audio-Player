package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;



import static Users.User.getMessageObject;

@Getter
@Setter
public final class BackwardCommand {

    /**
     * Implements the Backward command
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyBackward(final Command command, final User user) {

        final int time = 90;

        if (user.getLoad() == null) {
            return getMessageObject("backward", user.getUsername(), command.getTimestamp(),
                    "Please load a source before Rewounding.");

        }
        if (user.getCurrentPodcast() == null) {
            return getMessageObject("backward", user.getUsername(),
                    command.getTimestamp(), "The loaded source is not a podcast.");

        }

        Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

        Integer totalPodcastDuration = 0;
        for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
            totalPodcastDuration += episode.getDuration();
        }


        while (playTime > totalPodcastDuration) {
            playTime -= totalPodcastDuration;
        }
        Integer totalPlayingDuration = 0;
        for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
            if (playTime < episode.getDuration()) {
                if (playTime < time) {
                    PlayPauseCommand playPlause = new PlayPauseCommand(
                            command.getTimestamp() - totalPlayingDuration);
                    user.setPlayPause(playPlause);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(), "Rewound successfully.");
                } else {
                    PlayPauseCommand playPlause = new PlayPauseCommand(
                            command.getTimestamp() - totalPlayingDuration + time - playTime);
                    user.setPlayPause(playPlause);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(), "Rewound successfully.");
                }
            }
            totalPlayingDuration += episode.getDuration();
            playTime -= episode.getDuration();
        }
        return null;
    }

}
