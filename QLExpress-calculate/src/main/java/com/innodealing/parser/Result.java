package com.innodealing.parser;

public class Result {
	private String recognized;
	private String remaining;
	private boolean succeeded;
	private String  resolved;

	private Result(String recognized, String remaining, boolean succeeded) {
		this.recognized = recognized;
		this.remaining = remaining;
		this.succeeded = succeeded;
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public String getRecognized() {
		return recognized;
	}

	public String getRemaining() {
		return remaining;
	}

	public static Result succeed(String recognized, String remaining) {
		return new Result(recognized, remaining, true);
	}

	public static Result fail() {
		return new Result("", "", false);
	}

	public String getResolved() {
		return resolved;
	}

	public void setResolved(String resolved) {
		this.resolved = resolved;
	}

	@Override
	public String toString() {
		return "Result [recognized=" + recognized + ", remaining=" + remaining + ", succeeded=" + succeeded
				+ ", resolved=" + resolved + "]";
	}

	
	
	
}
