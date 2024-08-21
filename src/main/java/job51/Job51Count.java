package job51;

import boss.BossCount;
import boss.BossEnum;
import ch.qos.logback.core.util.StringCollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import entity.JobInfo;
import entity.LogInfo;
import lombok.SneakyThrows;
import mapper.JobInfoMapper;
import mapper.LogInfoMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JobUtils;
import utils.SeleniumUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import static utils.Bot.sendMessage;
import static utils.Constant.*;

/**
 * 前程无忧自动投递简历
 *
 * @author loks666
 */
public class Job51Count {
    private static final Logger log = LoggerFactory.getLogger(Job51.class);

    static Integer page = 1;
    static Integer maxPage = 50;
    static String cookiePath = "./src/main/java/job51/cookie.json";
    static String homeUrl = "https://www.51job.com";
    static String loginUrl = "https://login.51job.com/login.php?lang=c&url=https://www.51job.com/&qrlogin=2";
    static String baseUrl = "https://we.51job.com/pc/search?";
    static List<String> returnList = new ArrayList<>();
    static Job51Config config = Job51Config.init();

    public static void main(String[] args) {

        SeleniumUtil.initDriver();
        Date startDate = new Date();
        Login();
        config.getJobArea().forEach(Job51Count::postJobByCity);

        Date endDate = new Date();
        long durationSeconds = endDate.getTime() - startDate.getTime() ;

//        sendMessage(message);
        // 转换为时分秒
        int hours = (int) (TimeUnit.MILLISECONDS.toHours(durationSeconds) % 24);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationSeconds % TimeUnit.HOURS.toMillis(1));
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(durationSeconds % TimeUnit.MINUTES.toMillis(1));
        String durationMinutes = hours + "时" + minutes + "分" + seconds + "秒";
        String message = String.format("【51job】共保存了%d个简历,用时%s", returnList.size(), durationMinutes);
        log.info(message);
        LogInfo logInfo = new LogInfo();
        logInfo.setCreateDate(startDate);
        logInfo.setEndDate(endDate);
        logInfo.setSource("51job");
        logInfo.setCity(config.getJobArea().stream().map(value -> BossEnum.CityCode.forValue(value).getName()).toList().toString());
        logInfo.setJobTag(config.getKeywords().toString());
        logInfo.setSpendDate("用时："+ hours + "时" + minutes + "分" + seconds + "秒");
        logInfo.setTotalChat(String.valueOf(returnList.size()));
        SqlSessionFactory logInfoSessionFactory = JobUtils.getLogInfoSessionFactory();
        //初始化
        try (SqlSession session = logInfoSessionFactory.openSession(true)) {
            //创建mapper对象
            LogInfoMapper mapper = session.getMapper(LogInfoMapper.class);
            //插入
            mapper.insert(logInfo);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error("投完简历休息期间出现异常:", e);
        } finally {
            CHROME_DRIVER.quit();
        }
    }
    private static void postJobByCity(String cityCode) {
        String searchUrl = getSearchUrl(cityCode);
        config.getKeywords().forEach(keyword -> {
            SeleniumUtil.sleep(3);
            resume(searchUrl + "&keyword=" + keyword,cityCode,keyword);
        });


    }

    private static String getSearchUrl(String cityCode) {
        return baseUrl +
                JobUtils.appendParam("jobArea", cityCode) +
                JobUtils.appendListParam("salary", config.getSalary());
    }

    private static void Login() {
        CHROME_DRIVER.get(homeUrl);
        if (SeleniumUtil.isCookieValid(cookiePath)) {
            SeleniumUtil.loadCookie(cookiePath);
            CHROME_DRIVER.navigate().refresh();
            SeleniumUtil.sleep(1);
        }
        if (isLoginRequired()) {
            log.error("cookie失效，尝试扫码登录...");
            scanLogin();
        }
    }

    private static boolean isLoginRequired() {
        try {
            String text = CHROME_DRIVER.findElement(By.xpath("//p[@class=\"tit\"]")).getText();
            return text != null && text.contains("登录");
        } catch (Exception e) {
            log.info("cookie有效，已登录...");
            return false;
        }
    }

    @SneakyThrows
    private static void resume(String url,String city,String keyword) {
        CHROME_DRIVER.get(url);

        int i = 0;
        try {
            CHROME_DRIVER.findElements(By.className("ss")).get(i).click();
        } catch (Exception e) {
            findAnomaly();
        }
        try {
            WebElement element = CHROME_DRIVER.findElement(By.xpath("//ul[@class='el-pager']//li[contains(@class, 'number') and contains(@class, 'active')]"));
            if (element != null && element.isDisplayed()){
                SeleniumUtil.sleep(1);
                maxPage = Integer.valueOf(element.getText());
                log.info("最大页数为：{}",maxPage);
            }
        }catch (Exception e){
            log.info("查询最大页数错误！");
        }

        for (int j = page; j <= maxPage; j++) {
            while (true) {
                try {
                    WebElement mytxt = WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.id("jump_page")));
                    mytxt.click();
                    mytxt.clear();
                    mytxt.sendKeys(String.valueOf(j));
                    WAIT.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#app > div > div.post > div > div > div.j_result > div > div:nth-child(2) > div > div.bottom-page > div > div > span.jumpPage"))).click();
                    ACTIONS.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
                    log.info("第 {} 页", j);
                    break;
                } catch (Exception e) {
                    log.error("mytxt.clear()可能异常...");
//                    SeleniumUtil.sleep(1);
//                    String verify = CHROME_DRIVER.findElement(By.cssSelector("#WAF_NC_WRAPPER > p.waf-nc-title")).getText();
//                    if(verify.contains("访问验证")){
//                        log.error("出现访问验证了！程序退出...");
//                        CHROME_DRIVER.close();
//                        CHROME_DRIVER.quit(); // 关闭之前的ChromeCHROME_DRIVER实例
//                        SeleniumUtil.initDriver();
//                        CHROME_DRIVER.get(url);
//                    }


                }
            }
            List<WebElement> checkboxes = null;
            try{
                checkboxes = CHROME_DRIVER.findElements(By.cssSelector("div.ick"));
                if (checkboxes.isEmpty()) {
                    return;
                }
            }catch (Exception e){
                //找不到元素之后 ，休眠五秒，然后重新查找
                SeleniumUtil.sleep(3);
                checkboxes = CHROME_DRIVER.findElements(By.cssSelector("div.ick"));
            }


            postCurrentJob(city,keyword,checkboxes);
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }


    @SneakyThrows
    private static void postCurrentJob(String cityCode,String keyword,List<WebElement> checkboxes) {

        List<WebElement> jobPositions = CHROME_DRIVER.findElements(By.cssSelector("[class*='jname text-cut']"));
        List<WebElement> companies = CHROME_DRIVER.findElements(By.cssSelector("[class*='cname text-cut']"));
        List<WebElement> companyStatuss = CHROME_DRIVER.findElements(By.cssSelector("[class*='dc text-cut']"));
        List<WebElement> companyNums = CHROME_DRIVER.findElements(By.cssSelector("div.bl span:nth-of-type(3)"));
        List<WebElement> companyIndustrys = CHROME_DRIVER.findElements(By.cssSelector("div.bl span:nth-of-type(2)"));
        List<WebElement> addresses = CHROME_DRIVER.findElements(By.cssSelector("[class*='shrink-0']"));
        List<WebElement> jobHrefs = CHROME_DRIVER.findElements(By.xpath("//a[@class='cname text-cut']"));
        List<WebElement> jobSalarys = CHROME_DRIVER.findElements(By.cssSelector("[class*='sal shrink-0']"));
        List<WebElement> jobInfos = CHROME_DRIVER.findElements(By.cssSelector("div[sensorsname='JobShortExposure']"));

        for (int i = 0; i < checkboxes.size(); i++) {
            String jobPosition = null;
            if (jobPositions.size() > i && jobPositions.get(i) != null)
                jobPosition = jobPositions.get(i).getText();
            String companyName = null;
            if (companies.size() > i && companies.get(i) != null)
                companyName = companies.get(i).getText();
            String companyIndustry = null;
            if (companyIndustrys.size() > i && companyIndustrys.get(i) != null)
                companyIndustry = companyIndustrys.get(i).getText();
            String companyNum = null;
            if (companyNums.size() > i && companyNums.get(i) != null)
                companyNum = companyNums.get(i).getText();
            String companyStatus = null;
            if (companyStatuss.size() > i && companyStatuss.get(i) != null)
                companyStatus = companyStatuss.get(i).getText();
            String jobHref = null;
            if (jobHrefs.size() > i && jobHrefs.get(i) != null)
                jobHref = jobHrefs.get(i).getAttribute("href");
            String jobSalary = null;
            if (jobSalarys.size() > i && jobSalarys.get(i) != null)
                jobSalary = jobSalarys.get(i).getText();


            Map<String, String> stringMap = JobUtils.jsonToMap(jobInfos.get(i).getAttribute("sensorsdata"));
            String jobEducation = stringMap.get("jobDegree");
            String jobExperience = stringMap.get("jobYear");
            String jobAddress = stringMap.get("jobArea");
            log.info("打印一下两个城市：" + addresses.get(i).getText() + " | " + jobAddress);
            String jobHr = "";
            String jobSource = "51Job";
            String IndustryDirectory = config.getIndustryDirectory();
            String city = Job51Enum.jobArea.forName(cityCode);

            JobInfo jobInfo = new JobInfo();
            jobInfo.setWorkExperience(jobExperience);
            jobInfo.setEducation(jobEducation);
            jobInfo.setAddress(jobAddress);
            jobInfo.setCompanyName(companyName);
            jobInfo.setJobPosition(jobPosition);
            jobInfo.setTreatment(jobSalary);
            jobInfo.setUpdateDate(new Date());
            jobInfo.setSource(jobSource);
            jobInfo.setHr(jobHr);
            jobInfo.setJobTag(keyword);
            jobInfo.setHref(jobHref);
            jobInfo.setCompanyIndustry(companyIndustry);
            jobInfo.setCompanyNum(companyNum);
            jobInfo.setCompanyStatus(companyStatus);
            jobInfo.setIndustryDirectory(IndustryDirectory);
            jobInfo.setCity(city);

            SqlSessionFactory jobInfoSessionFactory = JobUtils.getJobInfoSessionFactory();
            //初始化
            try (SqlSession session = jobInfoSessionFactory.openSession(true)) {
                //创建mapper对象
                JobInfoMapper mapper = session.getMapper(JobInfoMapper.class);
                QueryWrapper<JobInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("company_name", jobInfo.getCompanyName());
                queryWrapper.eq("treatment", jobInfo.getTreatment());
                queryWrapper.eq("job_position", jobInfo.getJobPosition());
                queryWrapper.eq("source", jobInfo.getSource());

                JobInfo info = mapper.selectOne(queryWrapper);
                if (info != null) {
                    mapper.updateById(jobInfo);
                } else {
                    jobInfo.setCreateDate(new Date());
                    mapper.insert(jobInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            returnList.add(companyName + " | " + jobPosition);


//            SeleniumUtil.sleep(1);
//        ACTIONS.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
//        boolean success = false;
//        while (!success) {
//            try {
//                // 查询按钮是否存在
//                WebElement parent = CHROME_DRIVER.findElement(By.cssSelector("div.tabs_in"));
//                List<WebElement> button = parent.findElements(By.cssSelector("button.p_but"));
//                // 如果按钮存在，则点击
//                if (button != null && !button.isEmpty()) {
//                    SeleniumUtil.sleep(1);
//                    button.get(1).click();
//                    success = true;
//                }
//            } catch (ElementClickInterceptedException e) {
//                log.error("失败，1s后重试..");
//                SeleniumUtil.sleep(1);
//            }
//        }
//
//        try {
//            SeleniumUtil.sleep(3);
//            String text = CHROME_DRIVER.findElement(By.xpath("//div[@class='successContent']")).getText();
//            if (text.contains("快来扫码下载~")) {
//                //关闭弹窗
//                CHROME_DRIVER.findElement(By.cssSelector("[class*='van-icon van-icon-cross van-popup__close-icon van-popup__close-icon--top-right']")).click();
//            }
//        } catch (Exception ignored) {
//            log.info("未找到投递成功弹窗！可能为单独投递申请弹窗！");
//        }
//        String particularly = null;
//        try {
//            particularly = CHROME_DRIVER.findElement(By.xpath("//div[@class='el-dialog__body']/span")).getText();
//        } catch (Exception ignored) {
//        }
//        if (particularly != null && particularly.contains("需要到企业招聘平台单独申请")) {
//            //关闭弹窗
//            CHROME_DRIVER.findElement(By.cssSelector("#app > div > div.post > div > div > div.j_result > div > div:nth-child(2) > div > div:nth-child(2) > div:nth-child(2) > div > div.el-dialog__header > button > i")).click();
//            log.info("关闭单独投递申请弹窗成功！");
//        }
        }
    }

    private static void findAnomaly() {
        try {
            String verify = CHROME_DRIVER.findElement(By.cssSelector("#WAF_NC_WRAPPER > p.waf-nc-title")).getText();
            String limit = CHROME_DRIVER.findElement(By.xpath("//div[contains(@class, 'van-toast')]")).getText();
            if (verify.contains("访问验证") || limit.contains("投递太多")) {
                //关闭弹窗
                log.error("出现访问验证了！程序退出...");
                CHROME_DRIVER.close();
                CHROME_DRIVER.quit(); // 关闭之前的ChromeCHROME_DRIVER实例
                System.exit(-2);
            }

        } catch (Exception ignored) {
            log.info("未出现访问验证，继续运行...");
        }
    }

    private static void scanLogin() {
        log.info("等待扫码登陆..");
        CHROME_DRIVER.get(loginUrl);
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("hasresume")));
        SeleniumUtil.saveCookie(cookiePath);
    }

}
