package main;

import Commands.Command;
import Library.LikeSong;
import Users.User;
import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.LibraryInput;
import fileio.input.PlaylistInput;
import fileio.input.SongInput;
import fileio.input.UserInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     *
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePathInput  for input file
     * @param filePathOutput for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePathInput,
                              final String filePathOutput) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(LIBRARY_PATH), LibraryInput.class);

        ArrayNode outputs = objectMapper.createArrayNode();


        // TODO add your implementation

        ArrayList<Command> commands = objectMapper.readValue(
                new File("input/" + filePathInput),
                new TypeReference<ArrayList<Command>>() {
        });

        List<User> users = new ArrayList<>();

        List<PlaylistInput> allPlaylists = new ArrayList<>();


        for (UserInput user : library.getUsers()) {
            users.add(new User(user.getUsername()));
        }


        for (Command command : commands) {
            if (command.getCommand().equals("getTop5Songs")) {

                List<LikeSong> likesPerSong = new ArrayList<>();
                for (SongInput song : library.getSongs()) {
                    int count = 0;
                    for (User user : users) {
                        if (user.getPreferredSongs().contains(song.getName())) {
                            count++;
                        }
                    }
                    likesPerSong.add(new LikeSong(song, count));
                }
                likesPerSong.sort(new Comparator<LikeSong>() {
                    @Override
                    public int compare(final LikeSong likeSong1, final LikeSong likeSong2) {
                        if (likeSong2.getLikes().compareTo(likeSong1.getLikes()) == 0) {
                            return Integer.compare(library.getSongs().indexOf(likeSong1.getSong()), library.getSongs().indexOf(likeSong2.getSong()));
                        } else {
                            return likeSong2.getLikes().compareTo(likeSong1.getLikes());
                        }
                    }
                });

                ObjectNode resultNode = objectMapper.createObjectNode();

                ArrayNode results = objectMapper.createArrayNode();

                resultNode.put("command", command.getCommand());
                resultNode.put("timestamp", command.getTimestamp());
                for (LikeSong song : likesPerSong.subList(0, Math.min(likesPerSong.size(), 5))) {
                    results.add(song.getSong().getName());
                }
                resultNode.put("result", results);
                outputs.add(resultNode);
                continue;
            }
            if (command.getCommand().equals("getTop5Playlists")) {
                ObjectNode resultNode = objectMapper.createObjectNode();

                ArrayNode results = objectMapper.createArrayNode();

                resultNode.put("command", command.getCommand());
                resultNode.put("timestamp", command.getTimestamp());
                List<PlaylistInput> playlistInputsToBeSorted = new ArrayList<>(allPlaylists);
                playlistInputsToBeSorted.sort((playlist1, playlist2) -> playlist2.getFollowers().compareTo(playlist1.getFollowers()));
                for (PlaylistInput playlist : playlistInputsToBeSorted.subList(0, Math.min(playlistInputsToBeSorted.size(), 5))) {
                    results.add(playlist.getName());
                }
                resultNode.put("result", results);
                outputs.add(resultNode);
                continue;
            }

            User currentUser = getUserByName(users, command.getUsername());
            outputs.add(currentUser.applyCommand(command, allPlaylists));
        }


        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePathOutput), outputs);
    }

    private static User getUserByName(List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }
}
