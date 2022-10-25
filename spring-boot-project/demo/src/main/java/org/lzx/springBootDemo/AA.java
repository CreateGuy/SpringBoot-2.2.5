package org.lzx.springBootDemo;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author liuzhixuan
 * @date 2022-10-25
 */
@Component(value = "aa")
//@DependsOn(value = "autoClient")
public class AA {

	@Resource
	private BB bb;
}

@Component(value = "bb")
class BB {

	@Resource
	private AA aa;
}
