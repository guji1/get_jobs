package zhilian;

import boss.BossEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import entity.JobInfo;
import entity.LogInfo;
import job51.Job51Enum;
import mapper.JobInfoMapper;
import mapper.LogInfoMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Job;
import utils.JobUtils;
import utils.SeleniumUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static utils.Bot.sendMessage;
import static utils.Constant.*;

public class ZhiLianCount {
    private static final Logger log = LoggerFactory.getLogger(ZhiLianCount.class);

    static String loginUrl = "https://passport.zhaopin.com/login";

    static String homeUrl = "https://sou.zhaopin.com/?";

    static boolean isLimit = false;

    static int maxPage = 500;

    static ZhilianConfig config = ZhilianConfig.init();

    static List<Job> resultList = new ArrayList<>();


    public static void main(String[] args) {
        SeleniumUtil.initDriver();
        Date startDate = new Date();
        login();
        config.getCityCodes().forEach(cityCode ->{
            config.getKeywords().forEach(keyword -> {
                CHROME_DRIVER.get(getSearchUrl(keyword, 1,cityCode));
                submitJobs(keyword,cityCode);
                isLimit = false;
            });
        });

        Date endDate = new Date();
        long durationSeconds = endDate.getTime() - startDate.getTime() ;

//        sendMessage(message);
        // 转换为时分秒
        int hours = (int) (TimeUnit.MILLISECONDS.toHours(durationSeconds) % 24);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(durationSeconds % TimeUnit.HOURS.toMillis(1));
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(durationSeconds % TimeUnit.MINUTES.toMillis(1));
        String durationMinutes = hours + "时" + minutes + "分" + seconds + "秒";
        String message = String.format("【51job】共保存了%s个简历,用时%s", resultList.size(), durationMinutes);
        log.info(message);
        LogInfo logInfo = new LogInfo();
        logInfo.setCreateDate(startDate);
        logInfo.setEndDate(endDate);
        logInfo.setSource("zhiLian");
        logInfo.setCity(config.getCityCodes().stream().map(value -> BossEnum.CityCode.forValue(value).getName()).toList().toString());
        logInfo.setJobTag(config.getKeywords().toString());
        logInfo.setSpendDate("用时："+ hours + "时" + minutes + "分" + seconds + "秒");
        logInfo.setTotalChat(String.valueOf(resultList.size()));
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
//        sendMessage(message);
        CHROME_DRIVER.close();
        CHROME_DRIVER.quit();
    }

    private static String getSearchUrl(String keyword, int page,String city) {
        return homeUrl +
                JobUtils.appendParam("jl", city) +
                JobUtils.appendParam("kw", keyword) +
                "&p=" + page;
    }

