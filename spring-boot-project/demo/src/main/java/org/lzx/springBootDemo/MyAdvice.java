package org.lzx.springBootDemo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MyAdvice {

	/**
	 * 切点名称：myPointCut
	 * 执行环境：执行过程中即：execution(表示 目标地址下所有类的所有方法，不限定参数)
	 */
	@Pointcut(value = "execution( * org.lzx.springBootDemo.controller.*.*(..))")
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
	@After(value = "myPointCut()")
	public Object myLogger2(JoinPoint joinPoint) throws Throwable {
		Signature signature = joinPoint.getSignature();
		return "111";
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
	@Before(value = "myPointCut()")
	public Object myLogger(JoinPoint joinPoint) throws Throwable {
		Signature signature = joinPoint.getSignature();
		return "111";
	}

}
