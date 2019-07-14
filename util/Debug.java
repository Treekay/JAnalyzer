package util;

import java.io.PrintWriter;

/**
 * @author Zhou Xiaocong
 * @since 2013-2-28
 * @version 1.0
 *
 */
public class Debug {
	private static long startTime = 0;
	private static long lastTime = 0;
	private static PrintWriter writer = new PrintWriter(System.out);
	private static boolean display = true;
	private static boolean screen = false;
	private static boolean writerIsScreen = true;
	
	public static void setStart() {
		startTime = System.currentTimeMillis();
	}
	
	public static void setStart(String msg) {
		if (display) {
			writer.println(msg);
			writer.flush(); 
		}
		startTime = System.currentTimeMillis();
		lastTime = 0;
		if (screen && !writerIsScreen) System.out.println(msg);
	}

	public static long time() {
		long millis = System.currentTimeMillis() - startTime;
		return millis;
	}
	
	public static long time(String msg) {
		long currentTime = System.currentTimeMillis() - startTime;
		long millis = currentTime - lastTime;
		if (display) {
			writer.println(msg + ", " + millis + "(" + currentTime + ") milliseconds!");
			writer.flush();
		}
		if (screen && !writerIsScreen) System.out.println(msg + ", " + millis + "(" + currentTime + ") milliseconds!");
		lastTime = currentTime;
		return millis;
	}

	public static void setWriter(PrintWriter writer) {
		Debug.writer = writer;
		writerIsScreen = false;
	}
	
	public static PrintWriter getWriter() {
		return writer;
	}
	
	public static void println(String msg) {
		if (display) {
			writer.println(msg);
			writer.flush();
		}
		if (screen && !writerIsScreen) {
			System.out.println(msg);
		}
	}
	
	public static void println(boolean condition, String msg) {
		if (display) {
			if (condition) {
				writer.println(msg);
				writer.flush();
			}
		}
		if (screen && !writerIsScreen) {
			if (condition) {
				System.out.println(msg);
			}
		}
	}
	
	public static void print(String msg) {
		if (display) {
			writer.print(msg);
			writer.flush();
		}
		if (screen && !writerIsScreen) {
			System.out.print(msg);
		}
	}
	
	public static void print(boolean condition, String msg) {
		if (display) {
			if (condition) {
				writer.println(msg);
				writer.flush();
			}
		}
		if (screen && !writerIsScreen) {
			if (condition) {
				System.out.println(msg);
			}
		}
	}

	public static void assertion(boolean condition, String msg) {
		if (condition) throw new AssertionError(msg);
	}
	
	public static void flush() {
		writer.flush();
	}

	public static void enable() {
		display = true;
	}
	public static void disable() {
		display = false;
	}
	
	public static void setScreenOn() {
		screen = true;
	}
	public static void setScreenOff() {
		screen = false;
	}
}
