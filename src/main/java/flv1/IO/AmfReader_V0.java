package flv1.IO;


import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static flv1.IO.VU.debug;

public class AmfReader_V0
{
//    final SerializationContext context;
//    final ReferenceList<Object> refs;

    final AmfReader.Base b;
//    final Amf3 amf3;

    public AmfReader_V0(/*SerializationContext context, */AmfReader.Base b/*, Amf3 amf3*/)
    {
        this.b       = b;
//        this.amf3    = amf3;
//        this.context = context;
//        this.refs    = new ReferenceList<Object>();
    }

    // read_* implementations

    public Object ReadItem() throws IOException 
    {
        int marker = b.ReadByte();
        //debug("marker::" + marker);
        return ReadItem(marker);
    }

//    enum AMFTypes
//    {
//        Number = 0x00, // (Encoded as IEEE 64-bit double-precision floating point number)
//        Boolean = 0x01, // (Encoded as a single byte of value 0x00 or 0x01)
//        String = 0x02, //(ASCII encoded)
//        Object = 0x03, // (Set of key/value pairs)
//        Null = 0x05,
//        Array = 0x08,
//        End = 0x09
//    }
    
    Object ReadItem(int marker) throws IOException
    {
        AmfReader.Base core = b;
        AmfReader_V0 amf0 = this;
//        debug("ReadItem::", marker);
//        Marker.Number
        if(marker==0x00) return      core.ReadDouble();                      // 0x00 - number    ;
        if(marker==0x01) return          core.ReadBoolean();                     // 0x01 - boolean    ;
        if(marker==0x02) return          core.ReadUtf();                         // 0x02 - String    ;
        if(marker==0x03) return         amf0.ReadAsObject();                   // 0x03 - Object    ;
        if(marker==0x04)           { throw new IllegalArgumentException(); }; // 0x04 - movieclip    ;
        if(marker==0x05) return      null;                                   // 0x05 - null    ;
        if(marker==0x06) return          null;                                   // 0x06 - undefined    ;
//        if(marker==0x07) return          amf0.ReadObjectRef();                   // 0x07 - reference    ;
        if(marker==0x08) return          amf0.ReadEcmaArray();                   // 0x08 - ECMA array    ;
        if(marker==0x09)           { throw new IllegalArgumentException(); }; // 0x09 - 'Object end marker' - we handle this in deserializer block; we shouldn't encounter it here    ;
        if(marker==0x0A) return      amf0.ReadStrictArray();                 // 0x0A - strict array    ;
//        if(marker==0x0B) return          amf0.ReadDate();                        // 0x0B - date    ;
        if(marker==0x0C) return          amf0.ReadLongString();                  // 0x0C - long String    ;
//        if(marker==0x0D)           { throw new IllegalArgumentException(); }; // 0x0D - unsupported marker    ;
//        if(marker==0x0E)       { throw new IllegalArgumentException(); }; // 0x0E - recordset    ;
//        if(marker==0x0F) return      amf0.ReadXmlDocument();                 // 0x0F - xml document    ;
//        if(marker==0x10) return          amf0.ReadObject();                      // 0x10 - typed Object    ;
//        if(marker==0x11) return          amf3.ReadItem();                         // 0x11 - avmplus Object  
        throw new IllegalArgumentException();
    }

//    Object ReadObjectRef()
//    {
//        return refs.Get(b.ReadUInt16());
//    }

    // amf0 Object
//    Object ReadObject()
//    {
//        var type = b.ReadUtf();
//
//        if (context.HasConcreteType(type))
//        {
//            var instance = context.CreateInstance(type);
//            var klass    = context.GetClassInfo(instance);
//
//            refs.Add(instance);
//
//            foreach (var pair in ReadItems())
//            {
//                if (klass.TryGetMember(pair.key, out var member))
//                    member.SetValue(instance, pair.value);
//            }
//
//            return instance;
//        }
//        else if (context.AsObjectFallback)
//        {
//            // Object reference added in this.ReadAmf0AsObject()
//            var obj = ReadAsObject();
//
//            obj.TypeName = type;
//            return obj;
//        }
//        else
//        {
//            throw new ArgumentException($"can't deserialize Object: the type \"{type}\" isn't registered, and anonymous Object fallback has been disabled");
//        }
//    }

//    AsObject ReadAsObject()
//    {
//        var asObject = new AsObject();
//
//        refs.Add(asObject);
//        asObject.Replace(ReadItems());
//
//        return asObject;
//    }

    String ReadLongString() throws IOException 
    {
        int length = b.ReadInt32();
        return b.ReadUtf(length);
    }

    JSONObject ReadEcmaArray() throws IOException
    {
        int length     = b.ReadInt32();
        JSONObject dictionary = new JSONObject(true);
//        refs.Add(dictionary);

        for (MyPair<String, Object> pair : ReadItems())
            dictionary.put(pair.key, pair.value);

        return dictionary;
    }
    
    JSONObject ReadAsObject() throws IOException
    {
        JSONObject dictionary = new JSONObject(true);

        for (MyPair<String, Object> pair : ReadItems())
            dictionary.put(pair.key, pair.value);

        return dictionary;
    }

    ArrayList<Object> ReadStrictArray() throws IOException 
    {
        int length = b.ReadInt32();
        ArrayList<java.lang.Object> array = new ArrayList<Object>(length);

//        refs.Add(array);

        for (int i = 0; i < length; i++)
            array.add(ReadItem());

        return array;
    }

//    long ReadDate() throws IOException 
//    {
//        double milliseconds = b.ReadDouble();
////        var date         = UnixDateTime.Epoch.AddMilliseconds(milliseconds);
//        
//
//        // http://download.macromedia.com/pub/labs/amf/amf0_spec_121207.pdf
//        // """
//        // While the design of this type reserves room for time zone offset information,
//        // it should not be filled in, nor used, as it is unconventional to change time
//        // zones when serializing dates on a network. It is suggested that the time zone
//        // be queried independently as needed.
//        //  -- AMF0 specification, 2.13 Date Type
//        // """
//        int offset = b.ReadUInt16();
//
//        return convertWindowsTimecodeToUnixTimecode((long) milliseconds);
//    }

//    XDocument ReadXmlDocument()
//    {
//        var xml = ReadLongString();
//
//        return String.IsNullOrEmpty(xml)
//                ? new XDocument()
//                : XDocument.Parse(xml, LoadOptions.PreserveWhitespace);
//    }


    // internal helper methods

    ArrayList<MyPair<String, Object>> ReadItems() throws  IOException
    {
        ArrayList<MyPair<String, Object>> pairs = new ArrayList<MyPair<String, Object>>();
        while (true)
        {
            String key  = b.ReadUtf();
            int type = b.ReadByte();
//            debug("key::", key, type);
        
            if (type == 0x09) // Object-end marker
                return pairs;
        
            pairs.add(new MyPair<>(key, ReadItem(type)));
        }
    }


}
