package flv1.IO;

import flv1.ReusableByteOutputStream;
import flv1.IO.BU;

import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

import static flv1.IO.VU.debug;

public class AmfWriter
{

    public static class Base
    {
        // `cache` allows us to avoid allocations when reading items that are less than n bytes
//        final byte[]     tempArray;
        final byte[]     temporary;
        final ReusableByteOutputStream writer;


        // constructor

            public Base(ReusableByteOutputStream writer)
        {
            this.writer    = writer;
//            this.tempArray = new byte[8];
            this.temporary = new byte[8];
        }


        // helper
        void CopyTemporary(int length) {
                WriteBytes(temporary, 0, length);
        }


        // writers
        public void WriteByte(byte value) {
            writer.write(value);
        }
        public void WriteBytes(byte[] buffer){
            try {
                writer.write(buffer);
            } catch (Exception e) {
                debug(e);
            }
        }

        public void WriteBytes(byte[] buffer, int index, int count) {
            try {
                writer.write(buffer, index, count);
            } catch (Exception e) {
                debug(e);
            }
        }

//        public void WriteBytes(Space<byte> span)
//                => writer.Write(span);

        public void WriteInt16(short value)
        {
            temporary[0] = (byte)(value >> 8);
            temporary[1] = (byte)value;

            CopyTemporary(2);
        }

        public void WriteUInt16(int value)
        {
            temporary[0] = (byte)(value >> 8);
            temporary[1] = (byte)value;

            CopyTemporary(2);
        }

        public void WriteInt32(int value)
        {
            temporary[0] = (byte)(value >> 24);
            temporary[1] = (byte)(value >> 16);
            temporary[2] = (byte)(value >> 8);
            temporary[3] = (byte)value;

            CopyTemporary(4);
        }

        public void WriteUInt32(long value)
        {
            temporary[0] = (byte)(value >> 24);
            temporary[1] = (byte)(value >> 16);
            temporary[2] = (byte)(value >> 8);
            temporary[3] = (byte)value;

            CopyTemporary(4);
        }

        // writes a little endian 32-bit integer
        public void WriteLittleEndianInt(long value)
        {
            temporary[0] = (byte)value;
            temporary[1] = (byte)(value >> 8);
            temporary[2] = (byte)(value >> 16);
            temporary[3] = (byte)(value >> 24);

            CopyTemporary(4);
        }

        public void WriteUInt24(int value)
        {
            temporary[0] = (byte)(value >> 16);
            temporary[1] = (byte)(value >> 8);
            temporary[2] = (byte)(value >> 0);

            CopyTemporary(3);
        }

        public void WriteBoolean(boolean value)
        {
            WriteByte(value ? (byte)1 : (byte)0);
        }

        public void WriteDouble(double value)
        {
            BU.putLongBE(temporary, 0, Double.doubleToLongBits(value));
            writer.write(temporary);
            //new DataOutputStream(writer).writeDouble(value);
            //debug("WriteDouble::", value);
            
//            var temp = *((ulong*)&value);
//
//            temporary[0] = (byte)(temp >> 56);
//            temporary[1] = (byte)(temp >> 48);
//            temporary[2] = (byte)(temp >> 40);
//            temporary[3] = (byte)(temp >> 32);
//            temporary[4] = (byte)(temp >> 24);
//            temporary[5] = (byte)(temp >> 16);
//            temporary[6] = (byte)(temp >> 8);
//            temporary[7] = (byte)temp;
//
//            CopyTemporary(8);
        }

        public void WriteSingle(float value)
        {
            BU.putIntBE(temporary, 0, Float.floatToIntBits(value));
            writer.write(temporary, 0, 4);
            //new DataOutputStream(writer).writeFloat(value);
//            var temp = *((uint*)&value);
//
//            temporary[0] = (byte)(temp >> 24);
//            temporary[1] = (byte)(temp >> 16);
//            temporary[2] = (byte)(temp >> 8);
//            temporary[3] = (byte)temp;
//
//            CopyTemporary(4);
        }

        // String with 16-bit length prefix
        public void WriteUtfPrefixed(String value)
        {
//            Check.NotNull(value);
            byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
            WriteUtfPrefixed(utf8);
        }

        public void WriteUtfPrefixed(byte[] utf8)
        {
//            Check.NotNull(utf8);
            WriteUInt16(utf8.length);
            WriteBytes(utf8);
        }
    }
    
    public final ReusableByteOutputStream writer;
//    final SerializationContext context;

    final Base core;
    public final AmfWriter_V0 amf0;
//    final Amf3 amf3;

//    public int Length   => writer.Length;
//    public int Position => writer.Position;

    public AmfWriter(ReusableByteOutputStream writer)
    {
       // this.context = Check.NotNull(context);
        this.writer  = writer;

        this.core = new Base(writer);
//        this.amf3 = new Amf3(context, this, core);
        this.amf0 = new AmfWriter_V0(core);
    }


//    public Space<byte> Span => writer.Span;
//    public void   Return()  => writer.Return();
//    public byte[] ToArray() => writer.ToArray();
//    public byte[] ToArrayAndReturn() => writer.ToArrayAndReturn();



    // efficiency: avoid re-allocating this object by re-binding it to a new buffer, effectively resetting this object.

//    public void Reset()
//    {
//        amf0.Reset();
//        amf3.Reset();
//        writer.Reset();
//    }


//    public void WriteByte(byte value)                           => core.WriteByte(value);
//    public void WriteBytes(Space<byte> span)                    => core.WriteBytes(span);
//    public void WriteBytes(byte[] buffer)                       => core.WriteBytes(buffer);
//    public void WriteBytes(byte[] buffer, int index, int count) => core.WriteBytes(buffer, index, count);
//    public void WriteInt16(short value)                         => core.WriteInt16(value);
//    public void WriteUInt16(ushort value)                       => core.WriteUInt16(value);
//    public void WriteInt32(int value)                           => core.WriteInt32(value);
//    public void WriteUInt32(uint value)                         => core.WriteUInt32(value);
//    public void WriteLittleEndianInt(uint value)                => core.WriteLittleEndianInt(value);
//    public void WriteUInt24(uint value)                         => core.WriteUInt24(value);
//    public void WriteBoolean(boolean value)                        => core.WriteBoolean(value);
//    public void WriteDouble(double value)                       => core.WriteDouble(value);
//    public void WriteSingle(float value)                        => core.WriteSingle(value);
//    public void WriteUtfPrefixed(String value)                  => core.WriteUtfPrefixed(value);
//    public void WriteUtfPrefixed(byte[] utf8)                   => core.WriteUtfPrefixed(utf8);
    public void WriteAmf0Object(Object value) {
        amf0.WriteItem(value);
    }
//    public void WriteAmf3Object(object value)                   => amf3.WriteItem(value);
//
//    public void WriteBoxedAmf0Object(ObjectEncoding encoding, object value)
//        => amf0.WriteBoxedItem(encoding, value);
//
//    public void WriteAmfObject(ObjectEncoding encoding, object value)
//    {
//        if (encoding == ObjectEncoding.Amf0)
//            amf0.WriteItem(value);
//        else if (encoding == ObjectEncoding.Amf3)
//            amf3.WriteItem(value);
//        else
//            throw new ArgumentOutOfRangeException("unsupported encoding");
//    }


}