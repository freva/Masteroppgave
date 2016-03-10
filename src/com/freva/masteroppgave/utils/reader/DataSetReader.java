package com.freva.masteroppgave.utils.reader;

import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class DataSetReader implements Iterator<DataSetEntry>, Iterable<DataSetEntry>, Progressable {
    private final LineReader lineReader;
    private final int tweetIndex;
    private final int classIndex;

    public DataSetReader(File file, int tweetIndex, int classIndex) throws IOException {
        lineReader = new LineReader(file);
        this.tweetIndex = tweetIndex;
        this.classIndex = classIndex;
    }


    @Override
    public boolean hasNext() {
        return lineReader.hasNext();
    }

    @Override
    public DataSetEntry next() {
        return new DataSetEntry(lineReader.next(), tweetIndex, classIndex);
    }


    public Iterator<DataSetEntry> iterator() {
        return this;
    }


    @Override
    public double getProgress() {
        return lineReader.getProgress();
    }
}
