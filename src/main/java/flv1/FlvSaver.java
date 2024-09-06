package flv1;


import com.alibaba.fastjson.JSONObject;
import flv1.IO.AmfReader;
import flv1.IO.AmfWriter;
import flv1.IO.BU;
import flv1.IO.VU;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.*;
import java.util.ArrayList;

import static flv1.IO.VU.debug;

public class FlvSaver {
	public int MaxDataCount = 6300; // max keyframes index
	
	public double MinInterval = 1900; // min interval between keyframes index
	
	public long SaveMetaInterval = 0; // min interval to save metadata while saving
	
	private long lastMetaSaved = 0;

	public ArrayList<Double> times = new ArrayList<>(MaxDataCount);
	public ArrayList<Double> filepositions = new ArrayList<>(MaxDataCount);
	
	public String FileName;
	FileOutputStream _fs;
	CountingOutputStream _fs_j;
	

	private int _readPos = 0;
	private boolean hasHeader = false;
	final int BUF_LEN = 2048;        // NOTE: this MUST be large enough to get header + meta data...
	// private MemoryStream memStore = new MemoryStream(BUF_LEN);
	public MemoryStream memStore = new MemoryStream(null, BUF_LEN);


	public int NumVideo;
	public int NumAudio;
	public int MaxTimeStamp;
	int BaseTimeStamp = 0;

	private FlvHeader _header = new FlvHeader(this);
	private Tag _curTag = new Tag();

	boolean bKeyFrames = true;
	
	final static byte[] empty = new byte[0];
	final byte[] trident = new byte[4];
	
	public FlvSaver(String out) {
		FileName = out;
	}


	public int Write(byte[] buffer, int offset, int len) throws IOException 
	{
		if (!hasHeader)
		{   // header is 9 bytes
			debug("GrabHeader");
			GrabHeader(buffer, offset, len);
			debug("GrabHeader done.", memStore.pos);
		}
		else if (_curTag.TagSize > 0)
		{
			ReadTagData(buffer, offset, len);
		}
		else {
			// Parse size and timeStamp for new tag; will write tag head
			ParseTag(buffer, offset, len);
		}
		return 0;
	}



