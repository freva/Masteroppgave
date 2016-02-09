package com.freva.masteroppgave.utils;


public class ProgressBar {
    private static final String fullProgress = "====================================================================================================";
    private static final String noProgress =   "                                                                                                    ";
    private long taskStart = System.currentTimeMillis();
    private long lastProgress = 0;
    private int taskSize = 0;


    public ProgressBar(int taskSize) {
        this.taskSize = taskSize;
    }

    public void printProgress(int current) {
        if(System.currentTimeMillis()-lastProgress < 1000) return;

        double perc = 100.0 * current/taskSize;
        String timeElapsed = convertSecondsToHMmSs((System.currentTimeMillis()-taskStart)/1000);
        String progress = "[" + fullProgress.substring(0, (int) perc) + noProgress.substring(0, (int) (100-perc)) + "]";
        System.out.print("\r" + progress + " | " + timeElapsed + " | " + String.format("%.2f", perc) + "%");

        lastProgress = System.currentTimeMillis();
    }

    private static String convertSecondsToHMmSs(long seconds) {
        long sec = seconds % 60;
        long min = (seconds / 60) % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
