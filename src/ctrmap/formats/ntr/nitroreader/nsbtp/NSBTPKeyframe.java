package ctrmap.formats.ntr.nitroreader.nsbtp;

public class NSBTPKeyframe {
	public int frame;
	public String textureName;
    public String paletteName;

    public NSBTPKeyframe(int frame, String textureName, String paletteName) {
		this.frame = frame;
        this.textureName = textureName;
        this.paletteName = paletteName;
    }
}
