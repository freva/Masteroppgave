package com.freva.masteroppgave.utils;


public class ProgressBar {
    private static final String fullProgress = "||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||";
    private static final String noProgress =   "----------------------------------------------------------------------------------------------------";
    private static final int updateResolution = 1000;
    private long taskStart = System.currentTimeMillis();
    private long lastProgress = 0;
    private int taskSize = 0;


    public ProgressBar(int taskSize) {
        this.taskSize = taskSize;
    }

    public void printProgress(int current) {
        if(System.currentTimeMillis()-lastProgress < updateResolution) return;

        System.out.print(getProgress(current));
    }

    private String getProgress(int current) {
        double perc = 100.0 * current/taskSize;

        long secondsElapsed = (System.currentTimeMillis()-taskStart)/1000;
        String timeElapsed = convertSecondsToMmSs(secondsElapsed);
        String timeRemaining = current != 0 ? convertSecondsToMmSs((taskSize-current)*secondsElapsed/current) : "Infin";
        String progress = getProgressBar(perc) + " Elapsed: " + timeElapsed + " | Remaining: " + timeRemaining;

        lastProgress = System.currentTimeMillis();
        return "\r" + progress;
    }

    public void finish() {
        System.out.println(getProgress(taskSize));
    }

    private static String getProgressBar(double percent) {
        String bar = fullProgress.substring(0, (int) percent) + noProgress.substring((int) percent);
        String status = (percent == 100 ? "Finish" : String.format("%05.2f%%", percent));
        return "[" + bar.substring(0, 47) + " " + status + " " + bar.substring(53) + "]";
    }

    private static String convertSecondsToMmSs(long seconds) {
        long sec = seconds % 60;
        long min = (seconds / 60) % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
