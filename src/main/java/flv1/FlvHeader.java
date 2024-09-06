package flv1;

import com.alibaba.fastjson.JSONObject;
import flv1.IO.AmfWriter;
import flv1.IO.AsObject;
import flv1.IO.Marker;
import flv1.IO.BU;

import static flv1.IO.VU.debug;


public class FlvHeader
{
    public final int HEADER_SIZE    = 9;
    public byte[] Header            = new byte[HEADER_SIZE];
    public static byte[] tmpBuffer = new byte[8192];
    public Tag Tag = new Tag();
    
    public JSONObject Meta = new JSONObject(true);
    
    final FlvSaver saver;

    JSONObject keyframesAss = new JSONObject(true);

    MemoryStream memory = new MemoryStream();

    AmfWriter writer = new AmfWriter(memory.data);

    public FlvHeader(FlvSaver saver) {
        this.saver = saver;
    }

    public MemoryStream GetBytes()
    {
        //debug("--> write [FlvHeader]");
        memory.reset(0);
        long writeSt = memory.pos;
        memory.write(Header, 0, Header.length); //  FLV 01 (A0V) 90 00 00 00 
        
        // this.Meta["fuck"] = 0.0;    // place holder.. 
        if (!this.Meta.containsKey("duration"))
        {   // assure that we add space for the duration key
            this.Meta.put("duration", 0.0);
        }
        if (!this.Meta.containsKey("lasttimestamp"))
        {   // assure that we add space for the lasttimestamp
            this.Meta.put("lasttimestamp", 0.0);
        }
//        this.Meta.put("duration", 0.0);

        final boolean bKeyFrames = saver.bKeyFrames;
        if (bKeyFrames) {
            //var times = new double[2] { 0.0, 0.2 };
            //var filepositions = new double[2] { 6400, 6500 };
            keyframesAss.put("times", saver.times);
            keyframesAss.put("filepositions", saver.filepositions);
            //debug("keyframesAss=" + saver.times.size() );
            this.Meta.put("keyframes", new AsObject(keyframesAss));  // place holder..   
        }


        // PreviousTagSize 0
        int writeEd = memory.pos;
//        memory.data.ensureCapacity(writeEd+4);
        BU.putIntBE(memory.data.getBytes(), writeEd, writeEd - writeSt);
        memory.pos += 4;

        // Tag Header 0
        int _htPos = memory.pos;
        memory.write(Tag.tag); // place holder

        // Tag Data 0
        int startPos = memory.pos;
        int minSz = (bKeyFrames ?saver.MaxDataCount:0) * 2 * (8 + 1) + 4096;
        memory.data.ensureCapacity(minSz);
        writer.WriteAmf0Object("onMetaData");
        writer.WriteAmf0Object(this.Meta);

        int endPos = memory.data.count;
        
        if(bKeyFrames) { // fill zeros
            int zeros = minSz - (endPos - startPos) - 1; 
            while(zeros>0) {
                int tmp = Math.min(tmpBuffer.length, zeros);
                memory.data.write(tmpBuffer, 0, tmp);
                zeros -= tmp;
            }
            memory.data.write(Marker.ObjectEnd);
        }
        endPos = memory.data.count;
        
        Tag.TagSize = endPos-startPos;


        BU.putIntBE(memory.data.getBytes(), endPos, Tag.TagSize + 11);

        //byte[] tagLenBigEn = BitConverter.GetBytes((Tag.TagSize+ metahead.Length) );
        //Array.Reverse(tagLenBigEn);
        //memory.Write(tagLenBigEn, 0, 4); // _curTag.GetBytes()

        //byte[] tagLenBigEn = BitConverter.GetBytes(writeEd - writeSt  );
        //Array.Reverse(tagLenBigEn);
        //memory.Write(tagLenBigEn, 0, 4); // _curTag.GetBytes()


        endPos = memory.data.count + 4;

        // seek back and write 11 bytes of tag header
        memory.pos = _htPos;
        //Tag.TimeStamp = 0;
        //debug(Tag.TimeStamp + " metahead TimeStamp, TagType::" + (Tag.TagType));
        byte[] metahead = Tag.GetBytes();
        memory.write(metahead);

        //File.WriteAllBytes("R:\\cache\\meta1.bin", meta);
        // skip raw tag writting  // memory.Write(Tag.Data, 0, Tag.Data.Length);

        
        memory.pos = memory.count = endPos;

        return memory;
    }
}
