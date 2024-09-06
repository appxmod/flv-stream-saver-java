package flv1.IO;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;

public class VU  {
	
	public static void debug(Object... o) {
		if(o.length==1 && o[0]==null) {
//			try {
//				throw new RuntimeException("watch stacktrace!");
//			} catch (RuntimeException e) {
//				debug(e);
//			}
			return;
		}
		Log(o);
	}

	public static void show(String o) {
		Log(o);
	}

	public static void Log(Object... o) {
//		if (true) {
//			try {
//				throw new RuntimeException();
//			} catch (RuntimeException e) {
//				e.printStackTrace();
//			}
//		}
		String sep = ", ";
		StringBuilder msg = new StringBuilder(1024);
		for(int i=0;i<o.length;i++) {
			Object o1 = o[i];
			if (o1 != null) {
				if (o1 instanceof Exception) {
					Exception e = ((Exception)o[i]);
					msg.append(e);
					ByteArrayOutputStream s = new ByteArrayOutputStream();
					PrintStream p = new PrintStream(s);
					e.printStackTrace(p);
					msg.append(s);
					continue;
				}
				if (o1!=null && o1.getClass().isArray()) {
					String classname = o1.getClass().getName();
					if (classname.length()==2) {
						switch (classname.charAt(1)) {
							case 'B': {
								o1 = msg.append(Arrays.toString((byte[]) o1));
							} break;
							case 'D': {
								o1 = msg.append(Arrays.toString((double[]) o1));
							} break;
							case 'F': {
								o1 = msg.append(Arrays.toString((float[]) o1));
							} break;
							case 'L': {
								o1 = msg.append(Arrays.toString((long[]) o1));
							} break;
							case 'I': {
								o1 = msg.append(Arrays.toString((int[]) o1));
							} break;
							case 'S': {
								o1 = msg.append(Arrays.toString((short[]) o1));
							} break;
						}
					} else {
						o1 = Arrays.toString((Object[]) o1);
					}
				}
			}
			msg.append(o1);
			msg.append(sep);
		}
		String message = msg.toString();
		System.out.println(message);
		sep = " ";
		//return message;
	}

	public static long stst;
	public static long ststrt;
	public static long stst_add;
	public static long rt(Object... o) {
		if(o.length>0)Log(o);
		return ststrt = System.currentTimeMillis();
	}
	public static long pt(Object...args) {
		long ret=System.currentTimeMillis()-ststrt;
		Log((args.length==0?"":args[0]) + Long.toString(ret) +" "+listToStr(1, args));
		return ret;
	}
	public static void tp(long stst, Object...args) {
		long time = (System.currentTimeMillis() - stst);
		Log(time+" "+listToStr(0, args));
		stst_add+=time;
	}

	public static String listToStr(int st, Object...args) {
		String ret="";
		for (int i = st; i < args.length; i++) {
			ret+=args[i];
		}
		return ret;
	}

	public static int id(Object object) {
		return System.identityHashCode(object);
	}

	public static String idStr(Object object) {
		return Integer.toHexString(System.identityHashCode(object));
	}

	public static long now() {
		return System.currentTimeMillis();
	}

	public static Object elapsed(long st) {
		return now()-st;
	}

	public static String getAssetName(String name) {
		return new File(name).getName();
	}

	public static String getSuffix(String key) {
		int idx = key.lastIndexOf(".");
		if (idx>=0) {
			return key.substring(idx).toLowerCase();
		}
		return "";
	}



	public static void rt() {
		stst = System.currentTimeMillis();
	}
	public static void pt(String...args) {
		Log(args,(System.currentTimeMillis()-stst));
	}

	public static void pt_mins(String...args) {
		Log(args,((System.currentTimeMillis()-stst)/1000.f/60)+"m");
	}

}
