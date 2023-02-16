package org.lzx.springBootDemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.lzx.springBootDemo.entity.Statements;
import org.lzx.springBootDemo.mapper.StatementsMapper;
import org.lzx.springBootDemo.test.Test1;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Controller
public class HelloController {

	@Resource
	private StatementsMapper statementsMapper;

	@GetMapping("hello")
	@ResponseBody
	@Transactional
	public String hello(HttpServletRequest request, HttpServletResponse response) throws IOException {
		A();
		return "hello";
	}

	@Transactional(rollbackFor = Exception.class)
	public void A() {
		Statements statements = new Statements();
		statements.setId(0L);
		statements.setOffStatus(1);
		statementsMapper.updateById(statements);

		B();
	}

	@Transactional(rollbackFor = Exception.class)
	public void B() {
		Statements statements = new Statements();
		statements.setId(0L);
		statements.setOffStatus(2);
		statementsMapper.updateById(statements);

		//throw new RuntimeException();
	}
}
