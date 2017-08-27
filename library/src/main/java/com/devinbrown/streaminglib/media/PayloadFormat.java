package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;
import android.util.SparseArray;

/**
 * Reference: RTP Profiles - https://en.wikipedia.org/wiki/RTP_audio_video_profile
 * Reference: MIME Types - http://www.iana.org/assignments/media-types/media-types.xhtml
 */
public class PayloadFormat {
    private static final String TAG = "PayloadFormat";

    private static final int PAYLOAD_TYPE_MIN = 0;
    private static final int PAYLOAD_TYPE_MAX = 127;
    private static final int DYNAMIC_PAYLOAD_TYPE_MIN = 96;
    private static final int DYNAMIC_PAYLOAD_TYPE_MAX = PAYLOAD_TYPE_MAX;

    // Supported static payload types
    // NOTE: There are many other static payload types defined. See reference "RTP Profiles"
    private static final PayloadFormat PCMU = new PayloadFormat(0, MediaFormat.MIMETYPE_AUDIO_G711_MLAW, 1, 8000);
    private static final PayloadFormat PCMA = new PayloadFormat(8, MediaFormat.MIMETYPE_AUDIO_G711_ALAW, 1, 8000);
    private static final PayloadFormat L16_S = new PayloadFormat(10, "audio/L16", 2, 8000);
    private static final PayloadFormat L16_M = new PayloadFormat(11, "audio/L16", 1, 8000);
    private static final PayloadFormat MPA_M = new PayloadFormat(14, MediaFormat.MIMETYPE_AUDIO_MPEG, 1, 90000);
    private static final PayloadFormat MPA_S = new PayloadFormat(14, MediaFormat.MIMETYPE_AUDIO_MPEG, 2, 90000);
    private static final PayloadFormat H263 = new PayloadFormat(34, MediaFormat.MIMETYPE_VIDEO_H263, null, 90000);

    public static SparseArray<PayloadFormat> staticPayloadTypes = new SparseArray<>();

    public Integer payloadType;
    public String mimeType;
    public Integer channelCount;
    public Integer clockRate;

    private PayloadFormat(Integer p, String m, Integer ch, int cl) {
        payloadType = p;
        mimeType = m;
        channelCount = ch;
        clockRate = cl;
    }

    public static PayloadFormat staticPayloadType(int payloadType) {
        return staticPayloadTypes.get(payloadType);
    }

    public static boolean isDynamicPayloadType(int payloadType) {
        return payloadType >= DYNAMIC_PAYLOAD_TYPE_MIN && payloadType <= DYNAMIC_PAYLOAD_TYPE_MAX;
    }
}
