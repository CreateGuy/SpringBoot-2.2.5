package org.lzx.springBootDemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MyAdvice {

	/**
	 * 切点名称：myPointCut
	 * 执行环境：执行过程中即：execution(表示 目标地址下所有类的所有方法，不限定参数)
	 */
	@Pointcut(value = "execution( * org.lzx.springBootDemo.AA.*(..))")
	public void myPointCut() {
	}

	/**
	 * 定制通知
	 *
	 * @param proceedingJoinPoint 只能用在环绕通知中，其他4类只用JoinPoint
	 *                            ProceedingJoinPoint暴露了运行时状态
	 * @return
	 * @throws Throwable
	 * @Around 表示环绕通知，从myPointCut切点进去，增强点
	 */
	@Around("myPointCut()")
	public Object myLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		return null;
	}

}
