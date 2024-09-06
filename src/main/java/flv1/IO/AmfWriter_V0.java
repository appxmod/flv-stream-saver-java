package flv1.IO;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static flv1.IO.VU.debug;

public class AmfWriter_V0
{
//    final SerializationContext context;
//    final ReferenceList<Object> refs;

    final AmfWriter.Base b;
//    final Amf3 amf3;


    public AmfWriter_V0(AmfWriter.Base b)
    {
        this.b       = b;
//        this.amf3    = amf3;
//        this.context = context;
//        this.refs    = new ReferenceList<Object>();
    }


    // helper methods

//    public void Reset() => refs.Clear();

//    void ReferenceAdd(Object value) => refs.Add(value, out var _);
//    boolean ReferenceAdd(Object value, out ushort index) => refs.Add(value, out index);
//    boolean ReferenceGet(Object value, out ushort index) => refs.TryGetValue(value, out index);


    // writers


    public void WriteItem(Object value)
    {
        if (value == null)
        {
            WriteMarker(Marker.Null);
        }
//        else if (ReferenceGet(value, out var index))
//        {
//            WriteMarker(Marker.Reference);
//            b.WriteUInt16(index);
//        }
        else
        {
            WriteItemInternal(value);
        }
    }

    // writes an Object, with the specified encoding. if amf3 encoding is specified, then it is wrapped in an
    // amf0 envelope that says to upgrade the encoding to amf3
//    public void WriteBoxedItem(ObjectEncoding encoding, Object value)
//    {
//        if (value == null)
//        {
//            WriteMarker(Marker.Null);
//        }
//        else if (ReferenceGet(value, out var index))
//        {
//            WriteMarker(Marker.Reference);
//            b.WriteUInt16(index);
//        }
//        else
//        {
//            switch (encoding)
//            {
//                case ObjectEncoding.Amf0:
//                    WriteItemInternal(value);
//                    break;
//
//                case ObjectEncoding.Amf3:
//                    WriteMarker(Marker.Amf3Object);
//                    amf3.WriteItem(value);
//                    break;
//
//                default:
//                    throw new ArgumentOutOfRangeException(nameof(encoding));
//            }
//        }
//    }

    void WriteItemInternal(Object v)
    {
//        if      (NumberTypes(value))               WriteNumber(Convert.ToDouble(value));
//        else if (Writers.TryGetValue(type, out var write)) write(this, value);
//        else                                               DispatchGenericWrite(value);
        AmfWriter_V0 x = this;
        if(v instanceof Double)                                         WriteNumber((Double)v)                                                    ;
        else if(v instanceof Integer)                                         WriteNumber((double)((Integer)v))                                                    ;
        else if(v instanceof Float)                                         WriteNumber((double)((Float)v))                                                    ;
        else if(v instanceof Long)                                         WriteNumber((double)((Long)v))                                                    ;
        else if(v instanceof Boolean)                                   x.WriteBoolean((boolean)v)                                                    ;
//        else if(v instanceof char)                                      x.WriteVariantString(v.ToString())                                         ;
        else if(v instanceof String)                                    x.WriteVariantString((String)v)                                            ;
//        else if(v instanceof DateTime)                                  x.WriteDateTime((DateTime)v)                                               ;
        else if(v instanceof AsObject)                                  x.WriteAsObject((AsObject)v)                                               ;
//        else if(v instanceof Guid)                                      x.WriteVariantString(v.ToString())                                         ;
//        else if(v instanceof XDocument)                                 x.WriteXDocument((XDocument)v)                                             ;
//        else if(v instanceof XElement)                                  x.WriteXElement((XElement)v)                                               ;
        else if(v instanceof JSONObject)                                x.WriteAssociativeArray((JSONObject)v)                    ;
        else if(v instanceof List)                                  x.WriteArray((List)v, ((ArrayList)v).size());
        else                                               DispatchGenericWrite(v);                
    }

    // writes a String, either as a short or long strong depending on length.
    void WriteVariantString(String value)
    {
        // Console.WriteLine("WriteVariantString"+value);
//        CheckDebug.NotNull(value);
        // for(var i=0;i<8;i++)  b.WriteByte(0xf0);
        // for(var i=0;i<8;i++)  b.WriteBytes(Encoding.UTF8.GetBytes("x"));

        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        int length = utf8.length;

        if (length < Short.MAX_VALUE*2)
        {
            // unsigned 16-bit length
            WriteMarker(Marker.String);
            b.WriteUInt16(utf8.length);
            b.WriteBytes(utf8);
        }
        else
        {
            // unsigned 32-bit length
            WriteMarker(Marker.LongString);
            b.WriteUInt32(utf8.length);
            b.WriteBytes(utf8);
        }
    }

    void WriteAsObject(AsObject val)
    {
        JSONObject dictionary = val.data;
//        CheckDebug.NotNull(value);
//        ReferenceAdd(value);

//        if (String.IsNullOrEmpty(value.TypeName))
//        {
            WriteMarker(Marker.Object);
//        }
//        else
//        {
//            WriteMarker(Marker.TypedObject);
//            b.WriteUtfPrefixed(value.TypeName);
//        }

        for (String key : dictionary.keySet())
        {
            Object value = dictionary.get(key);
            if (!StringUtils_isEmpty(key) && value != null && !(value instanceof String && StringUtils_isEmpty(((String)value)))) {
                //debug("key::"+key, value, b.writer.size());
                b.WriteUtfPrefixed(key);
                WriteItem(value);
            }
        }

        // Object end is marked with a zero-length field name, and an end of Object marker.
        b.WriteUInt16(0);
        WriteMarker(Marker.ObjectEnd);
    }

