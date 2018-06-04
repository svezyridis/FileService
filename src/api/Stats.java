package api;

public class Stats {
	private int writes=0;
	private int reads=0;
	public void incWrites() {
		this.writes+=1;
	}
	public void incReads() {
		this.reads+=1;
	}
	public int getWrites() {
		return this.writes;
	}
	public int getRreads() {
		return this.reads;
	}

}
