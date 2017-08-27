package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;
import android.util.Log;

import com.devinbrown.streaminglib.Utils;
import com.devinbrown.streaminglib.sdp.Fmtp;
import com.devinbrown.streaminglib.sdp.Rtpmap;
import com.devinbrown.streaminglib.sdp.SessionDescription;
import com.devinbrown.streaminglib.sdp.MediaDescription;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MediaFormatHelper {
    private static final String TAG = "MediaFormatHelper";

    private enum Media {AUDIO, VIDEO}

    public static MediaFormat[] parseSdp(SessionDescription s) {
        List<MediaFormat> formats = new ArrayList<>();
        for (MediaDescription m : s.mediaDescriptions) {
            // Maybe make this a HashMap<String, MediaFormat> with String being the control URL, or
            // at least store them that way internally and only expose the MediaFormat or some other
            // object that encapsulates the MF.
            formats.addAll(parseMediaDescription(m));
        }
        return formats.toArray(new MediaFormat[formats.size()]);
    }

    private static List<MediaFormat> parseMediaDescription(MediaDescription md) {
        List<MediaFormat> formats = new ArrayList<>();

        // Identify payloadTypes: 0, 14, 96, etc
        List<Integer> payloadTypes = md.payloadTypes;

        for (int p : payloadTypes) {
            MediaFormat f = null;

            // Try to look up RTP non dynamic payload format
            if (PayloadFormat.isDynamicPayloadType(p)) {

                Log.d(TAG, "parseMediaDescription: MD : " + md.toString());
                Log.d(TAG, "parseMediaDescription: p : " + p);

                Rtpmap rtpmap = md.getRtpmapWithFormat(p);
                Fmtp fmtp = md.getFmtpWithFormat(p);
                f = mediaFormatFromDynamicPayloadType(md.media, rtpmap, fmtp);
            } else {
                f = mediaFormatFromStaticPayloadType(md.media, p);
            }

            if (f == null) {
                Log.e(TAG, "Problem parsing MediaFormat from Media Description for format: " + p);
            } else {
                formats.add(f);
            }
        }

        return formats;
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
                // TODO: Video not supported. Not sure how to get video dimensions
                //format = MediaFormat.createVideoFormat();

                // Perhaps just set MIME type
                //format = new MediaFormat();
                //format.setString(MediaFormat.KEY_MIME, mimeType);
                break;
        }

        return format;
    }

    private static void setFormatSpecificData(String mimeType, MediaFormat format, Fmtp fmtp) {
        switch (mimeType.toLowerCase()) {
            case "audio/mpeg-generic":
                //case MediaFormat.MIMETYPE_AUDIO_AAC:
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
}
