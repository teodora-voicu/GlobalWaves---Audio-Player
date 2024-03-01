package applycommands;

import Commands.Command;
import Users.User;
import Library.Library;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.PlaylistInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static Users.User.getMessageObject;

@Setter
@Getter
public final class SelectCommand {

    private Integer itemNumber;

    private boolean selected = true;

    public SelectCommand(final Integer itemNumber) {
        this.itemNumber = itemNumber;
    }

    /**
     * Implements the Select command.
     *
     * @param command the given command.
     * @param user the user who requested a command.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applySelect(final Command command, final Library library,
                                  final User user, final List<PlaylistInput> allPlaylists) {

        if (user.getSearch().getType().equals("song")) {
            List<SongInput> songs = user.getSearch().searchSongs(library);
            if (command.getItemNumber() > songs.size()) {
                selected = false;
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(), "The selected ID is too high.");

            } else {
                SongInput song = songs.get(command.getItemNumber() - 1);
                user.setCurrentSong(song);
                user.setCurrentPodcast(null);
                user.setCurrentPlaylist(null);
                user.setSearch(null);
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(), "Successfully selected "
                                +
                                song.getName() + ".");

            }
        } else if (user.getSearch().getType().equals("podcast")) {
            List<PodcastInput> podcasts = user.getSearch().searchPodcasts(library);
            if (command.getItemNumber() > podcasts.size()) {
                selected = false;
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(),
                        "The selected ID is too high.");

            } else {
                PodcastInput podcast = podcasts.get(command.getItemNumber() - 1);
                user.setCurrentSong(null);
                user.setCurrentPodcast(podcast);
                user.setCurrentPlaylist(null);
                user.setSearch(null);
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(), "Successfully selected "
                                +
                                podcast.getName() + ".");

            }
        } else if (user.getSearch().getType().equals("playlist")) {
            List<PlaylistInput> playlists = user.getSearch().searchPlaylists(allPlaylists,
                    user.getUsername());
            if (command.getItemNumber() > playlists.size()) {
                selected = false;
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(), "The selected ID is too high.");

            } else {
                PlaylistInput playlist = playlists.get(command.getItemNumber() - 1);
                user.setCurrentSong(null);
                user.setCurrentPodcast(null);
                user.setCurrentPlaylist(playlist);
                user.setSearch(null);
                return getMessageObject("select", user.getUsername(),
                        command.getTimestamp(), "Successfully selected "
                                +
                                playlist.getName() + ".");

            }
        }

        return null;
    }
}
