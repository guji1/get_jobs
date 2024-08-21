package liepin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import entity.JobInfo;
import lombok.SneakyThrows;
import mapper.JobInfoMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JobUtils;
import utils.SeleniumUtil;
import zhilian.ZhilianEnum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static utils.Bot.sendMessage;
import static utils.Constant.*;
import static utils.SeleniumUtil.isCookieValid;
import static utils.SeleniumUtil.sleep;

public class LiepinCount {
    private static final Logger log = LoggerFactory.getLogger(LiepinCount.class);
    static String homeUrl = "https://www.liepin.com/";
    static String cookiePath = "./src/main/java/liepin/cookie.json";
    static int maxPage = 50;
    static List<String> resultList = new ArrayList<>();
    static String baseUrl = "https://www.liepin.com/zhaopin/?";
    static LiepinConfig config = LiepinConfig.init();


    public static void main(String[] args) {
        SeleniumUtil.initDriver();
        login();
        for (String cityCode : config.getCityCodes()) {
            for (String keyword : config.getKeywords()) {
                submit(keyword,cityCode);
            }
        }

        printResult();
        CHROME_DRIVER.close();
        CHROME_DRIVER.quit();
    }

    private static void printResult() {
        String message = String.format("【猎聘】投递完成,共投递 %d 个岗位！\n今日投递岗位:\n%s",
                resultList.size(),
                String.join("\n", resultList));
        log.info(message);
        sendMessage(message);
    }

    @SneakyThrows
    private static void submit(String keyword,String cityCode) {
        CHROME_DRIVER.get(getSearchUrl(cityCode) + "&key=" + keyword);
        WebElement div = null ;
        try{
            WAIT.until(ExpectedConditions.presenceOfElementLocated(By.className("list-pagination-box")));
            div = CHROME_DRIVER.findElement(By.className("list-pagination-box"));
            List<WebElement> lis = div.findElements(By.tagName("li"));
            int page = Integer.parseInt(lis.get(lis.size() - 2).getText());
            if (page > 1) {
                maxPage = page;
            }
        }catch (Exception e){
            log.info("设置最大页出错！");
            SeleniumUtil.sleep(5);
            WAIT.until(ExpectedConditions.presenceOfElementLocated(By.className("list-pagination-box")));
            div = CHROME_DRIVER.findElement(By.className("list-pagination-box"));
            List<WebElement> lis = div.findElements(By.tagName("li"));
            int page = Integer.parseInt(lis.get(lis.size() - 2).getText());
            if (page > 1) {
                maxPage = page;
            }
        }

        for (int i = 0; i < maxPage; i++) {
            log.info("正在投递【{}】第【{}】页...", keyword, i + 1);
            submitJob(cityCode,keyword);
            log.info("已投递第【{}】页所有的岗位...\n", i + 1);
            div = CHROME_DRIVER.findElement(By.className("list-pagination-box"));
            WebElement nextPage = div.findElement(By.xpath(".//li[@title='Next Page']"));
            if (nextPage.getAttribute("disabled") == null) {
                nextPage.click();
            } else {
                break;
            }
        }
        log.info("【{}】关键词投递完成！", keyword);
    }

    private static String getSearchUrl(String cityCode) {
        return baseUrl +
                JobUtils.appendParam("city", cityCode) +
                JobUtils.appendParam("salary", config.getSalary()) +
                "&currentPage=" + 0 + "&dq=" + cityCode;
    }


    private static void setMaxPage(List<WebElement> lis) {
        try {

        } catch (Exception ignored) {
        }
    }

    private static void submitJob(String cityCode,String keyword) {
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.job-list-box div[style*='margin-bottom']")));
        List<WebElement> elements = CHROME_DRIVER.findElements(By.cssSelector("div.job-list-box div[style*='margin-bottom']"));
        for (WebElement jobCard : elements) {
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

            String jobSource = "LiePin";
            String IndustryDirectory = config.getIndustryDirectory();
            String city = LiepinEnum.CityCode.forName(cityCode);

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
        }

    }

    @SneakyThrows
    private static void login() {
        log.info("正在打开猎聘网站...");
        CHROME_DRIVER.get(homeUrl);
        log.info("猎聘正在登录...");
        if (isCookieValid(cookiePath)) {
            SeleniumUtil.loadCookie(cookiePath);
            CHROME_DRIVER.navigate().refresh();
        }
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("header-logo-box")));
        if (isLoginRequired()) {
            log.info("cookie失效，尝试扫码登录...");
            scanLogin();
            SeleniumUtil.saveCookie(cookiePath);
        } else {
            log.info("cookie有效，准备投递...");
        }
    }

    private static boolean isLoginRequired() {
        String currentUrl = CHROME_DRIVER.getCurrentUrl();
        return !currentUrl.contains("c.liepin.com");
    }

    private static void scanLogin() {
        try {
            SeleniumUtil.click(By.className("switch-login-type-btn-box"));
            log.info("等待扫码..");
            boolean isLoggedIn = false;

            // 一直循环，直到元素出现（用户扫码登录成功）
            while (!isLoggedIn) {
                try {
                    isLoggedIn = !CHROME_DRIVER.findElements(By.xpath("//*[@id=\"main-container\"]/div/div[3]/div[2]/div[3]/div[1]/div[1]")).isEmpty();
                } catch (Exception ignored) {
                    SeleniumUtil.sleep(1);
                }
            }
            log.info("用户扫码成功，继续执行...");
        } catch (Exception e) {
            log.error("scanLogin() 失败: {}", e.getMessage());
        }
    }


}
