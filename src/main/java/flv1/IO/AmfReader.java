package flv1.IO;



import flv1.ReusableByteInputStream;
import flv1.ReusableByteOutputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static flv1.IO.VU.debug;

public class AmfReader
{
    public boolean trimVals;

    class Base
    {
        // `cache` allows us to avoid allocations when reading items that are less than n bytes
        final byte[]     tempArray;
        final int[]     temporary;
        final ReusableByteInputStream reader;

        // constructor
        public Base(ReusableByteInputStream reader)
        {
            this.reader    = reader;
            this.tempArray = new byte[8];
            this.temporary = new int[8];
        }

        // helpers
        void Require(int count)
        {
            int read = reader.read(tempArray, 0, count);
            for (int i = 0; i < count; i++) {
                temporary[i] = tempArray[i]&0xff;
            }
            // Console.WriteLine(("read::" + read));
            if (read != count)
                throw new IllegalStateException("EndOfStreamException");
        }

        // readers
        public int ReadByte()
        {
            return reader.read();
        }

        ReusableByteOutputStream buffer = new ReusableByteOutputStream(1024);
        public ReusableByteOutputStream ReadBytes(int count) throws IOException 
        {
            buffer.ensureCapacity(count);
            buffer.reset();
            int read = reader.read(buffer.getBytes(), 0, count);
            buffer.precede(count);
//            debug("read::", read);
            return buffer;
        }

        public void ReadBytes(byte[] buffer, int index, int count)
        {
            if (reader.read(buffer, index, count) != count)
                throw new IllegalStateException("tried to read past end of data stream");
        }

        public int ReadUInt16()
        {
            Require(2);
            return (int)(((tempArray[0] & 0xFF) << 8) | (tempArray[1] & 0xFF));
        }

        public short ReadInt16()
        {
            Require(2);
            return (short)(((temporary[0] & 0xFF) << 8) | (temporary[1] & 0xFF));
        }

        public Boolean ReadBoolean()
        {
            return reader.read()!=0;
        }

        public int ReadInt32()
        {
            Require(4);
            return (int)(temporary[0] << 24) | (temporary[1] << 16) | (temporary[2] << 8) | temporary[3];
        }

        public long ReadUInt32()
        {
            Require(4);
            return (long)(((temporary[0]+0L) << 24) | (temporary[1] << 16) | (temporary[2] << 8) | temporary[3]);
        }

        public int ReadLittleEndianInt()
        {
            Require(4);
            return (temporary[3] << 24) | (temporary[2] << 16) | (temporary[1] << 8) | temporary[0];
        }

        public int ReadUInt24()
        {
            Require(3);
            return (int)(temporary[0] << 16 | temporary[1] << 8 | temporary[2]);
        }

        // 64-bit IEEE-754 double precision floating point
        public double ReadDouble() throws IOException 
        {
            return new DataInputStream(reader).readDouble();
//            Require(8);
//            var lo = (uint)(temporary[7] | temporary[6] << 8 | temporary[5] << 16 | temporary[4] << 24);
//            var hi = (uint)(temporary[3] | temporary[2] << 8 | temporary[1] << 16 | temporary[0] << 24);
//            var value = (ulong)hi << 32 | lo;
//            return *(double*)&value;
        }

        // single-precision floating point number
        public float ReadSingle() throws IOException
        {
            return new DataInputStream(reader).readFloat();
//            Require(4);
//            var value = (uint)(temporary[0] << 24 | temporary[2] << 8 | temporary[1] << 16 | temporary[3]);
//            return *(float*)&value;
        }

        // utf8 string with length prefix
        public String ReadUtf() throws IOException {
            int length = ReadUInt16();
//             debug("length::" + length);
//             debug("length::" + temporary[0]);
//             debug("length::" + temporary[1]);
            return ReadUtf(length);
        }

        // utf8 string
        public String ReadUtf(int length) throws IOException {
            if (length == 0)
                return "";

            byte[] data = ReadBytes(length).getBytes();
            if(trimVals) {
                while(length>0 && data[length-1]==0) {
                    length--;
                }
            }
            
            return new String(data, 0,  length, StandardCharsets.UTF_8);
        }
    }
    
        final ReusableByteInputStream reader;
//        final SerializationContext context;

        final Base core;
        final AmfReader_V0 amf0;
//        final Amf3 amf3;

//        public int Length    => reader.Length;
//        public int Position  => reader.Position;
//        public int Remaining => reader.Length - reader.Position;


        public AmfReader(byte[] data/*, SerializationContext context*/)
        {
//            this.context = context;
            this.reader  = new ReusableByteInputStream(data);

            core = new Base(reader);
//            amf3 = new Amf3(context, this, core);
            amf0 = new AmfReader_V0(core);
        }


//        public byte   ReadByte()                                     => core.ReadByte();
//        public byte[] ReadBytes(int count)                           => core.ReadBytes(count);
//        public void   ReadBytes(byte[] buffer, int index, int count) => core.ReadBytes(buffer, index, count);
//        public ushort ReadUInt16()                                   => core.ReadUInt16();
//        public short  ReadInt16()                                    => core.ReadInt16();
//        public Boolean   ReadBoolean()                                  => core.ReadBoolean();
//        public int    ReadInt32()                                    => core.ReadInt32();
//        public uint   ReadUInt32()                                   => core.ReadUInt32();
//        public int    ReadLittleEndianInt()                          => core.ReadLittleEndianInt();
//        public uint   ReadUInt24()                                   => core.ReadUInt24();
//        public double ReadDouble()                                   => core.ReadDouble();
//        public float  ReadSingle()                                   => core.ReadSingle();
//        public string ReadUtf()                                      => core.ReadUtf();
//        public string ReadUtf(int length)                            => core.ReadUtf(length);
        public Object ReadAmf0Object() throws IOException{
            return amf0.ReadItem();
        }
//        public Object ReadAmf3Object()                               => amf3.ReadItem();

//        public Object ReadAmfObject(ObjectEncoding encoding)
//        {
//            if (encoding == ObjectEncoding.Amf0)
//                return amf0.ReadItem();
//
//            if (encoding == ObjectEncoding.Amf3)
//                return amf3.ReadItem();
//
//            throw new ArgumentOutOfRangeException("unsupported encoding");
//        }

    }