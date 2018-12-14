package com.fotoable.piano.midi.bean;

import java.util.List;

/**
 * 音节小组
 * Created by houfutian on 2017/6/14.
 */

public class MidiEventList {
    /**
     * 每组音节对应的时间点
     */
    private long mTime;
    /**
     * 音节组
     * mMidiNote.size一定>0
     */
    private List<MidiNote> mMidiNote;

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public List<MidiNote> getmMidiNote() {
        return mMidiNote;
    }

    public void setmMidiNote(List<MidiNote> mMidiNote) {
        this.mMidiNote = mMidiNote;
    }

    @Override
    public String toString() {
        return "MidiEventList{" +
                "mTime=" + mTime +
                ", mMidiNote=" + mMidiNote +
                '}';
    }
}
