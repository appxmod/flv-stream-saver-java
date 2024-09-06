package flv1;

import com.alibaba.fastjson.JSONObject;
import flv1.IO.AmfWriter;
import flv1.IO.BU;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static flv1.IO.VU.debug;

public class Tag
{
    public byte TagType = 0;
    public int TagSize = 0;
    public int TimeStamp;
    public byte TimeExtra = 0;
    public byte[] StreamId = new byte[3];
    public byte[] Data = null;
    public byte[] PrevSize            = new byte[4];

    public static byte[] timeBigEn = new byte[4];
    public int tagFlag;

    byte[] tag = new byte[11];
    
    public byte[] GetBytes()
    {
        int st = 0;

        //byte[] psize = new byte[4];
        //Buffer.BlockCopy(PrevSize, 0, psize, 0, PrevSize.Length); // copy prev size (4 bytes)
        //Array.Reverse(psize);
        //Buffer.BlockCopy(psize, 0, tag, 0, psize.Length); // copy prev size (4 bytes)

        // write tagSize
        BU.putIntBE(tag, 0, this.TagSize);

        // write tagType
        tag[st] = TagType;                                       // type (1 byte) 5

        //debug("writing tag header:: " + this.TimeStamp);

        // write timestamp
        BU.putIntBE(timeBigEn, 0, this.TimeStamp);
        System.arraycopy(timeBigEn, 1, tag, st + 4, 3); // 省略高数位
        
        //tag[st + 7] = TimeExtra; // tm extra (1 bytes) = 12
        //Buffer.BlockCopy(StreamId, 0, tag, st + 8, 3); // tm (3 bytes) = 15    

        tag[st + 7] = 0; // tm extra (1 bytes) = 12
        tag[st+8] = 0;
        tag[st + 9] = 0;
        tag[st + 10] = 0;

        return tag;
    }
};
