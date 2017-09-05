package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;
import android.util.Log;

import com.devinbrown.streaminglib.Utils;
import com.devinbrown.streaminglib.sdp.Fmtp;
import com.devinbrown.streaminglib.sdp.MediaDescription;
import com.devinbrown.streaminglib.sdp.Rtpmap;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MediaFormatHelper {
    private static final String TAG = "MediaFormatHelper";

    private enum Media {AUDIO, VIDEO}

    public static RtpMedia[] parseSdp(URI baseUri, SessionDescription s) {
        List<RtpMedia> media = new ArrayList<>();
        for (MediaDescription m : s.mediaDescriptions) {
            media.addAll(rtpMediaFromMediaDescription(baseUri, m));
        }
        return media.toArray(new RtpMedia[media.size()]);
    }

    /**
     * Create RtpMedias for the MediaDescription provided
     *
     * @param md MediaDescription
     * @return RtpMedia extracted from MediaDescription
     */
    private static List<RtpMedia> rtpMediaFromMediaDescription(URI baseUri, MediaDescription md) {
        List<RtpMedia> media = new ArrayList<>();

        // Identify payloadTypes: 0, 14, 96, etc
        List<Integer> payloadTypes = md.payloadTypes;

        for (int p : payloadTypes) {
            MediaFormat f = null;
            // Try to look up RTP non dynamic payload format
            if (PayloadFormat.isDynamicPayloadType(p)) {
                Rtpmap rtpmap = md.getRtpmapWithFormat(p);
                Fmtp fmtp = md.getFmtpWithFormat(p);
                f = mediaFormatFromDynamicPayloadType(md.media, rtpmap, fmtp);
            } else {
                f = mediaFormatFromStaticPayloadType(md.media, p);
            }

            if (f != null) {
                URI u = MediaFormatHelper.getControlUri(baseUri, md);
                media.add(new RtpMedia(u, md, f));
            } else {
                Log.e(TAG, "Problem parsing MediaFormat from Media Description for format: " + p);
            }
        }

        return media;
    }

    private static MediaFormat mediaFormatFromStaticPayloadType(String m, int p) {
        MediaFormat format = null;
        PayloadFormat payloadFormat = PayloadFormat.staticPayloadType(p);
        if (payloadFormat != null) {
            switch (m.toLowerCase()) {
                case "audio":
                    format = MediaFormat.createAudioFormat(
                            payloadFormat.mimeType,
                            payloadFormat.clockRate,
                            payloadFormat.channelCount);

                    break;
                case "video":
                    // TODO: Video not supported. Not sure how to get video dimensions
                    //format = MediaFormat.createVideoFormat();

                    // Perhaps just set MIME type
                    //format = new MediaFormat();
                    //format.setString(MediaFormat.KEY_MIME, payloadFormat.mimeType);
                    break;
            }
        }

        return format;
    }

    private static MediaFormat mediaFormatFromDynamicPayloadType(String m, Rtpmap r, Fmtp f) {
        MediaFormat format = null;

        String mimeType = m + "/" + r.mimeSubType;

        switch (m.toLowerCase()) {
            case "audio":
                format = MediaFormat.createAudioFormat(mimeType, r.clockRate, r.channelCount);
                setFormatSpecificData(mimeType, format, f);
                break;
            case "video":
                // TODO: MediaFormat.createVideoFormat not supported. Not sure how to get video dimensions
                format = new MediaFormat();
                format.setString(MediaFormat.KEY_MIME, mimeType);
                format.setInteger(MediaFormat.KEY_SAMPLE_RATE, r.clockRate);
                format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, r.channelCount);
                break;
        }

        return format;
    }

    private static void setFormatSpecificData(String mimeType, MediaFormat format, Fmtp fmtp) {
        switch (mimeType.toLowerCase()) {
            case "audio/mpeg-generic":
            case MediaFormat.MIMETYPE_AUDIO_AAC:
                setAacFormatSpecificData(format, fmtp);
                break;
        }
    }

    // a=fmtp:96 streamtype=5;profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=1210
    private static void setAacFormatSpecificData(MediaFormat format, Fmtp fmtp) {
        String params = fmtp.formatSpecificParams;
        String configString = null;
        String[] paramsSplit = params.split(";");
        for (String param : paramsSplit) {
            param = param.trim();
            String[] paramSplit = param.split("=");
            if (paramSplit[0].toLowerCase().equals("config") && paramSplit.length == 2) {
                configString = paramSplit[1];
            }
        }
        format.setByteBuffer("csd-0", ByteBuffer.wrap(Utils.hexStringToByteArray(configString)));
    }

    private static URI getControlUri(URI baseUri, MediaDescription md) {
        URI u = null;
        StringBuilder sb = new StringBuilder();
        List<String> controlList = md.getAttributeValues("control");
        if (controlList != null && controlList.size() > 0) {
            String control = controlList.get(0);
            if (baseUri != null) sb.append(baseUri);
            sb.append(control);
            try {
                u = new URI(sb.toString());
            } catch (URISyntaxException e) {
                Log.e(TAG, "Problem parsing RTSP control URI");
            }
        }
        return u;
    }
}