	private boolean ReadTagData(byte[] buffer, int offset, int len) throws IOException 
	{
		boolean ret = false;
		int toRead = 0;
		if (_readPos < _curTag.TagSize)
		{
			int dataLen = memStore.pos; // Math.max(memStore.count ,  dataLen)
			if (dataLen>0) {
				toRead = Math.min(dataLen, (_curTag.TagSize - _readPos));
				//debug(_readPos+" +++ ReadTagData:: from storage = "+toRead, memStore.count ,  dataLen);
				if(toRead>0) {
					_fs_write(memStore.GetBuffer(), 0, toRead); // write this tag data segment

					memStore.pos = toRead; // strike out written bytes!
					memStore.Rebase(true);
					memStore.pos = dataLen - toRead; // move head to data end!

					if(_readPos==0) {
						_curTag.tagFlag = memStore.GetBuffer()[0];
					}
					_readPos += toRead;
				}
			}

			toRead = Math.min(len, (_curTag.TagSize - _readPos));
			//debug(_readPos+" +++ ReadTagData:: from buffer = "+toRead, _curTag.TagSize, len);
			if(toRead>0) {
				_fs_write(buffer, 0, toRead); // write this tag data segment
				if(_readPos==0) {
					_curTag.tagFlag = buffer[0];
				}
				_readPos += toRead;
			}
		}
		
		if (_readPos >= _curTag.TagSize)
		{ 
			//debug("+++ ReadTagData:: done ! ", _curTag.TagSize);
			byte[] tagLenBigEn = new byte[4];
			BU.putIntBE(tagLenBigEn, 0, _curTag.TagSize + 11);
			_fs_write(tagLenBigEn, 0, 4); // write this tag size
			
			//_fs_log(("========TAG END #"+Count+"========"));

			//debug("done _curTag.tagFlag::",  (_curTag.tagFlag>>4)==1, Integer.toBinaryString(_curTag.tagFlag));
			if (bKeyFrames && (_curTag.tagFlag >> 4) == 1 && _curTag.TagType == (byte)TagType.VIDEO) // 关键帧
			{
				int lastIdx = times.size() - 1;
				while (lastIdx >= 0 && _curTag.TimeStamp < times.get(lastIdx)) {
					times.remove(lastIdx);
					filepositions.remove(lastIdx);
					//debug("times.remove::", _curTag.TimeStamp);
					lastIdx--;
				}
				//if (_curTag.TimeStamp % 1000 < 100)
				if (times.size() < MaxDataCount && (times.size() == 0 || ((_curTag.TimeStamp - times.get(times.size() - 1) * 1000) > MinInterval))) {
					times.add((double)(_curTag.TimeStamp / 1000.0));
					filepositions.add((double) (_fs_j.getCount() - _curTag.TagSize - 11 - 4));
					//debug("times::" + ((double)_curTag.TimeStamp / 1000.0) + " ==> " + _fs_j.getCount());
				}
				else if (times.size() > 0) {
					//debug("==!"+ _curTag.TimeStamp+ " > " + times[times.Count - 1]);
				}
			}
			
			if(SaveMetaInterval>0) {
				long now = VU.now();
				if(now-lastMetaSaved>SaveMetaInterval) {
					lastMetaSaved = now;
					PanallizeFile();
				}
			}

//			if (Count >= 2) {
//				DarkModeDialog.MsgBox(dataLogs);
//				throw new RuntimeException("test");
//			}

			// reset current tag
			_curTag.TagType = 0;
			_curTag.TagSize = -1;
			_readPos = 0;
			
			ret = true;
		}
		
		if(len-toRead > 0) // dont forget buffers
			memStore.write(buffer, toRead, len-toRead);
		if(memStore.pos>0) // continue consume memStore
			Write(empty,0,0);
		return ret;
	}

	int Count;
	

	private void ParseTag(byte[] buffer, int offset, int len) throws IOException {
		//debug("---- ParseTag::", memStore.pos, len);
		memStore.write(buffer, 0, len);
		if (memStore.pos >= BUF_LEN) // have we filled enough?
		{
			long dataLen = memStore.pos;
			memStore.reset(0);
			memStore.read(_curTag.PrevSize, 0, 4);
			//debug("Prev Tag Len::", BU.toInt( _curTag.PrevSize, 0)-11 );
			
			_curTag.TagType = (byte)memStore.read();
			//debug("Tag Type:", _curTag.TagType);

			// read tagSize
			memStore.read(trident, 1, 3);
			trident[0] = 0;
			_curTag.TagSize = BU.toInt(trident, 0);

			// read time
			memStore.read(trident, 1, 3);
			trident[0] = 0;
			_curTag.TimeStamp = BU.toInt(trident, 0);
			
			//debug("Tag Size::"+_curTag.TagSize, "Time::"+_curTag.TimeStamp);

			boolean isMed = false;
			if (_curTag.TagType == (byte)TagType.AUDIO) {
				this.NumAudio++;
				isMed = true;
			}
			else if (_curTag.TagType == (byte)TagType.VIDEO) {
				this.NumVideo++;
				isMed = true;
				//if (buffer[4+11] == 0x17)
				//	tagFlag |= TagFlag.Keyframe;
			}

			if (isMed) 
			{
				_curTag.TimeStamp -= this.BaseTimeStamp;
				if (_curTag.TimeStamp < this.MaxTimeStamp) {
					debug("逆行::", _curTag.TimeStamp, this.MaxTimeStamp, Count);
				}
				if (BaseTimeStamp==0 && _curTag.TimeStamp>2500)
				{
					if (Count<3) {
						_curTag.TimeStamp = 0;
						debug(Count, " wtf TimeStamp::", _curTag.TimeStamp, (_curTag.TagType), _curTag.TagSize);
					}
					else if(Count<=4) {
						BaseTimeStamp = _curTag.TimeStamp;
						_curTag.TimeStamp = 0;
						MaxTimeStamp = 0;
						debug("set BaseTimeStamp=", BaseTimeStamp);
					}
				}
				this.MaxTimeStamp = Math.max(this.MaxTimeStamp, _curTag.TimeStamp);
			}

			_curTag.TimeExtra = (byte)memStore.read();

			memStore.read(_curTag.StreamId, 0, _curTag.StreamId.length);

			if(isMed)  { Count++; }
			else if (_curTag.TagType == (byte)TagType.META) { }
			else {
				debug("Unknown Tag Type (something is wrong)" + _curTag.TagType);
				throw new IllegalStateException("Unknown Tag " + _curTag.TagType);     // TODO: throw discontinuity exception...
			}

			//Count++;
			//_curTag.TimeStamp = 0;
			byte[] tagHead = _curTag.GetBytes();
			//_fs_log(("========TAG HEADER #"+Count+"========"));
			_fs_write(tagHead, 0, tagHead.length);
			//_fs_log(("======== DATA #"+Count+"========"));

			memStore.count = (int) dataLen;
			memStore.Rebase(true); // 接下来开始读取 tag data!
			if(memStore.pos>0)
				Write(empty,0,0);
		}
	}


