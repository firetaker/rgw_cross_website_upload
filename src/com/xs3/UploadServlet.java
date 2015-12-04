package com.xs3;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;
import javax.servlet.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		PrintWriter writer=res.getWriter();
		String filename = req.getParameter("name");
		String fileseize = req.getParameter("size");
		String filetype = req.getParameter("type");
		
		System.out.println(filename + " " + fileseize + " " + filetype);
		String url  =  Util.xs3_generate_url(filename, filetype);
		
		res.setContentType("application/json;charset=utf-8");
        //res.addHeader("Access-Control-Allow-Origin", "http://test.xs3.com");
		JSONObject jsonObject = new JSONObject();  
        jsonObject.put("url", url);  
          
        JSONArray jsonArray = new JSONArray();  
        jsonArray.add(jsonObject);  
        
        System.out.println(jsonArray);   
        writer.write(jsonArray.toString()); 
        
		writer.flush();
        writer.close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json;charset=utf-8");
		PrintWriter writer=resp.getWriter();
		JSONObject jsonObject = new JSONObject();  
        jsonObject.put("url", "http://baidu.com");  
          
        JSONArray jsonArray = new JSONArray();  
        jsonArray.add(jsonObject);  
        
        writer.write(jsonArray.toString()); 
        
		writer.flush();
        writer.close();
	}

}
