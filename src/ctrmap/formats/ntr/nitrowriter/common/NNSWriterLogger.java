
package ctrmap.formats.ntr.nitrowriter.common;

public interface NNSWriterLogger {
	public void out(String str);
	public void err(String str);
	
	public static class DummyLogger implements NNSWriterLogger {

		@Override
		public void out(String str) {
			System.out.println(str);
		}

		@Override
		public void err(String str) {
			System.err.println(str);
		}
		
	}
}
