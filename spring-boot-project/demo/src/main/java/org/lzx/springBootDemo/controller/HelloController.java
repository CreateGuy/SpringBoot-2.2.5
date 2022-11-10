package org.lzx.springBootDemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class HelloController {

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
