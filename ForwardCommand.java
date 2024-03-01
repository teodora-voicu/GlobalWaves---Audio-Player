package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

import static Users.User.getMessageObject;

@Setter
@Getter
public final class ForwardCommand {

    static final int TIME = 90;

    /**
     * Implements the Forward command
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyForward(final Command command, final User user) {
        Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

        Integer totalPodcastDuration = 0;
        for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
            totalPodcastDuration += episode.getDuration();
        }

        if (user.getRepeatState() == 0) {
            Integer totalPlayingDuration = 0;
            EpisodeInput currentPlayingPodcastEpisode = null;
            for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                if (playTime < episode.getDuration()) {
                    currentPlayingPodcastEpisode = episode;
                    break;
                }
                totalPlayingDuration += episode.getDuration();
                playTime -= episode.getDuration();
            }
            if (playTime >= totalPodcastDuration || totalPlayingDuration == totalPodcastDuration) {
                user.setLoad(null);
                user.setPlayPause(null);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before attempting to forward.");
            } else {
                if (currentPlayingPodcastEpisode.getDuration() - playTime < TIME) {
                    PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                            - totalPlayingDuration - currentPlayingPodcastEpisode.getDuration());
                    user.setPlayPause(playPlause);
                } else {
                    PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                            - totalPlayingDuration - TIME - playTime);
                    user.setPlayPause(playPlause);
                }

                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(), "Skipped forward successfully.");
            }
        } else if (user.getRepeatState() == 1) {
            if (playTime >= 2 * totalPodcastDuration) {
                user.setLoad(null);
                user.setPlayPause(null);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before attempting to forward.");
            } else {
                if (playTime > totalPodcastDuration) {
                    user.setRepeatState(0);
                    user.setRepeatString("No Repeat");
                    playTime -= totalPodcastDuration;
                }
                Integer totalPlayingDuration = 0;
                for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {

                    if (playTime < episode.getDuration()) {
                        if (episode.getDuration() - playTime < TIME) {
                            PlayPauseCommand playPlause = new PlayPauseCommand(
                                    command.getTimestamp()
                                    - totalPlayingDuration - episode.getDuration());
                            user.setPlayPause(playPlause);
                        } else {
                            PlayPauseCommand playPlause = new PlayPauseCommand(
                                    command.getTimestamp()
                                            -
                                    totalPlayingDuration - TIME);
                            user.setPlayPause(playPlause);
                        }
                    }
                    totalPlayingDuration += episode.getDuration();
                    playTime -= episode.getDuration();
                }
            }
        } else {
            while (playTime > totalPodcastDuration) {
                playTime -= totalPodcastDuration;
            }
            Integer totalPlayingDuration = 0;
            for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {

                if (playTime < episode.getDuration()) {
                    if (episode.getDuration() - playTime < TIME) {
                        PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                                - totalPlayingDuration - episode.getDuration());
                        user.setPlayPause(playPlause);
                    } else {
                        PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                                - totalPlayingDuration - TIME);
                        user.setPlayPause(playPlause);
                    }
                }
                totalPlayingDuration += episode.getDuration();
                playTime -= episode.getDuration();
            }
        }
        return getMessageObject(command.getCommand(), command.getUsername(),
                command.getTimestamp(), "Skipped forward successfully.");
    }

}
