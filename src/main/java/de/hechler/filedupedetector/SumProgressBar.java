package de.hechler.filedupedetector;

public class SumProgressBar extends ProgressBar {
	
	private long sum;
	
	public SumProgressBar() {
		super();
		this.sum = 0;
	}

	public boolean nextStep(long add) {
		this.sum += add;
		return super.nextStep();
	}
	
	public long getSum() {
		return sum;
	}
}
