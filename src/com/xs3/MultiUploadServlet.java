package com.xs3;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MultiUploadServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse res)
				throws ServletException, IOException {
			
			PrintWriter writer=res.getWriter();
			String filename = req.getParameter("name");
			String filesize = req.getParameter("size");
			String filetype = req.getParameter("type");
			
			System.out.println(filename + " " + filesize + " " + filetype);
					
			res.setContentType("application/json;charset=utf-8");
			String json_urls = Util.xs3_init_multi_upload(filename,Integer.parseInt(filesize), filetype);
	        writer.write(json_urls); 
	        
			writer.flush();
	        writer.close();
		}
	}
