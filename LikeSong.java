package Library;

import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class LikeSong {
    private SongInput song;
    private Integer likes = 0;

    public LikeSong(final SongInput song, final Integer likes) {
        this.song = song;
        this.likes = likes;
    }
}
