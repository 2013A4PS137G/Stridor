package com.example.stridor_app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Recorder class built on top of androids' Audio Record to record raw 16 bit PCM data at 16000 sampling rate, 1 channel and save to wav file in storage
 * Has controls to start and stop recording
 */
public class MyRecorder {
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    public String filepath = "";
    public String ffile = "";
    public String ts = "";
    public String final_file_name = "";
    public long duration = 0;

    /** constructor
     *
     * @param file_path
     */
    public MyRecorder(String file_path) {
        bufferSize = AudioRecord.getMinBufferSize
                (RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;

        audioData = new short [bufferSize]; //short array that pcm data is put into.
        isRecording = false;
        filepath = file_path;
        final_file_name = "";
    }

    /**
     * Gets recorded Audio filename
     * @return
     */
    public String getFilename(){
        return final_file_name;
    }

    /**
     * Utility function: adds timestamp to recording name
     * @return
     */
    private String getTSFilename(){
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        ts = sdf.format(now);
        final_file_name = file.getAbsolutePath() + "/" + ts +
                AUDIO_RECORDER_FILE_EXT_WAV;

        return final_file_name;
    }

    private String getTempFilename() {
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if (isRecording && tempFile.exists())
            tempFile.delete();

        return (tempFile.getAbsolutePath());
    }

    /**
     * Post processing after writing Audio file. set read-write-execute permissions
     * @return
     */
    public void postProcessing(){
        File f = new File(filepath,AUDIO_RECORDER_FOLDER);
        if(f.exists()){
            f.setReadable(true, false);
            f.setWritable(true, false);
            f.setExecutable(true, false);
        }
        f = new File(final_file_name);
        if(f.exists()){
            f.setReadable(true, false);
            f.setWritable(true, false);
            f.setExecutable(true, false);
        }
    }

    /**
     * starts recording and dumps raw audio data to file
     */
    public void startRecording() {
        isRecording = true;

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize);
        int i = recorder.getState();
        if (i==1)
            recorder.startRecording();

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    /**
     * dumps raw audio data to file
     */
    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if (null != os) {
            while(isRecording) {
                read = recorder.read(data, 0, bufferSize);
                if (read > 0){
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * stops recording, deletes temp file, writes wave headers and renames audio file, post processing
     */
    public void stopRecording() {
        isRecording = false;

        if (null != recorder){

            int i = recorder.getState();
            if (i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        ffile = getTSFilename();
        copyWaveFile(getTempFilename(),ffile);
        deleteTempFile();
        postProcessing();
    }

    /**
     * deletes temp file
     */
    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    /**
     * copies wave data and write wave headers to new file
     */
    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            this.duration = totalAudioLen/byteRate;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    // My Edit start

            short[] audioData1 = null;

            int n = 0;

            DataInputStream in1;
            in1 = new DataInputStream(in);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while ((n = in1.read()) != -1) {
                bos.write(n);
            }

            ByteBuffer bb = ByteBuffer.wrap(bos.toByteArray());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer sb = bb.asShortBuffer();
            audioData1 = new short[sb.capacity()];

            for (int i = 0; i < sb.capacity(); i++) {
                audioData1[i] = sb.get(i);
            }


            float mx = 0;
            for (int i = 0; i < audioData1.length; i++) {
                if (Math.abs(audioData1[i]) > mx)
                    mx = Math.abs(audioData1[i]);
            }

            for (int i = 0; i < audioData1.length; i++) {
                int c = Math.round(Short.MAX_VALUE * (audioData1[i]/ mx));
                if (c > Short.MAX_VALUE)
                    c = Short.MAX_VALUE;
                if (c < Short.MIN_VALUE)
                    c = Short.MIN_VALUE;
                audioData1[i] = (short) c;
            }

            byte[] end = new byte[audioData1.length * 2];
            ByteBuffer.wrap(end).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audioData1);
            for(int i = 0; i < end.length; i++) {
                out.write(end[i]);
                out.flush();
            }
            in.close();
            out.close();
            // My Edit end
    * Code for normalizing waveform bw -1 to 1*/

    /**
     * writes wave headers
     * @param out
     * @param totalAudioLen
     * @param totalDataLen
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException
    {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    /**
     * check if object is recording
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * gets duration of recording
     * @return
     */
    public long getDuration(){
        return this.duration;
    }

}
