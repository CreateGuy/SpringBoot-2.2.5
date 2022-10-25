package org.lzx.springBootDemo;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author liuzhixuan
 * @date 2022-10-25
 */
//@Component
public class AA implements FactoryBean {

	private Long id;
	private String userName;

	@Override
	public Object getObject() throws Exception {
		return new AA();
	}

	@Override
	public Class<?> getObjectType() {
		return AA.class;
	}
}
