package com.xs3;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CompleteMultiUpload extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		PrintWriter writer=res.getWriter();
		String filename = req.getParameter("file_name");
		String uploadId = req.getParameter("upload_id");
		String filetype = req.getParameter("file_type");
		
		System.out.println(filename + " " + uploadId + " " + filetype);
				
		res.setContentType("application/json;charset=utf-8");
		Util.xs3_coplete_multi_upload(filename, uploadId);
        writer.write("ok"); 
        
		writer.flush();
        writer.close();
	}
}

