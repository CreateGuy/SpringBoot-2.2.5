package org.lzx.springBootDemo.controller;

import org.lzx.springBootDemo.test.Test1;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class HelloController {

	public HelloController(Test1 test1) {
		System.out.println(1);
	}

	@GetMapping("hello")
	@ResponseBody
	public String hello(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(true);
		session.invalidate();

		response.flushBuffer();
		return "hello";
	}

	@GetMapping("hello1")
	@ResponseBody
	public String hello1() {
		return "hello1";
	}
}
