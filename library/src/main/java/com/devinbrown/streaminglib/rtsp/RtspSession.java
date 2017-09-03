package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.sdp.SessionDescription;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

abstract class RtspSession {
    InputStream mInput;
    OutputStream mOutput;
    Socket mSocket;
    Thread mThread;
    SessionDescription sessionDescription;
    RtspServerSession.RtspHandlerThread mHandlerThread;

    void sendRtspMessage(RtspMessage r) throws IOException {
        mOutput.write(r.getBytes());
        mOutput.flush();
    }
}
