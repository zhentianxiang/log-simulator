package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class LogExceptionSimulator {
    private static final Logger logger = LoggerFactory.getLogger(LogExceptionSimulator.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();

    public static void main(String[] args) {
        SpringApplication.run(LogExceptionSimulator.class, args);
        startLogging();
    }

    private static void startLogging() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                simulateBusinessLogic();
            } catch (Exception e) {
                String traceId = "TRACE_" + System.currentTimeMillis();
                logger.error("{} [main] ERROR  com.example.MyService - [TraceID: {}] 处理订单时发生异常: {}",
                        getCurrentTimestamp(), traceId, e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void simulateBusinessLogic() {
        // 模拟 10% 概率抛出异常
        if (random.nextInt(10) < 1) {
            throw new RuntimeException("模拟业务处理错误！");
        }

        // 记录成功日志
        logger.info("{} [main] INFO  com.example.MyService - [TraceID: {}] 成功处理订单: ORDER_{}",
                getCurrentTimestamp(), System.currentTimeMillis(), (int) (Math.random() * 1000));

        // 模拟警告日志
        if (random.nextInt(10) < 3) {
            logger.warn("{} [main] WARN  com.example.MyService - [TraceID: {}] 订单处理可能存在延迟: ORDER_{}",
                    getCurrentTimestamp(), System.currentTimeMillis(), (int) (Math.random() * 1000));
        }
    }

    private static String getCurrentTimestamp() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
}

@RestController
@RequestMapping("/logs")
class LogController {
    private final Random random = new Random();  // 在 LogController 中定义 random

    @GetMapping("/view")
    public String viewLogsPage() {
        return "<html><head><title>日志查看</title></head><body>"
                + "<h2>此程序为教学程序</h2>"
                + "<button onclick=\"startListening()\">查看服务实时运行日志</button>"
                + "<pre id='logOutput'></pre>"
                + "<script>"
                + "function startListening() {"
                + "    var eventSource = new EventSource('/logs/stream');"
                + "    eventSource.onmessage = function(event) {"
                + "        var logMessage = event.data;"
                + "        var logLevel = logMessage.split(' ')[4];" // 提取日志级别
                + "        var logOutput = document.getElementById('logOutput');"
                + "        if (logLevel === 'ERROR') {"
                + "            logOutput.innerHTML += '<span style=\"color:red\">' + logMessage + '</span><br>';"
                + "        } else if (logLevel === 'WARN') {"
                + "            logOutput.innerHTML += '<span style=\"color:orange\">' + logMessage + '</span><br>';"
                + "        } else {"
                + "            logOutput.innerHTML += '<span style=\"color:green\">' + logMessage + '</span><br>';"
                + "        }"
                + "    };"
                + "}"
                + "</script>"
                + "</body></html>";
    }

    @GetMapping("/stream")
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                while (true) {
                    String logMessage = generateLogMessage();
                    emitter.send(logMessage);
                    Thread.sleep(1000); // 每秒发送一次日志
                }
            } catch (IOException | InterruptedException e) {
                emitter.complete();
            }
        });
        return emitter;
    }

    private String generateLogMessage() {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        long traceId = System.currentTimeMillis();
        int orderId = random.nextInt(1000);

        // 随机生成日志级别
        int level = random.nextInt(10); // 0-9，随机选取日志级别
        String logMessage = "";

        if (level < 1) {
            // 模拟 ERROR 级别日志
            logMessage = String.format("%s [main] ERROR com.example.MyService - [TraceID: %d] 处理订单时发生异常: 模拟业务处理错误！", timestamp, traceId);
        } else if (level < 3) {
            // 模拟 WARN 级别日志
            logMessage = String.format("%s [main] WARN  com.example.MyService - [TraceID: %d] 订单处理可能存在延迟: ORDER_%d", timestamp, traceId, orderId);
        } else {
            // 模拟 INFO 级别日志
            logMessage = String.format("%s [main] INFO  com.example.MyService - [TraceID: %d] 成功处理订单: ORDER_%d", timestamp, traceId, orderId);
        }

        return logMessage;
    }
}
