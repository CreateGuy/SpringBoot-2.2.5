package org.lzx.springBootDemo.test;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.system.JavaVersion;
import org.springframework.stereotype.Component;

@Component
//@ConditionalOnJava(value = JavaVersion.ELEVEN)
public class Test1 {
	public Test1() {
		System.out.println(2);
	}
}
