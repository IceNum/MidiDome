package com.fotoable.piano.midi.bean;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * 包含所有音节的对象
 * Created by houfutian on 2017/6/14.
 */

public class MidiEventBean {
    /**
     * 曲子总时长
     */
    private long gameTime;
    /**
     * 整首曲子总音节数
     */
    private int noteSize;

    /**
     * 节拍数  240正常.
     */
    private int nodeTio;

    /**
     * 两个音节最小的时间间隔
     */
    public int minDeltaTime;

    /**
     * 最小的音节数
     */
    private int nodeMinIndex;
    /**
     * 最大的音节数(有平行音节时,累加比如: 比如当前是88且4个平行音节,则nodeMaxIndex=88+4=92)
     *
     */
    private int nodeMaxIndex;


    /**
     * 音节组对象集合
     */
    private List<MidiEventList> midiEventList;

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public int getNoteSize() {
        return noteSize;
    }

    public void setNoteSize(int noteSize) {
        this.noteSize = noteSize;
    }

    public List<MidiEventList> getMidiEventList() {
        return midiEventList;
    }

    public void setMidiEventList(List<MidiEventList> midiEventList) {
        this.midiEventList = midiEventList;
    }

    public int getNodeMinIndex() {
        return nodeMinIndex;
    }

    public void setNodeMinIndex(int nodeMinIndex) {
        this.nodeMinIndex = nodeMinIndex;
    }

    public int getNodeMaxIndex() {
        return nodeMaxIndex;
    }

    public void setNodeMaxIndex(int nodeMaxIndex) {
        this.nodeMaxIndex = nodeMaxIndex;
    }

    @Override
    public String toString() {
        return "MidiEventBean{" +
                "gameTime=" + gameTime +
                "noteSize=" + noteSize +
                ", midiEventList=" + midiEventList +
                '}';
    }
}