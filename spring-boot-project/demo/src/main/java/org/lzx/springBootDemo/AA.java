package org.lzx.springBootDemo;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource(value = "classpath:application.yml")
public class AA{

	@Autowired
	private BB bb;

	@Value("${server.port}")
	private String port;

	//@Lookup
	//public BB printf() {
	//	return null;
	//}

	//@Bean
	//public AutoClient autoClient() {
	//	return new AutoClient();
	//}

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
