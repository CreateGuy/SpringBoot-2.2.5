package org.lzx.springBootDemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HelloController {

	@GetMapping("hello")
	@ResponseBody
	public String hello() {
		return "hello";
	}

	@GetMapping("hello1")
	@ResponseBody
	public String hello1() {
		return "hello1";
	}
}
