package ch.fork.AdHocRailway.railway.srcp;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fork on 3/23/14.
 */
public class SRCPThreadUtils {


    public static ThreadPoolExecutor createExecutorService() {
        return new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static RateLimiter createRateLimiter() {
        return RateLimiter.create(4);
    }
}