	String dataLogs = "";
	public void _fs_write(byte[] data) throws IOException
	{
		_fs_write(data, 0, data.length); // write this tag size
	}
//	public void _fs_write(byte[] data, int offset, int len) throws IOException {
//		String text = "======writing segment======";
//		for (int i = offset, sz=offset+len; i < sz; i++)
//		{
//			text += (String.format("%02X ", data[i]) + " ");
//		}
//		dataLogs += text + "\n";
//		debug(text);
//		_fs.write(data, offset, len); // write this tag size
//	}
//	public void _fs_log(byte[] data) throws IOException
//	{
//		_fs.write(data, 0, data.length); // write this tag size
//	}
//	public void _fs_log(String data) throws IOException
//	{
//		dataLogs += data + "\n";
//		debug(data);
//		_fs_log((data.getBytes(StandardCharsets.UTF_8))); // write this tag size
//	}
	/*...*/
	public void _fs_write(byte[] data, int offset, int len) throws IOException {
		if(_fs_j==null) {
			if (_fs == null) _fs = new FileOutputStream(this.FileName);
			_fs_j = new CountingOutputStream(new BufferedOutputStream(_fs, 4096));
		}
		_fs_j.write(data, offset, len); // write this tag size
	}
	public void _fs_log(String data) {  debug(data);  }
	/*...*/
//	public void _fs_write(byte[] data, int offset, int len) throws IOException {
//		String text = "======writing segment======";
//		for (int i = offset, sz=offset+len; i < sz; i++)
//		{
//			text += (String.format("%02X ", data[i]));
//		}
//		LogData(text);
//		_fs.write(data, offset, len); // write this tag size
//	}
//	public void _fs_log(String data) throws IOException
//	{
//		debug(data);
//		LogData(data;
////		_fs.write(data.getBytes(StandardCharsets.UTF_8)); // write this tag size
//	}
//	FileOutputStream fout;
//	private void LogData(String data) {
//		try {
//			if (fout ==null) fout = new FileOutputStream("R:\\ship\\out_debug.txt");
//			fout.write((data+"\n")).getBytes(StandardCharsets.UTF_8));
//		} catch (Exception e) {
//			debug(e);
//		}
//	}


