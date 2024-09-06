package flv1;

import flv1.IO.VU;

import java.io.FileInputStream;
import java.io.IOException;

public class TestSaveFlv {


	public static void main(String[] args) throws IOException {
		VU.rt();

		
		String in; String out;
		in = "R:\\ship\\in.flv";

		out = "R:\\ship\\out.flv";

		FlvSaver stream2File = new FlvSaver(out);
		
		stream2File.SaveMetaInterval = 1000 * 1;

		FileInputStream fs = new FileInputStream(in);


		int BUF_LEN = 4096;
		byte[] buffer = new byte[BUF_LEN];
		
		int read = 0;
		while((read=fs.read(buffer)) > 0) {
			stream2File.Write(buffer, 0, read);
//			try { Thread.sleep(50); } catch (Exception e) {  }
		}
		stream2File.PanallizeFile();
		
		VU.debug("done rest::", stream2File.memStore.pos);
//		if (stream2File.memStore.pos > 0) {
//			stream2File._fs.write(stream2File.memStore.GetBuffer(), 0, stream2File.memStore.pos);
//		}
		stream2File.close();
		fs.close();


		VU.pt("DONE::");
		
	}
}
