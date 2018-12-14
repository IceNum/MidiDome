package com.fotoable.piano.midi.bean;

/**
 * 音节对象
 * Created by houfutian on 2017/6/14.
 */

public class MidiNote {
    /**
     * 音量大小
     */
    private int velocity;
    /**
     * 音节播放的地址
     */
    private int noteValue;
    /**
     * 信道
     */
    private int channel;

    /**
     * 对应音符出现的时间
     */
    private long time;
    /**
     * 该音符处于第几列 处理防重叠问题
     * 根据每个音符的宽度,计算其所处的列
     * 1:表示第一列
     *
     */
    private int columnIndex;

    /**
     * item 的类型 比如: 方形 圆形
     */
    private int type;

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(int noteValue) {
        this.noteValue = noteValue;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MidiNote{" +
                "velocity=" + velocity +
                ", noteValue=" + noteValue +
                ", channel=" + channel +
                ", columnIndex=" + columnIndex +
                ", type=" + type +
                '}';
    }
}
