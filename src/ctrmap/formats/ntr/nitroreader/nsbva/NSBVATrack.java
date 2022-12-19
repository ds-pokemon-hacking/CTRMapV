package ctrmap.formats.ntr.nitroreader.nsbva;

import java.util.ArrayList;
import java.util.List;

public class NSBVATrack {
    public List<Boolean> animation;

    public NSBVATrack(int frameCount) {
        this.animation = new ArrayList(frameCount);
    }

    public void addFrame(boolean value) {
        this.animation.add(value);
    }
}
