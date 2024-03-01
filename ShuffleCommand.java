package applycommands;

import Commands.Command;
import Users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static Users.User.getMessageObject;


@Getter
@Setter
public final class ShuffleCommand {

    private Integer shuffleStartTime;

    private List<SongInput> playlistSongs;


    public ShuffleCommand(final List<SongInput> playlistSongs) {
        this.playlistSongs = new ArrayList<>(playlistSongs);
    }
    /**
     * Implements the Shuffle command
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyShuffle(final Command command, final User user) {
        SongInput currentPlayingSongInput = user.getCurrentPlayingSongInput(command.getTimestamp());
        Collections.shuffle(playlistSongs, new Random(command.getSeed()));


        Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

        if (user.getRepeatState() == 0) {
            for (SongInput song : user.getCurrentPlaylist().getPlaylistSongs()) {
                if (currentPlayingSongInput != song) {
                    playTime -= song.getDuration();
                    continue;
                }
                break;
            }

        } else if (user.getRepeatState() == 1) {
            while (playTime > user.getCurrentPlaylist().getDuration()) {
                playTime -= user.getCurrentPlaylist().getDuration();
            }
            for (SongInput song : user.getCurrentPlaylist().getPlaylistSongs()) {
                if (currentPlayingSongInput != song) {
                    playTime -= song.getDuration();
                    continue;
                }
                break;
            }
        } else {

            SongInput infinteSongRepeat = user.getRepeat().getInfinteSongRepeat();
            int playingTimeBeforeCurrentRepeatedSong = 0;
            for (SongInput songInput : user.getCurrentPlaylist().getPlaylistSongs()) {
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
        }

        int totalTime = playTime;
        for (SongInput songInput : playlistSongs) {
            if (songInput != currentPlayingSongInput) {
                totalTime += songInput.getDuration();
            } else {
                break;
            }
        }
        shuffleStartTime = command.getTimestamp() - totalTime;
        PlayPauseCommand playPause = new PlayPauseCommand(shuffleStartTime);

        user.setPlayPause(playPause);
        if (!user.isPlaying()) {
            user.setPlaying(true);
            playPause.applyPlayPause(command, user);
        }
        return getMessageObject(command.getCommand(), command.getUsername(),
                command.getTimestamp(), "Shuffle function activated successfully.");
    }

    /**
     * Implements the shuffle deactivation
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode deactivate(final Command command, final User user) {
        SongInput currentPlayingSongInput = user.getCurrentPlayingSongInput(command.getTimestamp());

        Integer playTime = user.getPlayPause().getTotalPlaytime(command.getTimestamp(), user);

        if (user.getRepeatState() == 0) {
            for (SongInput song : playlistSongs) {
                if (currentPlayingSongInput != song) {
                    playTime -= song.getDuration();
                    continue;
                }
                break;
            }

        } else if (user.getRepeatState() == 1) {
            while (playTime > user.getCurrentPlaylist().getDuration()) {
                playTime -= user.getCurrentPlaylist().getDuration();
            }
            for (SongInput song : playlistSongs) {
                if (currentPlayingSongInput != song) {
                    playTime -= song.getDuration();
                    continue;
                }
                break;
            }
        } else {

            SongInput infinteSongRepeat = user.getRepeat().getInfinteSongRepeat();
            int playingTimeBeforeCurrentRepeatedSong = 0;
            for (SongInput songInput : playlistSongs) {
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
        }

        int totalTime = playTime;
        for (SongInput songInput : user.getCurrentPlaylist().getPlaylistSongs()) {
            if (songInput != currentPlayingSongInput) {
                totalTime += songInput.getDuration();
            } else {
                break;
            }
        }
        shuffleStartTime = command.getTimestamp() - totalTime;
        PlayPauseCommand playPause = new PlayPauseCommand(shuffleStartTime);
        user.setPlayPause(playPause);
        if (!user.isPlaying()) {
            user.setPlaying(true);
            playPause.applyPlayPause(command, user);
        }
        return getMessageObject(command.getCommand(), command.getUsername(),
                command.getTimestamp(), "Shuffle function deactivated successfully.");
    }
}
