package org.lzx.springBootDemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lzx.springBootDemo.entity.Statements;
import org.lzx.springBootDemo.mapper.StatementsMapper;
import org.lzx.springBootDemo.service.IStatementsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 对账单列表 服务实现类
 * </p>
 *
 * @author zengfeiyue
 * @since 2022-05-13
 */
@Service
public class StatementsServiceImpl extends ServiceImpl<StatementsMapper, Statements> implements IStatementsService {

	@Resource
	private IStatementsService iStatementsService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateA() {
		Statements statements = new Statements();
		statements.setId(0L);
		statements.setOffStatus(1);
		baseMapper.updateById(statements);

		//iStatementsService.updateB();

		throw new RuntimeException();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateB() {
		Statements statements = new Statements();
		statements.setId(20080519129L);
		statements.setOffStatus(1);
		baseMapper.updateById(statements);

		throw new RuntimeException();
	}
}
