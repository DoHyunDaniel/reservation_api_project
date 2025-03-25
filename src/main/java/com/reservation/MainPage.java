package com.reservation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class MainPage {
	@RequestMapping("/")
	public String index() {

		return "index page";
	}
	
	@ResponseBody
	@RequestMapping("/hello")
	public String hello(HttpServlet Request, HttpServletResponse Response) {
		String msg = "<html>" + "<head>" + "</head>" + "<body>" + "<p>hello</p> <p>everyone!</p>" + "</body>"
				+ "</html>";

		return msg;
	}

}