	private long headAcc = 0;
	AmfReader metaReader;
	AmfWriter metaWriter;
	private void GrabHeader(byte[] buffer, int offset, int len) throws IOException {
		memStore.write(buffer, 0, len);
		headAcc += len;

		//debug("GrabHeader" + "/ " + memStore.pos);

		int hehe = memStore.pos;
		if (memStore.pos >= BUF_LEN)
		{// have we filled enough?
			memStore.pos = 0;
			memStore.read(_header.Header, 0, _header.Header.length);  // read the header.                
			memStore.read(_header.Tag.PrevSize, 0, 4);  // read prev tag len (should be 0).
			_header.Tag.TagType = (byte)memStore.read();  // Tag type (0x12 for Meta)
			debug("Tag Type 0=",_header.Tag.TagType);
			assert (_header.Tag.TagType == (byte) TagType.META);

			// tagSize
			memStore.read(trident, 1, 3);     // read 24 bit body length.  Note: Body length + 11 is the entire TAG size	
			trident[0] = 0;
			_header.Tag.TagSize = BU.toInt(trident, 0);
			debug("HEADER Tag Size=" +  _header.Tag.TagSize, len + "/" + headAcc);
			memStore.data.ensureCapacity(_header.Tag.TagSize);
			if (headAcc < _header.Tag.TagSize )
			{
				memStore.pos = hehe; // 接着读取啊！
				return;
			}

			// timeStamp
			memStore.read(trident, 1, 3);
			trident[0] = 0;
			_header.Tag.TimeStamp = BU.toInt(trident, 0);
			//debug("Tag TimeStamp 0=", _header.Tag.TimeStamp);

			_header.Tag.TimeExtra = (byte)memStore.read();
			memStore.read(_header.Tag.StreamId, 0, _header.Tag.StreamId.length);
			_header.Tag.Data = new byte[_header.Tag.TagSize];
			memStore.read(_header.Tag.Data, 0, _header.Tag.Data.length);

			metaReader = new AmfReader(_header.Tag.Data);
//			metaReader.trimVals = true;
			// get the onMetadata 
			String onMetadata = (String) metaReader.ReadAmf0Object();
			debug("onMetadata::" + onMetadata);
//			debug("onMetadata 1 ::" + amf.DecodeVal(_header.Tag.Data));
			_header.Meta = (JSONObject) metaReader.ReadAmf0Object();
			debug("Meta::", _header.Meta.size());

			MemoryStream header = _header.GetBytes();
			_fs_write(header.GetBuffer(), 0, header.Length());
//			DarkModeDialog.MsgBox(new String(header.GetBuffer(), 0, header.pos)+"\npos:"+header.pos+"\ncount:"+header.count);
			_fs_log(("======== "+(header.Length())+" HEAD END========"));

			
			// Now that we have what we came for.. lets start writing the file out Tag by Tag...
			hasHeader = true;
			_readPos = 0;
//			byte[] rest = memStore.GetBuffer().Skip((int)memStore.pos).Take((int)(memStore.Length - memStore.pos)).ToArray();
//			memStore.pos = 0;
			memStore.Rebase(true);
			Write(empty,0,0);
		}
	}



	int lastHeadLen;
	boolean keyFramesDirty;
	public RandomAccessFile raf;
	public void PanallizeFile() throws IOException {
		if (_fs!=null && MaxTimeStamp>0)
		{
//			VU.rt();
			_header.Meta.put("duration", (double)this.MaxTimeStamp / 1000.0); // duration is in seconds..
			_header.Meta.put("lasttimestamp", (double)this.MaxTimeStamp); // duration is in seconds..
			MemoryStream header = _header.GetBytes();
			if(raf==null) raf = new RandomAccessFile(FileName, "rw");
			raf.seek(0);
			raf.write(header.GetBuffer(), 0, header.Length());
			lastHeadLen = header.Length();
//			VU.pt(lastHeadLen+" PanallizeFile::");
		}
	}

	public void close() {
		try {
			_fs.close();
		} catch (Exception e) {
			debug(e);
		}
		if(raf!=null)
		try {
			raf.close();
		} catch (Exception e) {
			debug(e);
		}
	}
}
