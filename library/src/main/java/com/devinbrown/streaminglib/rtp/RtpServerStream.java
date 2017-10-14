package com.devinbrown.streaminglib.rtp;

import android.util.Log;
import android.util.Pair;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtsp.Rtsp;
import com.devinbrown.streaminglib.rtsp.RtspInterleavedData;
import com.devinbrown.streaminglib.rtsp.RtspSession;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.SocketException;

public class RtpServerStream extends RtpStream {
    private static final String TAG = "RtpServerStream";

    public RtpServerStream(RtspSession s, RtpMedia m) {
        super(s, m);
        m.streamEventBus.register(this);
    }

    @Override
    public void initializeUdp() throws SocketException {
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.SERVER;
        delivery = Delivery.UNICAST;

        setupUdpPorts();

        state = RtpStreamState.INITIALIZED;
    }

    @Override
    public void initializeMulticast() {
        assert (false);
    }

    @Override
    public void initializeTcp(Pair<Integer, Integer> interleavedRtpChannels) {
        rtpProtocol = RtpProtocol.TCP;
        streamType = StreamType.SERVER;
        delivery = Delivery.UNICAST;
        this.interleavedRtpChannels = interleavedRtpChannels;
        state = RtpStreamState.INITIALIZED;
    }

    @Override
    public void configureTcp() {
        Log.d("RtpServerStream", "configureTcp: !!!!!!!!");
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.TCP);

        state = RtpStreamState.CONFIGURED;

        startListening();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.MediaDataReceived event) {
        Log.d(TAG, "handleEvent: RtspClientStreamEvent.MediaDataReceived");

        try {
            switch (rtpProtocol) {
                case UDP:
                    sendRtp(event.data);
                    break;
                case TCP:
                    Rtsp r = new RtspInterleavedData(interleavedRtpChannels.first, event.data);
                    rtspSession.sendRtsp(r);
                    break;
            }
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    void startListening() {
        new Thread(new RtcpInputListener()).start();
        state = RtpStreamState.STREAMING;
    }
}
