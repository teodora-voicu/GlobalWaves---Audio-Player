package applycommands;

import Commands.Command;
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
public final class NextCommand {

    /**
     * Implements the Next command.
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyNext(final Command command, final User user) {

        Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

        if (user.getCurrentSong() != null) {
            SongInput currentSong = user.getCurrentSong();
            if (user.getRepeatState() == 0) {
                user.setLoad(null);
                user.setPlayPause(null);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before skipping to the next track.");
            } else if (user.getRepeatState() == 1) {
                if (playTime >= currentSong.getDuration()) {
                    user.setLoad(null);
                    user.setPlayPause(null);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before skipping to the next track.");
                }
                PlayPauseCommand playPause = new PlayPauseCommand(
                        command.getTimestamp() - currentSong.getDuration());
                user.setPlayPause(playPause);
                user.setRepeatState(0);
                user.setPlaying(true);
                user.setRepeatString("No Repeat");
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Skipped to next track successfully. The current track is "
                                +
                                currentSong.getName() + ".");
            } else {
                PlayPauseCommand playPause = new PlayPauseCommand(
                        command.getTimestamp() - currentSong.getDuration());
                user.setPlayPause(playPause);
                user.setPlaying(true);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Skipped to next track successfully. The current track is "
                                +
                                currentSong.getName() + ".");
            }
        } else if (user.getCurrentPlaylist() != null) {
            List<SongInput> currentPlaylistSongsListPlaying;
            if (user.getShuffle() != null) {
                currentPlaylistSongsListPlaying = user.getShuffle().getPlaylistSongs();
            } else {
                currentPlaylistSongsListPlaying = user.getCurrentPlaylist().getPlaylistSongs();
            }
            if (user.getRepeatState() == 0) {
                SongInput currentSongPlaying = null;
                Integer totalPlayingTimeFromPlaylist = 0;
                for (SongInput song : currentPlaylistSongsListPlaying) {
                    totalPlayingTimeFromPlaylist += song.getDuration();
                    if (playTime < song.getDuration()) {
                        currentSongPlaying = song;
                        break;
                    }
                    playTime -= song.getDuration();
                }
                if (currentSongPlaying == null
                        ||
                        totalPlayingTimeFromPlaylist == user.getCurrentPlaylist().getDuration()) {
                    user.setLoad(null);
                    user.setPlayPause(null);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before skipping to the next track.");
                }
                PlayPauseCommand playPlause = new PlayPauseCommand(
                        command.getTimestamp() - totalPlayingTimeFromPlaylist);
                user.setPlayPause(playPlause);
                user.setPlaying(true);
                SongInput currentSongInput = user.getCurrentPlayingSongInput(
                        command.getTimestamp());
                if (currentSongInput == null) {
                    user.setLoad(null);
                    user.setPlayPause(null);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before skipping to the next track.");
                }
                user.setPlaying(true);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Skipped to next track successfully. The current track is "
                                +
                                user.getCurrentPlayingSongInput(command.getTimestamp()).getName()
                                +
                                ".");
            } else if (user.getRepeatState() == 1) {
                while (playTime > user.getCurrentPlaylist().getDuration()) {
                    playTime -= user.getCurrentPlaylist().getDuration();
                }
                Integer totalPlayingTimeFromPlaylist = 0;
                for (SongInput song : currentPlaylistSongsListPlaying) {
                    totalPlayingTimeFromPlaylist += song.getDuration();
                    if (playTime < song.getDuration()) {
                        break;
                    }
                    playTime -= song.getDuration();
                }
                PlayPauseCommand playPlause;
                if (totalPlayingTimeFromPlaylist == user.getCurrentPlaylist().getDuration()) {
                    playPlause = new PlayPauseCommand(command.getTimestamp());
                } else {
                    playPlause = new PlayPauseCommand(command.getTimestamp()
                            -
                            totalPlayingTimeFromPlaylist);
                }

                user.setPlayPause(playPlause);
                user.setPlaying(true);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Skipped to next track successfully. The current track is "
                                +
                                user.getCurrentPlayingSongInput(command.getTimestamp()).getName()
                                +
                                ".");
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
                PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                        -
                        playingTimeBeforeCurrentRepeatedSong);
                user.setPlayPause(playPlause);
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Skipped to next track successfully. The current track is "
                                +
                                user.getCurrentPlayingSongInput(command.getTimestamp()).getName()
                                +
                                ".");
            }
        } else {
            Integer totalPodcastDuration = 0;
            for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                totalPodcastDuration += episode.getDuration();
            }

            if (user.getRepeatState() == 0) {
                Integer totalPlayingDuration  = 0;
                for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                    totalPlayingDuration += episode.getDuration();
                    if (playTime < episode.getDuration()) {
                        break;
                    }
                    playTime -= episode.getDuration();
                }
                if (playTime >= totalPodcastDuration
                        ||
                        totalPlayingDuration == totalPodcastDuration) {
                    user.setLoad(null);
                    user.setPlayPause(null);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before skipping to the next track.");
                } else {
                    PlayPauseCommand playPlause = new PlayPauseCommand(
                            command.getTimestamp() - totalPlayingDuration);
                    user.setPlayPause(playPlause);
                    user.setPlaying(true);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Skipped to next track successfully. The current track is "
                                    +
                                    user.getCurrentPlayingEpisode(command.getTimestamp()).getName()
                                    +
                                    ".");

                }
            } else if (user.getRepeatState() == 1) {
                if (playTime >= 2 * totalPodcastDuration) {
                    user.setLoad(null);
                    user.setPlayPause(null);
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before skipping to the next track.");
                } else {
                    if (playTime > totalPodcastDuration) {
                        user.setRepeatState(0);
                        user.setRepeatString("No Repeat");
                        playTime -= totalPodcastDuration;
                    }
                    Integer currentPlaying = 0;
                    for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                        currentPlaying += episode.getDuration();
                        if (playTime < episode.getDuration()) {
                            PlayPauseCommand playPlause = new PlayPauseCommand(
                                    command.getTimestamp() - currentPlaying);
                            user.setPlayPause(playPlause);
                            user.setPlaying(true);
                            return getMessageObject(command.getCommand(), command.getUsername(),
                                    command.getTimestamp(),
                                    "Skipped to next track successfully. The current track is "
                                            +
                                            user.getCurrentPlayingEpisode(
                                                    command.getTimestamp()).getName()
                                            +
                                            ".");
                        }
                        playTime -= episode.getDuration();
                    }
                }
            } else {
                while (playTime > totalPodcastDuration) {
                    playTime -= totalPodcastDuration;
                }
                Integer currentPlaying = 0;
                for (EpisodeInput episode : user.getCurrentPodcast().getEpisodes()) {
                    currentPlaying += episode.getDuration();
                    if (playTime < episode.getDuration()) {
                        PlayPauseCommand playPlause = new PlayPauseCommand(command.getTimestamp()
                                -
                                currentPlaying);
                        user.setPlayPause(playPlause);
                        user.setPlaying(true);
                        return getMessageObject(command.getCommand(), command.getUsername(),
                                command.getTimestamp(),
                                "Skipped to next track successfully. The current track is "
                                        +
                                        user.getCurrentPlayingEpisode(
                                                command.getTimestamp()).getName()
                                        +
                                        ".");
                    }
                    playTime -= episode.getDuration();
                }
            }
        }

        return null;
    }
}
