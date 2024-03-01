package applycommands;

import Commands.Command;
import Library.Library;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static Users.User.getMessageObject;

@Setter
@Getter
public final class RepeatCommand {

    private int timeOfSettingTheRepeat = 0;

    private int currentLoopPlayingTime = 0;
    private SongInput infinteSongRepeat = null;

    /**
     * Implements the Repeat command.
     *
     * @param command the given command.
     * @param library the place where the information is stored.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyRepeat(final Command command, final Library library, final User user) {
        if (user.getCurrentPlaylist() != null) {


            List<SongInput> currentPlaylistSongsListPlaying;
            if (user.getShuffle() != null) {
                currentPlaylistSongsListPlaying = user.getShuffle().getPlaylistSongs();
            } else {
                currentPlaylistSongsListPlaying = user.getCurrentPlaylist().getPlaylistSongs();
            }
            if (user.getRepeatState() == 0) {
                user.setRepeatState(1);
                user.setRepeatString("Repeat All");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat all.");
            } else if (user.getRepeatState() == 1) {

                infinteSongRepeat = user.getCurrentPlayingSongInput(command.getTimestamp());
                user.setRepeatState(2);
                user.setRepeatString("Repeat Current Song");
                this.timeOfSettingTheRepeat = command.getTimestamp();
                Integer playTime = user.getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), user);
                while (playTime > user.getCurrentPlaylist().getDuration()) {
                    user.getPlayPause().setPausedTime(user.getPlayPause().getPausedTime()
                            +
                            user.getCurrentPlaylist().getDuration());
                    playTime -= user.getCurrentPlaylist().getDuration();
                }
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat current song.");

            } else if (user.getRepeatState() == 2) {
                user.setRepeatState(0);
                user.setRepeatString("No Repeat");
                Integer playTime = user.getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), user);
                int playingTimeBeforeCurrentRepeatedSong = 0;


                for (SongInput songInput : currentPlaylistSongsListPlaying) {
                    if (songInput != infinteSongRepeat) {
                        playingTimeBeforeCurrentRepeatedSong += songInput.getDuration();
                    } else {
                        break;
                    }
                }
                playTime -= playingTimeBeforeCurrentRepeatedSong;
                while (playTime > infinteSongRepeat.getDuration()) {
                    user.getPlayPause().setPausedTime(user.getPlayPause().getPausedTime()
                            +
                            infinteSongRepeat.getDuration());
                    playTime -= infinteSongRepeat.getDuration();
                }
                infinteSongRepeat = null;

                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to no repeat.");

            }
        } else if (user.getCurrentSong() != null) {
            if (user.getRepeatState() == 0) {
                user.setRepeatState(1);
                user.setRepeatString("Repeat Once");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat once.");

            } else if (user.getRepeatState() == 1) {
                user.setRepeatState(2);
                user.setRepeatString("Repeat Infinite");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat infinite.");

            } else if (user.getRepeatState() == 2) {
                user.setRepeatState(0);
                Integer totalPlayTime = user.getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), user);
                while (totalPlayTime > user.getCurrentSong().getDuration()) {
                    user.getPlayPause().setPausedTime(user.getPlayPause().getPausedTime()
                            +
                            user.getCurrentSong().getDuration());
                    totalPlayTime -= user.getCurrentSong().getDuration();
                }
                user.setRepeatString("No Repeat");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to no repeat.");

            }
        } else if (user.getCurrentPodcast() != null) {
            if (user.getRepeatState() == 0) {
                user.setRepeatState(1);
                user.setRepeatString("Repeat Once");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat once.");

            } else if (user.getRepeatState() == 1) {
                user.setRepeatState(2);
                user.setRepeatString("Repeat Infinite");
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to repeat infinite.");

            } else if (user.getRepeatState() == 2) {
                user.setRepeatState(0);
                user.setRepeatString("No Repeat");
                Integer totalPlayTime = user.getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), user);
                Integer totalPodcastDuration = 0;
                for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                    totalPodcastDuration += episode.getDuration();
                }
                while (totalPlayTime > totalPodcastDuration) {
                    totalPlayTime -= totalPodcastDuration;
                }
                return getMessageObject("repeat", user.getUsername(),
                        command.getTimestamp(),
                        "Repeat mode changed to no repeat.");

            }
        }
        return null;
    }
}
