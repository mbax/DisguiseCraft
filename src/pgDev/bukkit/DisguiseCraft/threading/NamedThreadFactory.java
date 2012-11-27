package pgDev.bukkit.DisguiseCraft.threading;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class NamedThreadFactory implements ThreadFactory {
	int count = 0;
	String name = "";
	
	public NamedThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, name + "-" + count++);
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
            	DisguiseCraft.logger.log(Level.SEVERE, "Uncaught exception in thread: " + t.getName(), e);
            }
        });
		return t;
	}

}
