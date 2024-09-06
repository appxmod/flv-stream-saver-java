package flv1.IO;

public class Marker
{
	public final static int Number      = 0x00; // 0x00 | 0
	public final static int Boolean     = 0x01; // 0x01 | 1
	public final static int String      = 0x02; // 0x02 | 2
	public final static int Object      = 0x03; // 0x03 | 3
	public final static int Movieclip   = 0x04; // 0x04 | 4
	public final static int Null        = 0x05; // 0x05 | 5
	public final static int Undefined   = 0x06; // 0x06 | 6
	public final static int Reference   = 0x07; // 0x07 | 7
	public final static int EcmaArray   = 0x08; // 0x08 | 8
	public final static int ObjectEnd   = 0x09; // 0x09 | 9
	public final static int StrictArray = 0x0A; // 0x0A | 10
	public final static int Date        = 0x0B; // 0x0B | 11
	public final static int LongString  = 0x0C; // 0x0C | 12
	public final static int Unsupported = 0x0D; // 0x0D | 13
	public final static int Recordset   = 0x0E; // 0x0E | 14
	public final static int Xml         = 0x0F; // 0x0F | 15
	public final static int TypedObject = 0x10; // 0x10 | 16
	public final static int Amf3Object  = 0x11; // 0x11 | 17
};