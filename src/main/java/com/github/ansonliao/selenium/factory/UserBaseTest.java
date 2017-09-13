package com.github.ansonliao.selenium.factory;

import com.aventstack.extentreports.ExtentTest;
import com.github.ansonliao.selenium.parallel.SeleniumParallel;
import com.github.ansonliao.selenium.report.factory.ExtentTestManager;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.testng.ITestResult.FAILURE;
import static org.testng.ITestResult.SKIP;
import static org.testng.ITestResult.SUCCESS;


public class UserBaseTest extends SeleniumParallel {
    public ExtentTest extentTest;

    @BeforeClass
    public void beforeClass(ITestContext iTestContext) {
        browserName = iTestContext.getCurrentXmlTest().getAllParameters()
                .get("browser").toString().trim();
        driverManager = DriverManagerFactory.getManager(
                BrowserUtils.getBrowserByString(Optional.of(browserName)));
        MyFileUtils.createScreenshotFolderForBrowser(this.getClass(), browserName);
        ExtentTestManager.createParentTest(this.getClass().getName(), getDescription(this.getClass()));
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        url = findUrl(method);
        setDriver(driverManager.getDriver());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult iTestResult) throws IOException {

        if (iTestResult.getStatus() == SUCCESS) {
            // add extent report log here
        }
        if (iTestResult.getStatus() == FAILURE) {
            String imgPrefix = takeScreenShot(iTestResult.getMethod().getMethodName());
            System.out.println("screenshot: " + imgPrefix);
            // add extent report log here
        }
        if (iTestResult.getStatus() == SKIP) {
            // add extent report log here
        }

        driverManager.quitDriver();
        ExtentTestManager.extentReport.flush();
    }

    public ExtentTest getExtentTest() {
        return extentTest;
    }
}
