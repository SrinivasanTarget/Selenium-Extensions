package com.github.ansonliao.selenium.parallel;

import com.github.ansonliao.selenium.annotations.Description;
import com.github.ansonliao.selenium.annotations.Edge;
import com.github.ansonliao.selenium.annotations.Headless;
import com.github.ansonliao.selenium.annotations.IgnoreFirefox;
import com.github.ansonliao.selenium.annotations.Incognito;
import com.github.ansonliao.selenium.annotations.URL;
import com.github.ansonliao.selenium.factory.DriverManager;
import com.github.ansonliao.selenium.factory.DriverManagerFactory;
import com.github.ansonliao.selenium.internal.Constants;
import com.github.ansonliao.selenium.utils.BrowserUtils;
import com.github.ansonliao.selenium.utils.MyFileUtils;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.apache.log4j.Logger;
import org.hamcrest.generator.qdox.JavaDocBuilder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


public class SeleniumParallel {
    protected static Logger logger = Logger.getLogger(SeleniumParallel.class);

    private WebDriver driver;
    protected DriverManager driverManager;
    protected String browserName;
    private boolean isIncognito;
    protected String url;

    /**
    private WebDriver driver;

    public SeleniumParallel(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    @BeforeClass
    @Parameters({"browser"})
    public void beforeClass(String browser) {
        browserName = browser;
    }

    public boolean isIncognito(Method method) {
        isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
        return isIncognito;
    }
    */

    public String findUrl(Method method) {
        if (method.isAnnotationPresent(URL.class)) {
            url = method.getAnnotation(URL.class).value();
        } else if (method.getDeclaringClass().isAnnotationPresent(URL.class)) {
            url = method.getDeclaringClass().getAnnotation(URL.class).value();
        } else {
            url = null;
        }

        return url;

    }

    public String getUrl() {
        return url;
    }

    public WebDriver startWebDriver(Method method) {
        driverManager.isIncognito = method.isAnnotationPresent(Incognito.class) ? true : false;
        driverManager.isHeadless = method.isAnnotationPresent(Headless.class) ? true : false;
        driver = driverManager.getDriver();
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public WebDriver openUrl(String url) {
        getDriver().get(url);
        return getDriver();
    }

    protected String takeScreenShot(String imgPrefix) throws IOException {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

        String destDir = Constants.SCREENSHOT_DIR
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.getClass().getPackage().getName())
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.getClass().getSimpleName())
                .concat(Constants.FILE_SEPARATOR)
                .concat(this.browserName)
                .concat(Constants.FILE_SEPARATOR)
                .concat(imgPrefix)
                .concat("_")
                .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))
                .concat(".jpeg");
        MyFileUtils.copyFile(scrFile, new File(destDir));
        return destDir.replace(Constants.PROJECT_ROOT_DIR + Constants.FILE_SEPARATOR, "");
    }

    public String getAuthors(String className, ITestNGMethod method) {
        logger.info("className = " + className);

        JavaDocBuilder builder = new JavaDocBuilder();
        JavaClass cls = (JavaClass) builder.getClassByName(className);
        List<DocletTag> authors = cls.getTagsByName("author");
        logger.info("authors = " + authors.toString());

        // get class authors as default author name
        String allAuthors = "";
        if (authors.size() != 0) {
            for (DocletTag author : authors) {
                if (author.getValue().trim().length() > 0) {
                    allAuthors += author.getValue() + " ";
                }
            }
        }

        // get method author
        List<JavaMethod> methods = cls.getMethods();
        logger.info("JavaMethod = " + methods.toString());
        JavaMethod mth = methods
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase(method.getMethodName()))
                .findFirst()
                .get();

        authors = mth.getTagsByName("author");
        if (authors.size() != 0) {
            allAuthors = "";
            for (DocletTag author : authors) {
                allAuthors += author.getValue() + " ";
            }
        }

        return allAuthors.trim();
    }

    protected String getDescription(Object object) {
        String description = null;
        if (object instanceof Class) {
            Class clazz = (Class) object;
            if (clazz.isAnnotationPresent(Description.class)) {
                description = clazz.getAnnotation(Description.class).toString().trim();
                return description;
            }
        }
        if (object instanceof Method) {
            Method method = (Method) object;
            if (method.isAnnotationPresent(Description.class)) {
                description = method.getDeclaredAnnotation(Description.class).toString().trim();
                return description;
            }
        }

        return description;
    }

    @Edge
    @IgnoreFirefox
    public static void main(String[] args) {
        SeleniumParallel parallel = new SeleniumParallel();
//        System.out.println(parallel.getBrowsers());

    }
}
