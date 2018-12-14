package com.fotoable.piano.midi;

import android.content.Context;
import android.util.Log;

import com.fotoable.piano.R;
import com.fotoable.piano.midi.bean.MidiEventBean;
import com.fotoable.piano.midi.bean.MidiEventList;
import com.fotoable.piano.midi.bean.MidiNote;
import com.fotoable.piano.utils.ResourceUtils;
import com.fotoable.piano.utils.ToastUtils;
import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.MidiTrack;
import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOn;
import com.pdrogfer.mididroid.event.meta.Tempo;
import com.pdrogfer.mididroid.util.MidiEventListener;
import com.pdrogfer.mididroid.util.MidiProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 音乐解析
 * Created by houfutian on 2017/6/14.
 */

public class MidiFileAnalysis {
    private static final String TAG = "MidiFileAnalysis";
    private Context context;
    private String fileName;
    private File file;
    private float bpm;

    public MidiEventBean midiEventBean = new MidiEventBean();
    private MidiEventList midiEventList;
    private MidiNote midiNote;
    private ArrayList list = new ArrayList();
    private ArrayList<MidiNote> item = new ArrayList();
    private long ticko = -1;
    private int columnIndex;

    public MidiFileAnalysis(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        file = new File(fileName);
        ResourceUtils.extractAsset(context, fileName, file);

    }

    public MidiEventBean getMidiEventBean() {

        if (file.exists() && file.isFile()) {
            try {
                int nodeMinIndex = Integer.MAX_VALUE;
                int nodeMaxIndex = 0;
                int minDeltaTime = Integer.MAX_VALUE;//最小的时间间隔
                long lastTrackStartTime = -1;
                MidiFile midiFile = new MidiFile(file);
                ArrayList<MidiTrack> tracks = midiFile.getTracks();
                List<MidiEvent> eventsToRemove = new ArrayList<>();
                for (MidiTrack track : tracks) {
                    Iterator<MidiEvent> it = track.getEvents().iterator();
                    while (it.hasNext()) {
                        MidiEvent event = it.next();
                        if ((event instanceof Tempo)) {
                            Tempo tempoEvent = (Tempo) event;
                            bpm = 120 / tempoEvent.getBpm();
//                            Log.i(TAG, "getBpm ====>" + tempoEvent.getBpm()+"   120/bpm=====>" + bpm );
                        }
                        if (event instanceof NoteOn) {
                            /**
                             * velocity 音节力度，<0过滤掉
                             */
                            int velocity = ((NoteOn) event).getVelocity();
                            if ( velocity > 0) {
                                eventsToRemove.add(event);
                            }
//                            long Tick = (long) (event.getTick()*bpm);
//                            int noteValue = ((NoteOn) event).getNoteValue();
//                            int Channel = ((NoteOn) event).getChannel();
//                            long Delta = event.getDelta();          //和上个音节的时间差
//                            Log.i(TAG,           "    Tick====>" + Tick
//                                                +"    velocity =>" + velocity
//                                                +"    noteValue===>" + noteValue
//                                                +"    Channel====>" + Channel
//                                                +"    Delta====>" + Delta
//                                                 );

                        }
                    }
                }

                if (eventsToRemove == null || eventsToRemove.size() < 1){
                    return null;
                }

                long gameTime = (long) (eventsToRemove.get(eventsToRemove.size() - 1).getTick() * bpm);
                midiEventBean.setGameTime(gameTime);
                midiEventBean.setNoteSize(eventsToRemove.size());
                for (MidiEvent event : eventsToRemove) {
                    if ((event instanceof NoteOn)) {
                        long tick = (long) (event.getTick() * bpm);
                        midiNote = new MidiNote();
                        midiNote.setVelocity(((NoteOn) event).getVelocity());
                        midiNote.setChannel(((NoteOn) event).getChannel());
                        midiNote.setNoteValue(((NoteOn) event).getNoteValue());
                        midiNote.setTime(tick);
                        midiNote.setType(0);    //默认圆形
                        int columnIndex = ((NoteOn) event).getNoteValue();
                        if (columnIndex > nodeMaxIndex) {
                            nodeMaxIndex = columnIndex;
                        }
                        if (columnIndex < nodeMinIndex) {
                            nodeMinIndex = columnIndex;
                        }

                        midiNote.setColumnIndex(columnIndex);

                        if (ticko != tick) {
                            ticko = tick;
                            midiEventList = new MidiEventList();
                            midiEventList.setmTime(tick);
                            if (lastTrackStartTime != -1) {
                                int tempDeltaTime = (int) (tick - lastTrackStartTime);
                                if (tempDeltaTime < minDeltaTime && tempDeltaTime > 0) {
                                    minDeltaTime = tempDeltaTime;
                                }
                            }
                            lastTrackStartTime = tick;


                            item = new ArrayList<>();
                            item.add(midiNote);
                            midiEventList.setmMidiNote(item);
                        } else {
                            if (item.size() < 4) {
                                item.add(midiNote);
                            }
                            midiEventList.setmMidiNote(item);
                            continue;
                        }
                        list.add(midiEventList);
                        midiEventBean.setMidiEventList(list);
                    }
                }
                midiEventBean.setNodeMaxIndex(nodeMaxIndex);
                midiEventBean.setNodeMinIndex(nodeMinIndex);
                midiEventBean.minDeltaTime = minDeltaTime;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return midiEventBean;
        } else {
            return null;
        }
    }

    /**
     * 一边播放一边解析
     */
    public void eventPrinter() {
        if (file.exists() && file.isFile()) {
            try {
                MidiFile midiFile = new MidiFile(file);
                EventPrinter eventPrinter = new EventPrinter();
                MidiProcessor midiProcessor = new MidiProcessor(midiFile);
//              midiProcessor.registerEventListener(eventPrinter, Tempo.class);
//              midiProcessor.registerEventListener(eventPrinter, NoteOn.class);
                midiProcessor.registerEventListener(eventPrinter, MidiEvent.class);

                // Start the processor:
                midiProcessor.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class EventPrinter implements MidiEventListener {

        @Override
        public void onStart(boolean b) {
            Log.i(TAG, "onStart=" + ",value =" + b);
        }

        @Override
        public void onEvent(MidiEvent midiEvent, long l) {
            Log.i(TAG, "midiEvent=" + midiEvent.toString() + ",value =" + l);
            long tick = midiEvent.getTick();
            int size = midiEvent.getSize();
            long delat = midiEvent.getDelta();
            Log.i(TAG, "midiEvent.getTick=" + midiEvent.getTick());
            if (midiEvent instanceof NoteOn) {
                int note = ((NoteOn) midiEvent).getNoteValue();
                int channel = ((NoteOn) midiEvent).getChannel();
                int veti = ((NoteOn) midiEvent).getVelocity();

                Log.i(TAG, "notevalue=" + note
                        + "   channel=" + channel
                        + "   veti=" + veti
                );

            }
        }

        @Override
        public void onStop(boolean b) {
            Log.i(TAG, "onStop=" + ",value =" + b);
        }
    }

}