    private static void submitJobs(String keyword,String cityCode) {

        SeleniumUtil.sleep(2);
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'joblist-box__item')]")));
        setMaxPages();
        for (int i = 1; i <= maxPage; i++) {

            if (i != 1) {
                CHROME_DRIVER.get(getSearchUrl(keyword, i ,cityCode));
            }
            log.info("开始投递【{}】关键词，第【{}】页...", keyword, i);
            // 等待岗位出现
            try {
                WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'joblist-box__item') and contains(@class, 'clearfix')]")));
            } catch (Exception ignore) {
//                CHROME_DRIVER.get(getSearchUrl(keyword, i ,cityCode));
                SeleniumUtil.sleep(5);
            }
            SeleniumUtil.sleep(2);
            List<WebElement> jobCards = null;
            try {
                jobCards = CHROME_DRIVER.findElements(By.xpath("//div[contains(@class, 'joblist-box__item') and contains(@class, 'clearfix')]"));
                if (jobCards.isEmpty()) {
                    log.info("当前数据为空");
                    return;
                }
            }catch (Exception e){
                SeleniumUtil.sleep(4);
                jobCards = CHROME_DRIVER.findElements(By.xpath("//div[contains(@class, 'joblist-box__item') and contains(@class, 'clearfix')]"));
                if (jobCards.isEmpty()) {
                    log.info("当前数据为空");
                    return;
                }
            }
            for (WebElement jobCard : jobCards){
                try {
                    WebElement jobPositions = jobCard.findElement(By.cssSelector(".jobinfo__top .jobinfo__name"));
                    String jobPosition = jobPositions.getText();

                    WebElement companyNames = jobCard.findElement(By.cssSelector(".companyinfo__top .companyinfo__name"));
                    String companyName = companyNames.getText();

                    String companyStatus = null;
                    String companyNum = null;
                    String companyIndustry = null;

                    List<WebElement> numCounts = jobCard.findElements(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag"));
                    if (numCounts.size() > 2 ){
                        WebElement companyStatuss = jobCard.findElement(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag:nth-child(1)"));
                        companyStatus = companyStatuss.getText();

                        WebElement companyNums = jobCard.findElement(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag:nth-child(2)"));
                        companyNum = companyNums.getText();

                        WebElement companyIndustrys = jobCard.findElement(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag:nth-child(3)"));
                        companyIndustry = companyIndustrys.getText();

                    }else {
                        WebElement companyNums = jobCard.findElement(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag:nth-child(1)"));
                        companyNum = companyNums.getText();

                        WebElement companyIndustrys = jobCard.findElement(By.cssSelector(".companyinfo__tag > div.joblist-box__item-tag:nth-child(2)"));
                        companyIndustry = companyIndustrys.getText();
                    }


                    WebElement addresses = jobCard.findElement(By.cssSelector(".jobinfo__other-info .jobinfo__other-info-item:first-child span"));
                    String address = addresses.getText();

                    WebElement jobHrefs = jobCard.findElement(By.cssSelector(".jobinfo__top .jobinfo__name"));
                    String jobHref = jobHrefs.getAttribute("href");

                    WebElement jobSalarys = jobCard.findElement(By.cssSelector(".jobinfo__top p.jobinfo__salary"));
                    String jobSalary = jobSalarys.getText();

                    WebElement jobEducations = jobCard.findElement(By.cssSelector(".jobinfo__other-info-item:nth-of-type(3)"));
                    String jobEducation = jobEducations.getText();

                    WebElement jobExperiences = jobCard.findElement(By.cssSelector(".jobinfo__other-info-item:nth-of-type(2"));
                    String jobExperience = jobExperiences.getText();

                    WebElement hrs= jobCard.findElement(By.cssSelector(".companyinfo__staff-name"));
                    String hr = hrs.getText();

                    String jobSource = "ZhiLian";
                    String IndustryDirectory = config.getIndustryDirectory();
                    String city = ZhilianEnum.CityCode.forName(cityCode);

                    JobInfo jobInfo = new JobInfo();
                    jobInfo.setWorkExperience(jobExperience);
                    jobInfo.setEducation(jobEducation);
                    jobInfo.setAddress(address);
                    jobInfo.setCompanyName(companyName);
                    jobInfo.setJobPosition(jobPosition);
                    jobInfo.setTreatment(jobSalary);
                    jobInfo.setUpdateDate(new Date());
                    jobInfo.setSource(jobSource);
                    jobInfo.setHr(hr);
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
                }catch (Exception e) {
                    SeleniumUtil.sleep(4);
                }
            }
        }
    }

    private static boolean checkIsLimit() {
        try {
            SeleniumUtil.sleepByMilliSeconds(500);
            WebElement result = CHROME_DRIVER.findElement(By.xpath("//div[@class='a-job-apply-workflow']"));
            if (result.getText().contains("达到上限")) {
                log.info("今日投递已达上限！");
                isLimit = true;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static void setMaxPages() {
        try {
            // 模拟 Ctrl + End
            ACTIONS.keyDown(Keys.CONTROL).sendKeys(Keys.END).keyUp(Keys.CONTROL).perform();
            while (true) {
                WebElement button;
                WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='soupager']//a[position()=last()")));
                try {
                    button = CHROME_DRIVER.findElement(By.xpath("//div[@class='soupager']//a[position()=last()]"));
                } catch (Exception ignore) {
                    button = CHROME_DRIVER.findElement(By.xpath("//div[@class='soupager']//a[position()=last()]"));
                }
                if (button.getAttribute("disabled") != null) {
                    // 按钮被禁用，退出循环
                    break;
                }

                button.click();
            }
            WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='soupager']//a[position()=last()-1]")));
            WebElement lastPage = CHROME_DRIVER.findElement(By.xpath("//div[@class='soupager']//a[position()=last()-1]"));
            if (lastPage != null && lastPage.getText().matches("\\d+")) {
                maxPage = Integer.parseInt(lastPage.getText());
                log.info("设置最大页数：{}", maxPage);
            }
            // 模拟 Ctrl + Home
            ACTIONS.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
        } catch (Exception ignore) {
            maxPage = 500;
            log.info("setMaxPages@设置最大页数异常！默认设置最大页数是500");
        }
    }

    private static void printRecommendJobs(List<WebElement> jobs) {
        jobs.forEach(j -> {
            String jobName = j.findElement(By.xpath(".//*[contains(@class, 'recommend-job__position')]")).getText();
            String salary = j.findElement(By.xpath(".//span[@class='recommend-job__demand__salary']")).getText();
            String years = j.findElement(By.xpath(".//span[@class='recommend-job__demand__experience']")).getText().replaceAll("\n", " ");
            String education = j.findElement(By.xpath(".//span[@class='recommend-job__demand__educational']")).getText().replaceAll("\n", " ");
            String companyName = j.findElement(By.xpath(".//*[contains(@class, 'recommend-job__cname')]")).getText();
            String companyTag = j.findElement(By.xpath(".//*[contains(@class, 'recommend-job__demand__cinfo')]")).getText().replaceAll("\n", " ");
            Job job = new Job();
            job.setJobName(jobName);
            job.setSalary(salary);
            job.setCompanyTag(companyTag);
            job.setCompanyName(companyName);
            job.setJobInfo(years + "·" + education);
            log.info("投递【{}】公司【{}】岗位，薪资【{}】，要求【{}·{}】，规模【{}】", companyName, jobName, salary, years, education, companyTag);
            resultList.add(job);
        });
    }

    private static void login() {
        CHROME_DRIVER.get(loginUrl);
        if (SeleniumUtil.isCookieValid("./src/main/java/zhilian/cookie.json")) {
            SeleniumUtil.loadCookie("./src/main/java/zhilian/cookie.json");
            CHROME_DRIVER.navigate().refresh();
            SeleniumUtil.sleep(1);
        }
        if (isLoginRequired()) {
            scanLogin();
        }
    }

    private static void scanLogin() {
        try {
            WebElement button = CHROME_DRIVER.findElement(By.xpath("//div[@class='zppp-panel-normal-bar__img']"));
            button.click();
            log.info("等待扫码登录中...");
            WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='zp-main__personal']")));
            log.info("扫码登录成功！");
            SeleniumUtil.saveCookie("./src/main/java/zhilian/cookie.json");
        } catch (Exception e) {
            log.error("扫码登录异常！");
            System.exit(-1);
        }
    }

    private static boolean isLoginRequired() {
        return !CHROME_DRIVER.getCurrentUrl().contains("i.zhaopin.com");
    }
}
