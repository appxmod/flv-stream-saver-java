package flv1.IO;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.zip.InflaterOutputStream;

public class BU{ //binaryUtils

	public static int calcChecksum(byte[] bytes) {
		Adler32 a32 = new Adler32();
		a32.update(bytes);
		return (int) a32.getValue();
	}
	public static int calcChecksum(byte[] bytes,int off,int len) {
		Adler32 a32 = new Adler32();
		a32.update(bytes,off,len);
		return (int) a32.getValue();
	}
	//解压等utils
	@Deprecated
	public static byte[] zlib_decompress(byte[] encdata,int offset,int ln) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InflaterOutputStream inf = new InflaterOutputStream(out);
			inf.write(encdata,offset, ln);
			inf.close();
			return out.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "ERR".getBytes();
		}
	}

	@Deprecated
	public static byte[] toLH(int n) {
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}



	public static long toLong(byte[] buffer,int offset) {
		long  values = 0;
		for (int i = 0; i < 8; i++) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}
	public static long toLongLE(byte[] buffer,int offset) {
		long  values = 0;
		for (int i = 7; i >= 0; i--) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}
	public static int toInt(byte[] buffer,int offset) {
		int  values = 0;
		for (int i = 0; i < 4; i++) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}

	public static int toIntLE(byte[] buffer,int offset) {
		int  values = 0;
		for (int i = 3; i >= 0; i--) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}

	public static short toShortLE(byte[] buffer,int offset) {
		short  values = 0;
		for (int i = 1; i >= 0; i--) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}

	public static void putShortLE(byte[] buffer, int offset, short value) {
		buffer[offset] = (byte) (value&0xff);
		buffer[offset+1] = (byte) (value>>8&0xff);
	}

	public static void putIntLE(byte[] buffer, int offset, int value) {
		buffer[offset] = (byte) (value&0xff);
		buffer[offset+1] = (byte) (value>>8&0xff);
		buffer[offset+2] = (byte) (value>>16&0xff);
		buffer[offset+3] = (byte) (value>>24&0xff);
	}

	public static void putIntBE(byte[] buffer, int offset, long value) {
		for (int i = 0; i < 4; i++) {
			buffer[offset+i] = (byte) ((value>>(8*(3-i)))&0xff);
		}
	}


	public static void putLongLE(byte[] buffer, int offset, long value) {
		for (int i = 0; i < 8; i++) {
			buffer[offset+i] = (byte) (value&0xff);
			value>>=8;
		}
	}

	public static void putLongBE(byte[] buffer, int offset, long value) {
		for (int i = 0; i < 8; i++) {
			buffer[offset+i] = (byte) ((value>>(8*(7-i)))&0xff);
		}
	}

	static byte[] _fast_decrypt(byte[] data,byte[] key){
		long previous = 0x36;
		for(int i=0;i<data.length;i++){
			//INCONGRUENT CONVERTION FROM byte to int
			int ddd = data[i]&0xff;
			long t = (ddd >> 4 | ddd << 4) & 0xff;
			t = t ^ previous ^ (i & 0xff) ^ (key[(i % key.length)]&0xff);
			previous = ddd;
			data[i] = (byte) t;
		}
		return data;
	}



	@Deprecated
	public static void printBytes2(byte[] b) {
		for(int i=0;i<b.length;i++)
			System.out.print((int)(b[i]&0xff)+",");
		System.out.println();
	}
	public static void printBytes3(byte[] b){
		String val="";
		for(int i=0;i<b.length;i++)
			val+="0x"+byteTo16(b[i])+",";
		VU.Log(val);
	}
	public static void printBytes3(byte[][] b){
		StringBuilder val= new StringBuilder();
		for (int j = 0; j < b.length; j++) {
			val.append(new String(b[j])).append(",");
			if(false) {
				for(int i=0;i<b[j].length;i++)
					val.append("0x").append(byteTo16(b[j][i])).append(",");
				val.append("_");
			}
		}
		VU.Log(val.toString());
	}
	public static void printBytes3(byte[][][] b){
		StringBuilder val= new StringBuilder();
		for (int k = 0; k < b.length; k++) {
			for (int j = 0; j < b[k].length; j++) {
				val.append(new String(b[k][j])).append(",");
				if(false) {
					for (int i = 0; i < b[k][j].length; i++) {
						val.append("0x").append(byteTo16(b[k][j][i])).append(",");
					}
					val.append("_");
				}
			}
			val.append("/");
		}
		VU.Log(val.toString());
	}
	@Deprecated
	public static void printBytes(byte[] b){
		for(int i=0;i<b.length;i++)
			System.out.print("0x"+byteTo16(b[i])+",");
		System.out.println();
	}

	public static void LogBytes(byte[] b){
		LogBytes(b, 0, b.length);
	}

	public static void LogBytes(byte[] b,int off,int ln){
		String val="";
		ln+=off;
		for(int i=off;i<ln;i++)
			val+="0x"+byteTo16(b[i])+",";
		VU.Log(val);
	}
	@Deprecated
	public static void printBytes(byte[] b,int off,int ln){
		for(int i=off;i<off+ln;i++)
			System.out.print("0x"+byteTo16(b[i])+",");
		System.out.println();
	}
	public static void printFile(byte[] b,int off,int ln,String path){
		printFile(b, off, ln, new File(path));
	}
	@Deprecated
	public static void printFile(byte[] b,int off,int ln,File path){
		try {
			File p = path.getParentFile();
			if(!p.exists()) p.mkdirs();
			FileOutputStream fo = new FileOutputStream(path);
			fo.write(b);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			VU.Log(e);
		}
	}

	public static void SaveToFile(String string, File f) throws IOException {
		FileOutputStream fout = new FileOutputStream(f);
		fout.write(string.getBytes(StandardCharsets.UTF_8));
		fout.close();
	}

	public static void SaveToFile(InputStream input, File f) throws IOException {
		FileOutputStream fout = new FileOutputStream(f);
		byte[] data = new byte[4096];
		int len;
		while((len=input.read(data))!=-1)
			fout.write(data, 0, len);
		fout.close();
	}

	public static String StreamToString(InputStream input) throws IOException {
		int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(input, StandardCharsets.UTF_8);
		for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
			out.append(buffer, 0, numRead);
		}
		input.close();
		return out.toString();
	}

	@Deprecated
	public static void printFile(byte[] b, String path){
		printFile(b,0,b.length,new File(path));
	}

	@Deprecated
	public static void printFile(byte[] b, File path){
		printFile(b,0,b.length,path);
	}

	@Deprecated
	public static void printFileStream(InputStream b, File path){
		try {
			printStreamToFile(b,0,b.available(),path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public static void printStreamToFile(InputStream b, int start, int end,  File path){
		try {
			if(start>0)
				b.skip(start);
			File p = path.getParentFile();
			if(!p.exists()) p.mkdirs();
			FileOutputStream fo = new FileOutputStream(path);
			byte[] data = new byte[4096];
			int len;
			while ((len=b.read(data))>0){
				fo.write(data, 0, len);
			}
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String byteTo16(byte bt){
		String[] strHex={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
		String resStr="";
		int low =(bt & 15);
		int high = bt>>4 & 15;
		resStr = strHex[high]+strHex[low];
		return resStr;
	}

	public static char toChar(byte[] buffer,int offset) {
		char  values = 0;
		for (int i = 0; i < 2; i++) {
			values <<= 8; values|= (buffer[offset+i] & 0xff);
		}
		return values;
	}

	public static byte[] fileToByteArr(File f) {
		try {
			FileInputStream fin = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fin.read(data);
			return data;
		} catch (Exception e) {
			VU.Log(e);
		}
		return null;
	}


	private static boolean startsWithUtf8Bom(byte[] data) {
		// UTF-8 BOM的字节序列是 {0xEF, 0xBB, 0xBF}
		byte[] utf8Bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		// 检查字节数组的前几个字节是否与UTF-8 BOM相匹配
		if (data.length >= utf8Bom.length) {
			for (int i = 0; i < utf8Bom.length; i++) {
				if (data[i] != utf8Bom[i]) {
					return false; // 字节数组与UTF-8 BOM不匹配
				}
			}
			return true; // 字节数组以UTF-8 BOM开头
		} else {
			return false; // 字节数组长度不足以包含UTF-8 BOM
		}
	}


	public static String fileToString(File f) {
		try {
			FileInputStream fin = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fin.read(data);
			fin.close();
			if(startsWithUtf8Bom(data))
				return new String(data, 3, data.length-3, StandardCharsets.UTF_8);
			return new String(data, StandardCharsets.UTF_8);
		} catch (Exception e) {
			VU.Log(e);
		}
		return null;
	}

	public static String fileToString(File f, Charset charset) {
		try {
			FileInputStream fin = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fin.read(data);
			fin.close();
			return new String(data, charset);
		} catch (Exception e) {
			VU.Log(e);
		}
		return null;
	}

	public static FileInputStream fileToStream(File f) {
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String FileToString(File f) throws IOException {
		FileInputStream fin = new FileInputStream(f);
		byte[] data = new byte[(int) f.length()];
		fin.read(data);
		fin.close();
		return new String(data, "utf8");
	}


	public static String fileToString(String path, byte[] buffer, /*ByteOutputStream bo,*/ Charset charset) {
//		try {
//			FileInputStream fin = new FileInputStream(path);
//			bo.reset();
//			int len;
//			while((len = fin.read(buffer))>0)
//				bo.write(buffer, 0, len);
//			return new String(bo.getBytes(),0, bo.size(), charset);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return null;
	}

	public static String transStream(InputStream in) {
		byte[] data = new byte[4096];
//		ByteOutputStream bout = new ByteOutputStream(8192);
//		try{
//			int len;
//			while ((len=in.read(data))>0) {
//				bout.write(data, 0 ,len);
//			}
//		} catch (Exception ignored) {  }
//		return new String(bout.getBytes(), 0, bout.size());
		return null;
	}

	public static void appendToFile(File f, String...args) {
		try {
			FileOutputStream fout = new FileOutputStream(f, true);
			for(String aI:args) {
				fout.write(aI.getBytes());
			}
			fout.flush();
			fout.close();
		} catch (Exception ignored) { }
	}

	@Deprecated
	public long toLong1(byte[] b,int offset)
	{
		long l = 0;
		l = b[offset+0];
		l |= ((long) b[offset+1] << 8);
		l |= ((long) b[offset+2] << 16);
		l |= ((long) b[offset+3] << 24);
		l |= ((long) b[offset+4] << 32);
		l |= ((long) b[offset+5] << 40);
		l |= ((long) b[offset+6] << 48);
		l |= ((long) b[offset+7] << 56);
		return l;
	}


	public static String unwrapMdxName(String in) {
		if(in.toLowerCase().endsWith(".mdx"))
			return in.substring(0,in.length()-4);
		return in;
	}
	public static String unwrapMddName(String in) {
		if(in.toLowerCase().endsWith(".mdd"))
			return in.substring(0,in.length()-4);
		return in;
	}

	public static int bit_length(long num) {
		int res = 1;
		num >>= 1;
		while(num != 0) {
			res += 1;
			num >>= 1;
		}
		return res;
	}

	public static int readInt(InputStream bin) throws IOException {
		int ch1 = bin.read();
		int ch2 = bin.read();
		int ch3 = bin.read();
		int ch4 = bin.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public static int readShort(InputStream bin) throws IOException {
		int ch1 = bin.read();
		int ch2 = bin.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short)((ch1 << 8) + (ch2 << 0));
	}

	public static int readShortLE(InputStream bin) throws IOException {
		return bin.read()|(bin.read()<<8);
	}

	public static InputStream SafeSkipReam(InputStream fin, long toSkip) throws IOException {
		//CMN.Log("SafeSkipReam::", toSkip);
		if(toSkip==0) return fin;
		int tryCount=0;long skipped;
		while(toSkip>0){
			skipped=fin.skip(toSkip);
			if(false && skipped==0&&tryCount>=3){
				break;
			} else {
				toSkip-=skipped;
				tryCount++;
			}
		}
		return fin;
	}

	public static String calcMD5(String path){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(path);
			byte[] buffer = new byte[4096];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				md5.update(buffer, 0, len);
			}
			fis.close();

			byte[] byteArray = md5.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : byteArray) {
				sb.append(String.format("%02x", b&0xFF));
			}
			return sb.toString();
		} catch (Exception e){
			VU.Log(e);
		}
		return "";
	}

}
