package de.hechler.filedupedetector;

public class ProgressBar {
	
	private long cnt;
	private long estimatedMax;
	private long cntStepsBeforeTimeCheck;
	private long nextTimeCheck;
	private long nextMessageTime;
	private long displayDelayMillis;
	
	public ProgressBar() {
		this.cnt = 0;
		this.estimatedMax = 0;
		this.cntStepsBeforeTimeCheck = 100;
		this.displayDelayMillis = 5*1000;
		this.nextTimeCheck = this.cnt + this.cntStepsBeforeTimeCheck;
		this.nextMessageTime = System.currentTimeMillis() + this.displayDelayMillis;
	}

	public boolean nextStep() {
		cnt += 1;
		if (cnt < nextTimeCheck) {
			return false;
		}
		nextTimeCheck = cnt + cntStepsBeforeTimeCheck;
		if (System.currentTimeMillis() < nextMessageTime) {
			return false;
		}
		nextMessageTime = System.currentTimeMillis() + displayDelayMillis;
		return true;
	}
	
	public String getPercentage() {
		if (estimatedMax == 0) {
			return Long.toString(cnt);
		}
		return Double.toString(100.0*cnt/estimatedMax);
	}
}