    private boolean StringUtils_isEmpty(String key) {
        return key==null || key.length()==0;
    }

//    void WriteTypedObject(Object value)
//    {
//        CheckDebug.NotNull(value);
//        ReferenceAdd(value);
//
//        var klass = context.GetClassInfo(value);
//
//        WriteMarker(Marker.TypedObject);
//        b.WriteUtfPrefixed(klass.Name);
//
//        foreach (var member in klass.Members)
//        {
//            b.WriteUtfPrefixed(member.Name);
//            WriteItem(member.GetValue(value));
//        }
//
//        // Object end is marked with a zero-length field name, and an end of Object marker.
//        b.WriteUInt16(0);
//        WriteMarker(Marker.ObjectEnd);
//    }

//    void WriteDateTime(DateTime value)
//    {
//        // http://download.macromedia.com/pub/labs/amf/amf0_spec_121207.pdf
//        // """
//        // While the design of this type reserves room for time zone offset information,
//        // it should not be filled in, nor used, as it is unconventional to change time
//        // zones when serializing dates on a network. It is suggested that the time zone
//        // be queried independently as needed.
//        //  -- AMF0 specification, 2.13 Date Type
//        // """
//
//        var duration = value.ToUniversalTime() - UnixDateTime.Epoch;
//        WriteMarker(Marker.Date);
//        b.WriteDouble(duration.TotalMilliseconds);
//        b.WriteUInt16(0); // time zone offset
//    }
//
//    void WriteXDocument(XDocument value)
//    {
//        CheckDebug.NotNull(value);
//        ReferenceAdd(value);
//
//        UnmarkedWriteLongString(
//            value.ToString(SaveOptions.DisableFormatting));
//    }
//
//    void WriteXElement(XElement value)
//    {
//        CheckDebug.NotNull(value);
//        ReferenceAdd(value);
//
//        UnmarkedWriteLongString(
//            value.ToString(SaveOptions.DisableFormatting));
//    }

    void WriteArray(List enumerable, int length)
    {
//        CheckDebug.NotNull(enumerable);
//        ReferenceAdd(enumerable);

        WriteMarker(Marker.StrictArray);
        b.WriteInt32(length);

        for (Object element : enumerable)
            WriteItem(element);
    }

    public void WriteAssociativeArray(JSONObject dictionary)
    {
//        CheckDebug.NotNull(dictionary);
//        ReferenceAdd(dictionary);

        WriteMarker(Marker.EcmaArray);
        b.WriteInt32(dictionary.size());

//        List<String> keys = new ArrayList<>(Arrays.asList(dictionary.keySet().toArray(new String[0])));
//        Collections.reverse(keys);
//        keys.remove("ksvi");
        for (String key : dictionary.keySet())
        {
            Object value = dictionary.get(key);
            if (!StringUtils_isEmpty(key) && value != null && !(value instanceof String && StringUtils_isEmpty(((String)value)))) {
                //debug("key::"+key, value, b.writer.size());
                b.WriteUtfPrefixed(key);
                WriteItem(value);
            }
        }

        // Object end is marked with a zero-length field name, and an end of Object marker.
        b.WriteUInt16(0);
        WriteMarker(Marker.ObjectEnd);
    }

    void WriteBoolean(boolean value)
    {
        WriteMarker(Marker.Boolean);
        b.WriteBoolean(value);
    }

    void WriteNumber(double value)
    {
        WriteMarker(Marker.Number);
        b.WriteDouble(value);
    }


    void DispatchGenericWrite(Object value)
    {
//        switch (value)
//        {
//            case Enum e:
//                WriteNumber(Convert.ToDouble(e));
//                break;
//
//            case IDictionary<String, Object> dictionary:
//                WriteAssociativeArray(dictionary);
//                break;
//
//            case IList list:
//                WriteArray(list, list.Count);
//                break;
//
//            case ICollection collection:
//                WriteArray(collection, collection.Count);
//                break;
//
//            case IEnumerable enumerable:
//                var type = value.GetType();
//
//                if (type.ImplementsGenericInterface(typeof(ICollection<>)) || type.ImplementsGenericInterface(typeof(IList<>)))
//                {
//                    dynamic d = value;
//                    int count = d.Count;
//
//                    WriteArray(enumerable, count);
//                }
//                else
//                {
//                    var values = enumerable.Cast<Object>().ToArray();
//                    WriteArray(values, values.Length);
//                }
//
//                break;
//
//            default:
//                WriteTypedObject(value);
//                break;
//        }
    }

//    void UnmarkedWriteLongString(String value)
//    {
//        CheckDebug.NotNull(value);
//
//        var utf8 = Encoding.UTF8.GetBytes(value);
//
//        WriteMarker(Marker.LongString);
//        b.WriteUInt32((uint)utf8.Length);
//        b.WriteBytes(utf8);
//    }

    void WriteMarker(int marker)
    {
        b.WriteByte((byte)marker);
    }


    static final boolean NumberTypes(Object obj)
    {
        return obj instanceof Integer || obj instanceof Float 
                || obj instanceof Double || obj instanceof Long;
    };



}
