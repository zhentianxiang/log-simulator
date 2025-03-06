package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogExceptionSimulator {
    private static final Logger logger = LoggerFactory.getLogger(LogExceptionSimulator.class);

    public static void main(String[] args) {
        boolean isNormalMode = args.length > 0 && "normal".equalsIgnoreCase(args[0]);

        int i = 0;
        while (true) {
            try {
                simulateBusinessLogic(i++, isNormalMode);
            } catch (Exception e) {
                String traceId = "TRACE_" + System.currentTimeMillis();
                logger.error("[TraceID: {}] 处理订单时发生异常: {}", traceId, e.getMessage(), e);
            }

            try {
                Thread.sleep(1000); // 每秒执行一次
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.warn("线程被中断，退出程序", ex);
                break;
            }
        }
    }

    private static void simulateBusinessLogic(int id, boolean isNormalMode) {
        logger.debug("开始处理订单，订单编号: {}", id);
        String orderId = isNormalMode ? ("ORDER_" + id) : (id % 2 == 0 ? null : ("ORDER_" + id));
        processOrder(orderId);
    }

    private static void processOrder(String orderId) {
        if (orderId == null) {
            logger.warn("业务校验失败: 订单ID不能为空");
            throw new IllegalArgumentException("订单ID不能为空");
        }

        if (orderId.isEmpty()) {
            logger.warn("业务校验失败: 订单ID内容为空");
            throw new NullPointerException("订单ID内容为空");
        }

        logger.info("[操作审计] 成功处理订单: {}", orderId);
    }
}