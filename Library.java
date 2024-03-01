package Library;

import checker.CheckerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


@Setter
@Getter
public final class Library {

    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

    private ArrayList<SongInput> songs;
    private ArrayList<PodcastInput> podcasts;
    private ArrayList<UserInput> users;

    private static Library singleton;

    public static Library getInstance() throws IOException {
        if (singleton == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            singleton = objectMapper.readValue(new File(LIBRARY_PATH), Library.class);
        }
        return singleton;
    }

    private Library() {

    }
}
