package by.fly.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.ext.spring.ApplicationContextHolder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LogbackConfig {

    public static final String LOG_PATTERN = "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg %n";
    public static final String LOG_DIR = System.getProperty("user.dir") + "/logs/";

    private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ConsoleAppender consoleAppender() {
        ConsoleAppender appender = new ConsoleAppender();
        appender.setContext(loggerContext);
        appender.setEncoder(patternLayoutEncoder());
        appender.addFilter(traceFilter());
        return appender;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThresholdFilter traceFilter() {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(loggerContext);
        filter.setLevel("TRACE");
        return filter;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThresholdFilter errorFilter() {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(loggerContext);
        filter.setLevel("ERROR");
        return filter;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public PatternLayoutEncoder patternLayoutEncoder() {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(LOG_PATTERN);
        return encoder;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HTMLLayout htmlLayout() {
        HTMLLayout layout = new HTMLLayout();
        layout.setContext(loggerContext);
        return layout;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SMTPAppender smtpAppender(
            @Value("${mail.username}") String username,
            @Value("${mail.password}") String password,
            @Value("${mail.to}") String to,
            @Value("${mail.subject}") String subject) {
        SMTPAppender appender = new SMTPAppender();
        appender.setContext(loggerContext);
        appender.setSmtpHost("smtp.gmail.com");
        appender.setSmtpPort(465);
        appender.setSSL(true);
        appender.setUsername(username);
        appender.setPassword(password);
        appender.setFrom(username);
        appender.addTo(to);
        appender.setSubject(subject);
        appender.setLayout(htmlLayout());
        appender.addFilter(errorFilter());
        return appender;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RollingFileAppender rollingFileAppender() {
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(loggerContext);
        appender.setFile(LOG_DIR + "fly.log");
        TimeBasedRollingPolicy<Object> rollingPolicy = rollingPolicy(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);
        appender.setEncoder(patternLayoutEncoder());
        return appender;
    }

    private TimeBasedRollingPolicy<Object> rollingPolicy(FileAppender appender) {
        TimeBasedRollingPolicy<Object> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setFileNamePattern(LOG_DIR + "fly.%d{yyyy-MM-dd}.log");
        rollingPolicy.setParent(appender);
        return rollingPolicy;
    }

}
