package org.lzx.springBootDemo.controller;

import org.lzx.springBootDemo.service.IStatementsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HelloController {

	@Resource
	private IStatementsService statementsService;

	@GetMapping("hello")
	@ResponseBody
	public String hello(HttpServletRequest request, HttpServletResponse response) throws IOException {
		statementsService.updateA();
		return "hello";
	}
}
