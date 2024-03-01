package Users;

import applycommands.*;
import Commands.Command;
import Library.Library;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import fileio.input.PlaylistInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static applycommands.StatusCommand.applyStatus;


@Setter
@Getter
public final class User {
    private String username;

    private SearchCommand search = null;
    private SelectCommand select = null;
    private LoadCommand load = null;
    private PlayPauseCommand playPause = null;
    private RepeatCommand repeat = null;
    private StatusCommand status = null;
    private BackwardCommand backward = null;
    private List<PlaylistInput> playlists;
    private SongInput currentSong = null;
    private PodcastInput currentPodcast = null;
    private PlaylistInput currentPlaylist = null;
    private ShuffleCommand shuffle = null;
    private boolean playing;
    private Integer repeatState = 0;
    private String repeatString = "No Repeat";
    private List<String> preferredSongs;
    private Map<PodcastInput, Integer> podcastsPlayTime;
    private List<PlaylistInput> follow;

    public User(final String username) {
        this.username = username;
        this.preferredSongs = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.podcastsPlayTime = new HashMap<>();
        follow = new ArrayList<>();
    }

    /**
     * Implements the logic of the program.
     *
     * @param command the given command.
     * @param allPlaylists all the playlists.
     * @return appropriate messages regarding the outcome of the command.
     */
    public ObjectNode applyCommand(final Command command,
                                   final List<PlaylistInput> allPlaylists) throws IOException {
        Library library = Library.getInstance();
        if (command.getCommand().equals("search")) {
            SearchCommand searchCommand = new SearchCommand(command.getType(),
                    command.getFilters());
            setSearch(searchCommand);
            if (getCurrentPodcast() != null && getLoad() != null) {
                Integer totalPodcastPlaytime = getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), this);
                podcastsPlayTime.put(getCurrentPodcast(), totalPodcastPlaytime);
            }
            setSelect(null);
            setLoad(null);
            setCurrentPlaylist(null);
            setCurrentPodcast(null);
            setCurrentSong(null);
            setPlayPause(null);
            setRepeatState(0);
            setRepeatString("No Repeat");
            return searchCommand.applySearch(command, library, allPlaylists);
        } else if (command.getCommand().equals("select")) {
            if (this.getSearch() == null) {
                return getMessageObject("select", this.getUsername(),
                        command.getTimestamp(),
                        "Please conduct a search before making a selection.");
            }
            SelectCommand selectCommand = new SelectCommand(command.getItemNumber());
            setSelect(selectCommand);
            return selectCommand.applySelect(command, library, this, allPlaylists);
        } else if (command.getCommand().equals("load")) {

            if (this.getSelect() == null || (this.getCurrentSong()
                    ==
                    null && this.getCurrentPodcast()
                    ==
                    null && this.getCurrentPlaylist() == null)) {
                return getMessageObject("load", this.getUsername(),
                        command.getTimestamp(),
                        "Please select a source before attempting to load.");
            }
            LoadCommand loadCommand = new LoadCommand();
            setLoad(loadCommand);

            return loadCommand.applyLoad(command, this);
        } else if (command.getCommand().equals("playPause")) {
            if (this.getLoad() == null) {
                return getMessageObject("playPause", this.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before attempting to pause or resume playback.");
            }

            return this.getPlayPause().applyPlayPause(command, this);
        } else if (command.getCommand().equals("repeat")) {
            if (this.getLoad() == null) {
                return getMessageObject("repeat", this.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before setting the repeat status.");
            }
            RepeatCommand repeatCommand;
            if (this.getRepeat() == null) {
                repeatCommand = new RepeatCommand();
                setRepeat(repeatCommand);
            } else {
                repeatCommand = this.getRepeat();
            }
            return repeatCommand.applyRepeat(command, library, this);
        } else if (command.getCommand().equals("createPlaylist")) {
            return this.createPlaylist(command.getPlaylistName(), command.getTimestamp(),
                    allPlaylists);
        } else if (command.getCommand().equals("status")) {
            return applyStatus(command, this);
        } else if (command.getCommand().equals("backward")) {
            BackwardCommand backwardCommand = new BackwardCommand();
            setBackward(backwardCommand);
            return backwardCommand.applyBackward(command, this);
        } else if (command.getCommand().equals("like")) {
            return applyLike(command);
        } else if (command.getCommand().equals("addRemoveInPlaylist")) {
            return applyAddRemoveInPlaylist(command);
        } else if (command.getCommand().equals("showPlaylists")) {
            return applyShowPlaylists(command);
        } else if (command.getCommand().equals("showPreferredSongs")) {
            return applyShowPreferredSongs(command);
        } else if (command.getCommand().equals("shuffle")) {
            if (getLoad() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before using the shuffle function.");
            }
            if (getCurrentPlaylist() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "The loaded source is not a playlist.");
            }

            Integer playTime = getPlayPause().getTotalPlaytime(command.getTimestamp(), this);
            if (repeatState == 0) {
                if (playTime >= getCurrentPlaylist().getDuration()) {
                    this.load = null;
                    this.playPause = null;
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before using the shuffle function.");
                }
            }
            if (repeatState == 1) {
                if (playTime >= 2 * getCurrentPlaylist().getDuration()) {
                    this.load = null;
                    this.playPause = null;
                    return getMessageObject(command.getCommand(), command.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before using the shuffle function.");
                }
            }

            if (getShuffle() != null) {
                shuffle.deactivate(command, this);
                shuffle = null;
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Shuffle function deactivated successfully.");
            }
            ShuffleCommand shuffleCommand = new ShuffleCommand(currentPlaylist.getPlaylistSongs());
            setShuffle(shuffleCommand);
            return shuffleCommand.applyShuffle(command, this);
        } else if (command.getCommand().equals("next")) {
            if (getLoad() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before skipping to the next track.");
            }
            NextCommand next = new NextCommand();
            return next.applyNext(command, this);

        } else if (command.getCommand().equals("prev")) {
            if (getLoad() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before returning to the previous track.");
            }
            PrevCommand prev = new PrevCommand();
            return prev.applyPrev(command, this);

        } else if (command.getCommand().equals("forward")) {
            if (getLoad() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before attempting to forward.");
            }

            if (getCurrentPodcast() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "The loaded source is not a podcast.");
            }

            ForwardCommand forward = new ForwardCommand();
            return forward.applyForward(command, this);
        } else if (command.getCommand().equals("follow")) {
            if (this.getSelect() == null || !this.getSelect().isSelected()) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Please select a source before following or unfollowing.");
            }
            if (this.getCurrentPlaylist() == null) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "The selected source is not a playlist.");
            }
            if (this.username.equals(this.getCurrentPlaylist().getUsername())) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "You cannot follow or unfollow your own playlist.");
            }

            if (this.follow.contains(currentPlaylist)) {
                this.follow.remove(currentPlaylist);
                currentPlaylist.decrementFollowers();
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Playlist unfollowed successfully.");
            } else {
                this.follow.add(currentPlaylist);
                currentPlaylist.incrementFollowers();
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Playlist followed successfully.");
            }
        } else if (command.getCommand().equals("switchVisibility")) {
            if (playlists.size() < command.getPlaylistId()) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "The specified playlist ID is too high.");
            }
            PlaylistInput playlistInput = playlists.get(command.getPlaylistId() - 1);
            playlistInput.switchVisibility();
            if (playlistInput.isPrivacy()) {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Visibility status updated successfully to private.");
            } else {
                return getMessageObject(command.getCommand(), command.getUsername(),
                        command.getTimestamp(),
                        "Visibility status updated successfully to public.");
            }
        }


        return null;
    }

    public static ObjectNode getMessageObject(final String command, final String user,
                                              final Integer timestamp, final String message) {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", command);
        objectNode.put("user", user);
        objectNode.put("timestamp", timestamp);
        objectNode.put("message", message);
        return objectNode;
    }

    public ObjectNode createPlaylist(final String playlistName, final Integer timestamp,
                                     final List<PlaylistInput> allPlaylists) {
        for (PlaylistInput currentPlaylist : this.playlists) {
            if (currentPlaylist.getName().equals(playlistName)) {
                return getMessageObject("createPlaylist", this.getUsername(),
                        timestamp, "A playlist with the same name already exists.");
            }
        }
        PlaylistInput newPlaylist = new PlaylistInput(playlistName, this.getUsername());
        this.playlists.add(newPlaylist);
        allPlaylists.add(newPlaylist);
        return getMessageObject("createPlaylist", this.getUsername(),
                timestamp, "Playlist created successfully.");

    }

    public static ObjectNode getStatusObject(final String command, final String user,
                                             final Integer timestamp, final String name,
                                             final Integer remainedTime, final String repeatString,
                                             final boolean shuffle, final boolean pause) {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ObjectNode statsobjectNode = objectMapper.createObjectNode();
        objectNode.put("command", command);
        objectNode.put("user", user);
        objectNode.put("timestamp", timestamp);
        statsobjectNode.put("name", name);
        statsobjectNode.put("remainedTime", remainedTime);
        statsobjectNode.put("repeat", repeatString);
        statsobjectNode.put("shuffle", shuffle);
        statsobjectNode.put("paused", pause);
        objectNode.put("stats", statsobjectNode);
        return objectNode;
    }

    private ObjectNode applyLike(final Command command) {
        if (this.getLoad() == null) {
            return getMessageObject("like", this.getUsername(),
                    command.getTimestamp(),
                    "Please load a source before liking or unliking.");
        } else if (currentPodcast != null) {
            return getMessageObject("like", this.getUsername(),
                    command.getTimestamp(),
                    "Loaded source is not a song.");
        } else if (currentSong != null) {
            if (this.getPlayPause().getTotalPlaytime(
                    command.getTimestamp(), this) > currentSong.getDuration()) {
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before liking or unliking.");
            } else if (preferredSongs.contains(currentSong.getName())) {
                preferredSongs.remove(currentSong.getName());
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Unlike registered successfully.");
            } else {
                preferredSongs.add(currentSong.getName());
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Like registered successfully.");
            }
        } else if (currentPlaylist != null) {
            SongInput currentSongPlaying = getCurrentPlayingSongInput(command.getTimestamp());
            if (currentSongPlaying == null) {
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Please load a source before liking or unliking.");
            } else if (preferredSongs.contains(currentSongPlaying.getName())) {
                preferredSongs.remove(currentSongPlaying.getName());
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Unlike registered successfully.");
            } else {
                preferredSongs.add(currentSongPlaying.getName());
                return getMessageObject("like", this.getUsername(),
                        command.getTimestamp(),
                        "Like registered successfully.");
            }
        }
        return null;
    }

    private ObjectNode applyAddRemoveInPlaylist(final Command command) {
        if (this.getLoad() == null) {
            return getMessageObject("addRemoveInPlaylist",
                    this.getUsername(), command.getTimestamp(),
                    "Please load a source before adding to or removing from the playlist.");
        } else if (currentPodcast != null) {
            return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                    command.getTimestamp(), "The loaded source is not a song.");
        } else {
            Integer indexOfThePlaylist = command.getPlaylistId() - 1;
            if (indexOfThePlaylist > playlists.size()) {
                return getMessageObject("addRemoveInPlaylist",
                        this.getUsername(), command.getTimestamp(),
                        "The specified playlist does not exist.");
            }
            PlaylistInput playlistInputToAddOrRemove = this.playlists.get(indexOfThePlaylist);
            if (currentSong != null) {
                if (this.getPlayPause().getTotalPlaytime(
                        command.getTimestamp(), this) > currentSong.getDuration()) {
                    return getMessageObject("addRemoveInPlaylist",
                            this.getUsername(), command.getTimestamp(),
                            "Please load a source before adding"
                                    +
                                    " to or removing from the playlist.");
                }
                if (playlistInputToAddOrRemove.getPlaylistSongs().contains(currentSong)) {
                    playlistInputToAddOrRemove.getPlaylistSongs().remove(currentSong);
                    return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                            command.getTimestamp(), "Successfully removed from playlist.");
                }
                playlistInputToAddOrRemove.getPlaylistSongs().add(currentSong);
                return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                        command.getTimestamp(), "Successfully added to playlist.");
            } else {
                SongInput currentSongPlaying = getCurrentPlayingSongInput(command.getTimestamp());
                if (currentSongPlaying == null) {
                    return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                            command.getTimestamp(),
                            "Please load a source before adding to or"
                                    +
                                    " removing from the playlist.");
                } else if (playlistInputToAddOrRemove.getPlaylistSongs().contains(
                        currentSongPlaying)) {
                    playlistInputToAddOrRemove.getPlaylistSongs().remove(currentSongPlaying);
                    return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                            command.getTimestamp(), "Successfully removed from playlist.");
                } else {
                    playlistInputToAddOrRemove.getPlaylistSongs().add(currentSongPlaying);
                    return getMessageObject("addRemoveInPlaylist", this.getUsername(),
                            command.getTimestamp(), "Successfully added to playlist.");
                }
            }
        }
    }

    private ObjectNode applyShowPlaylists(final Command command) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        objectNode.put("user", this.username);
        objectNode.put("timestamp", command.getTimestamp());
        ArrayNode result = objectMapper.createArrayNode();
        for (PlaylistInput playlist : playlists) {
            ObjectNode objectNodePlaylist = objectMapper.createObjectNode();
            objectNodePlaylist.put("name", playlist.getName());
            ArrayNode songsFromPlaylist = objectMapper.createArrayNode();
            for (SongInput song : playlist.getPlaylistSongs()) {
                songsFromPlaylist.add(song.getName());
            }
            objectNodePlaylist.put("songs", songsFromPlaylist);
            objectNodePlaylist.put("visibility", playlist.isPrivacy()
                    ?
                    "private" : "public");
            objectNodePlaylist.put("followers", playlist.getFollowers());
            result.add(objectNodePlaylist);
        }
        objectNode.put("result", result);
        return objectNode;
    }


    private ObjectNode applyShowPreferredSongs(final Command command) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        objectNode.put("user", this.username);
        objectNode.put("timestamp", command.getTimestamp());
        ArrayNode result = objectMapper.createArrayNode();
        for (String song : this.preferredSongs) {
            result.add(song);
        }
        objectNode.put("result", result);
        return objectNode;
    }

    public SongInput getCurrentPlayingSongInput(final Integer commandTimestamp) {
        SongInput currentSongPlaying = null;
        Integer playlistPlayTime = this.getPlayPause().getTotalPlaytime(commandTimestamp,
                this);

        List<SongInput> currentPlaylistSongsListPlaying;
        if (this.getShuffle() != null) {
            currentPlaylistSongsListPlaying = this.getShuffle().getPlaylistSongs();
        } else {
            currentPlaylistSongsListPlaying = this.getCurrentPlaylist().getPlaylistSongs();
        }
        for (SongInput song : currentPlaylistSongsListPlaying) {
            if (playlistPlayTime < song.getDuration()) {
                currentSongPlaying = song;
                break;
            }
            playlistPlayTime -= song.getDuration();
        }
        if (repeatState == 0) {
            return currentSongPlaying;
        } else if (repeatState == 1) {
            while (currentSongPlaying == null) {
                for (SongInput song : currentPlaylistSongsListPlaying) {
                    if (playlistPlayTime < song.getDuration()) {
                        currentSongPlaying = song;
                        break;
                    }
                    playlistPlayTime -= song.getDuration();
                }
            }
            return currentSongPlaying;
        } else {
            return this.getRepeat().getInfinteSongRepeat();
        }
    }

    public EpisodeInput getCurrentPlayingEpisode(final Integer commandTimestamp) {
        Integer playTime = this.getPlayPause().getTotalPlaytime(
                commandTimestamp, this);

        Integer totalPodcastDuration = 0;
        for (EpisodeInput episode : this.getCurrentPodcast().getEpisodes()) {
            totalPodcastDuration += episode.getDuration();
        }
        if (this.getRepeatState() == 0) {
            if (playTime >= totalPodcastDuration) {
                return null;
            } else {
                for (EpisodeInput episode : this.getCurrentPodcast().getEpisodes()) {
                    if (playTime < episode.getDuration()) {
                        return episode;
                    }
                    playTime -= episode.getDuration();
                }
            }
        } else if (this.getRepeatState() == 1) {
            if (playTime >= 2 * totalPodcastDuration) {
                return null;
            } else {
                if (playTime > totalPodcastDuration) {
                    this.setRepeatState(0);
                    this.setRepeatString("No Repeat");
                    playTime -= totalPodcastDuration;
                }
                for (EpisodeInput episode : this.getCurrentPodcast().getEpisodes()) {
                    if (playTime < episode.getDuration()) {
                        return episode;
                    }
                    playTime -= episode.getDuration();
                }
            }
        } else {
            while (playTime > totalPodcastDuration) {
                playTime -= totalPodcastDuration;
            }
            for (EpisodeInput episode : this.getCurrentPodcast().getEpisodes()) {
                if (playTime < episode.getDuration()) {
                    return episode;
                }
                playTime -= episode.getDuration();
            }
        }
        return null;
    }

}

