package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static Users.User.getStatusObject;

@Setter
@Getter
public final class StatusCommand {

    private StatusCommand() {

    }
    /**
     * Implements the Status command
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public static ObjectNode applyStatus(final Command command, final User user) {
        if (user.getPlayPause() == null) {
            return getStatusObject(command.getCommand(), user.getUsername(), command.getTimestamp(),
                    "", 0, user.getRepeatString(), false, true);
        } else {
            boolean shuffle = false;
            if (user.getShuffle() != null) {
                shuffle = true;
            }
            Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

            if (user.getCurrentSong() != null) {
                SongInput currentSong = user.getCurrentSong();
                if (user.getRepeatState() == 0) {
                    if (playTime > currentSong.getDuration()) {
                        return getStatusObject(command.getCommand(), user.getUsername(),
                                command.getTimestamp(),
                                "", 0, user.getRepeatString(), false, true);
                    }
                    return getStatusObject(command.getCommand(), user.getUsername(),
                            command.getTimestamp(),
                            currentSong.getName(), currentSong.getDuration() - playTime,
                            user.getRepeatString(), false, !user.isPlaying());
                } else if (user.getRepeatState() == 1) {
                    if (playTime >= 2 * currentSong.getDuration()) {
                        return getStatusObject(command.getCommand(), user.getUsername(),
                                command.getTimestamp(),
                                "", 0, user.getRepeatString(), false, true);
                    }
                    if (playTime > currentSong.getDuration()) {
                        playTime -= currentSong.getDuration();
                        user.setRepeatState(0);
                        user.setRepeatString("No Repeat");
                    }
                    return getStatusObject(command.getCommand(), user.getUsername(),
                            command.getTimestamp(),
                            currentSong.getName(), currentSong.getDuration() - playTime,
                            user.getRepeatString(), false, !user.isPlaying());
                } else {
                    while (playTime > currentSong.getDuration()) {
                        playTime -= currentSong.getDuration();
                    }
                    return getStatusObject(command.getCommand(), user.getUsername(),
                            command.getTimestamp(),
                            currentSong.getName(), currentSong.getDuration() - playTime,
                            user.getRepeatString(), false, !user.isPlaying());
                }
            } else if (user.getCurrentPlaylist() != null) {

                List<SongInput> currentPlaylistSongsListPlaying;
                if (user.getShuffle() != null) {
                    currentPlaylistSongsListPlaying = user.getShuffle().getPlaylistSongs();
                } else {
                    currentPlaylistSongsListPlaying = user.getCurrentPlaylist().getPlaylistSongs();
                }
                if (user.getRepeatState() == 0) {
                    for (SongInput song : currentPlaylistSongsListPlaying) {
                        if (playTime < song.getDuration()) {
                            return getStatusObject(command.getCommand(), user.getUsername(),
                                    command.getTimestamp(), song.getName(), song.getDuration()
                                            - playTime, user.getRepeatString(), shuffle,
                                    !user.isPlaying());
                        }
                        playTime -= song.getDuration();
                    }
                    if (playTime >= 0) {
                        return getStatusObject(command.getCommand(), user.getUsername(),
                                command.getTimestamp(),
                                "", 0, user.getRepeatString(), false, true);
                    }
                } else if (user.getRepeatState() == 1) {
                    while (true) {
                        for (SongInput song : currentPlaylistSongsListPlaying) {
                            if (playTime < song.getDuration()) {
                                return getStatusObject(command.getCommand(), user.getUsername(),
                                        command.getTimestamp(), song.getName(), song.getDuration()
                                                - playTime, user.getRepeatString(), shuffle,
                                        !user.isPlaying());
                            }
                            playTime -= song.getDuration();
                        }
                    }
                } else {

                    SongInput infinteSongRepeat = user.getRepeat().getInfinteSongRepeat();
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
                        playTime -= infinteSongRepeat.getDuration();
                    }
                    return getStatusObject(command.getCommand(), user.getUsername(),
                            command.getTimestamp(), infinteSongRepeat.getName(),
                            infinteSongRepeat.getDuration() - playTime, user.getRepeatString(),
                            shuffle, !user.isPlaying());
                }
            } else {
                Integer totalPodcastDuration = 0;
                for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                    totalPodcastDuration += episode.getDuration();
                }
                if (user.getRepeatState() == 0) {
                    if (playTime >= totalPodcastDuration) {
                        return getStatusObject(command.getCommand(), user.getUsername(),
                                command.getTimestamp(), "", 0, user.getRepeatString(), false, true);
                    } else {
                        for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                            if (playTime < episode.getDuration()) {
                                return getStatusObject(command.getCommand(), user.getUsername(),
                                        command.getTimestamp(), episode.getName(),
                                        episode.getDuration() - playTime,
                                        user.getRepeatString(),
                                        false, !user.isPlaying());
                            }
                            playTime -= episode.getDuration();
                        }
                    }
                } else if (user.getRepeatState() == 1) {
                    if (playTime >= 2 * totalPodcastDuration) {
                        return getStatusObject(command.getCommand(), user.getUsername(),
                                command.getTimestamp(), "", 0, user.getRepeatString(), false, true);
                    } else {
                        if (playTime > totalPodcastDuration) {
                            user.setRepeatState(0);
                            user.setRepeatString("No Repeat");
                            playTime -= totalPodcastDuration;
                        }
                        for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                            if (playTime < episode.getDuration()) {
                                return getStatusObject(command.getCommand(), user.getUsername(),
                                        command.getTimestamp(), episode.getName(),
                                        episode.getDuration() - playTime, user.getRepeatString(),
                                        false, !user.isPlaying());
                            }
                            playTime -= episode.getDuration();
                        }
                    }
                } else {
                    while (playTime > totalPodcastDuration) {
                        playTime -= totalPodcastDuration;
                    }
                    for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                        if (playTime < episode.getDuration()) {
                            return getStatusObject(command.getCommand(), user.getUsername(),
                                    command.getTimestamp(), episode.getName(),
                                    episode.getDuration() - playTime, user.getRepeatString(),
                                    false, !user.isPlaying());
                        }
                        playTime -= episode.getDuration();
                    }
                }
            }
        }
        return null;
    }
}
