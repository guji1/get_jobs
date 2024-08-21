import boss.Boss;
import boss.BossCount;
import job51.Job51;
import job51.Job51Count;
import lagou.Lagou;
import lombok.extern.slf4j.Slf4j;
import zhilian.ZhiLian;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StartAllCount {

    public static void main(String[] args) {
        // 创建一个调度任务的服务，线程池大小为4，确保任务按顺序执行
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);


        // 调度执行
        scheduleTask(scheduler, StartAllCount::runAllPlatforms);
    }

    private static void runAllPlatforms() {
        safeRun(() -> BossCount.main(null));
        safeRun(() -> Job51Count.main(null));
//        safeRun(() -> ZhiLian.main(null));
//        safeRun(() -> Lagou.main(null));
    }

    private static void scheduleTask(ScheduledExecutorService scheduler, Runnable task) {
        long delay = getInitialDelay();

        // 设置定时任务，每天8点执行一次
        scheduler.scheduleAtFixedRate(task, delay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private static long getInitialDelay() {
        Calendar nextRun = Calendar.getInstance();
        nextRun.add(Calendar.DAY_OF_YEAR, 1); // 加一天
        nextRun.set(Calendar.HOUR_OF_DAY, 8); // 设置时间为8点
        nextRun.set(Calendar.MINUTE, 0);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        long currentTime = System.currentTimeMillis();
        return (nextRun.getTimeInMillis() - currentTime) / 1000; // 返回秒数
    }

    private static void safeRun(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("safeRun异常：{}", e.getMessage(), e);
        }
    }
}
