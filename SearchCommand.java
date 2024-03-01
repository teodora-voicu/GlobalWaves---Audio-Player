package applycommands;

import Commands.Command;
import Commands.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.PlaylistInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import Library.Library;

@Setter
@Getter
public final class SearchCommand {

    private String type;
    private Filter filters;


    public SearchCommand(final String type, final Filter filters) {
        this.type = type;
        this.filters = filters;
    }


    /**
     * Implements the Search command
     *
     * @param command the given command.
     * @param library the library where we have the information stored.
     * @param allPlaylists all the playlists that exist.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applySearch(final Command command, final Library library,
                                  final List<PlaylistInput> allPlaylists) {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();

        ArrayNode results = objectMapper.createArrayNode();

        resultNode.put("command", command.getCommand());
        resultNode.put("user", command.getUsername());
        resultNode.put("timestamp", command.getTimestamp());


        switch (command.getType()) {

            case "song":
                List<SongInput> firstFiveSongs = searchSongs(library);
                resultNode.put("message", "Search returned " + firstFiveSongs.size() + " results");
                for (SongInput song : firstFiveSongs) {
                    results.add(song.getName());
                }
                resultNode.put("results", results);
                break;

            case "podcast":

                List<PodcastInput> firstFivePodcasts = searchPodcasts(library);
                resultNode.put("message", "Search returned "
                        +
                        firstFivePodcasts.size()
                        +
                        " results");
                for (PodcastInput podcast : firstFivePodcasts) {
                    results.add(podcast.getName());
                }
                resultNode.put("results", results);
                break;

            case "playlist":

                List<PlaylistInput> firstFivePlaylists = searchPlaylists(
                        allPlaylists, command.getUsername());
                resultNode.put("message", "Search returned "
                        +
                        firstFivePlaylists.size()
                        + " results");
                for (PlaylistInput playlist : firstFivePlaylists) {
                    results.add(playlist.getName());
                }
                resultNode.put("results", results);
                break;
        }


        return resultNode;
    }

    /**
     * Searches songs in the library.
     *
     * @param library the place where the information is stored.
     * @return the first 5 findings.
     */
    public List<SongInput> searchSongs(final Library library) {

        ArrayList<SongInput> songResults = new ArrayList<>();
        for (SongInput song : library.getSongs()) {
            if (filters.getName() != null) {
                if (!song.getName().startsWith(filters.getName())) {
                    continue;
                }
            }
            if (filters.getAlbum() != null) {
                if (!song.getAlbum().equals(filters.getAlbum())) {
                    continue;
                }
            }

            if (filters.getTags() != null) {
                if (!song.getTags().containsAll(filters.getTags())) {
                    continue;
                }
            }

            if (filters.getLyrics() != null) {
                if (!song.getLyrics().toLowerCase().contains(filters.getLyrics().toLowerCase())) {
                    continue;
                }
            }

            if (filters.getGenre() != null) {
                if (!song.getGenre().equalsIgnoreCase(filters.getGenre())) {
                    continue;
                }
            }

            if (filters.getReleaseYear() != null) {
                if (filters.getReleaseYear().startsWith("<")) {
                    String number = filters.getReleaseYear().substring(1);
                    int no = Integer.parseInt(number);
                    if (song.getReleaseYear() >= no) {
                        continue;
                    }
                } else if (filters.getReleaseYear().startsWith(">")) {
                    String number = filters.getReleaseYear().substring(1);
                    int no = Integer.parseInt(number);
                    if (song.getReleaseYear() <= no) {
                        continue;
                    }
                }
            }

            if (filters.getArtist() != null) {
                if (!song.getArtist().equals(filters.getArtist())) {
                    continue;
                }
            }

            songResults.add(song);
        }
        return songResults.subList(0, Math.min(songResults.size(), 5));
    }

    /**
     * Searches podcasts in the library.
     *
     * @param library the place where the information is stored.
     * @return the first 5 findings.
     */
    public List<PodcastInput> searchPodcasts(final Library library) {
        ArrayList<PodcastInput> podcastResults = new ArrayList<>();
        for (PodcastInput podcast : library.getPodcasts()) {
            if (filters.getName() != null) {
                if (!podcast.getName().startsWith(filters.getName())) {
                    continue;
                }
            }
            if (filters.getOwner() != null) {
                if (!podcast.getOwner().equals(filters.getOwner())) {
                    continue;
                }
            }
            podcastResults.add(podcast);
        }
        return podcastResults.subList(0, Math.min(podcastResults.size(), 5));
    }


    /**
     * Searches playlists in all the users playlists.
     *
     * @param allPlaylists contains all the playlists made.
     * @return the first 5 findings.
     */
    public List<PlaylistInput> searchPlaylists(
            final List<PlaylistInput> allPlaylists, final String username) {
        ArrayList<PlaylistInput> playlistResults = new ArrayList<>();
        for (PlaylistInput playlist : allPlaylists) {
            if (filters.getName() != null) {
                if (!playlist.getName().startsWith(filters.getName())) {
                    continue;
                } else {
                    if (playlist.isPrivacy() && !playlist.getUsername().equals(username)) {
                        continue;
                    }
                }
            }
            if (filters.getOwner() != null) {
                if (!playlist.getUsername().equals(filters.getOwner())) {
                    continue;
                } else {
                    if (playlist.isPrivacy() && !playlist.getUsername().equals(username)) {
                        continue;
                    }
                }
            }
            playlistResults.add(playlist);
        }
        return playlistResults.subList(0, Math.min(playlistResults.size(), 5));
    }

}

