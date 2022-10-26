package org.lzx.springBootDemo;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuzhixuan
 * @date 2022-10-25
 */
@Component(value = "aa")
//@DependsOn(value = "autoClient")
public class AA extends  BB{

	@Resource
	private BB bb;

	//@Lookup
	//public BB printf() {
	//	return null;
	//}

	@Bean
	public AutoClient autoClient() {
		return new AutoClient();
	}

	@PostConstruct
	public void postConstruct(){

	}

	@PostConstruct
	public void postConstruct2(){

	}

//	@PreDestroy
	@Resource
	public void preDestroy(AutoClient autoClient){

	}
}

@Component(value = "bb")
class BB {

	@Resource
	private AA aa;
}
