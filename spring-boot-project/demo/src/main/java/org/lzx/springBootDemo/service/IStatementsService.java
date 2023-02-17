package org.lzx.springBootDemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lzx.springBootDemo.entity.Statements;

/**
 * <p>
 * 对账单列表 服务类
 * </p>
 *
 * @author zengfeiyue
 * @since 2022-05-13
 */
public interface IStatementsService extends IService<Statements> {

	void updateA();

	void updateB();
}
