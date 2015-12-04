package com.xs3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.util.DateUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PartSummary;

public class Util {

	public static String xs3_access_key = "your-access-key";
	public static String xs3_secret_key = "your-secret-key";
	public static String xs3_endpoint = "s3.bj.xs3cnc.com";
	public static String xs3_bucketname = "your-bucket-name";

	static AmazonS3 xs3_client;
	
	public static String gen_part_url(String uploadId,int file_size, String file_name, String file_type)
	{
		AWSCredentials xs3_credentials = new BasicAWSCredentials(
				xs3_access_key, xs3_secret_key);
		ClientConfiguration xs3_clientconfig = new ClientConfiguration();
		xs3_clientconfig.setProtocol(Protocol.HTTP);

		S3ClientOptions xs3_client_options = new S3ClientOptions();
		xs3_client_options.setPathStyleAccess(true);

		xs3_client = new AmazonS3Client(xs3_credentials, xs3_clientconfig);
		xs3_client.setEndpoint(xs3_endpoint);
		xs3_client.setS3ClientOptions(xs3_client_options);

		try {
			
			final int xs3_part_size = 1024 * 1024 * 5;

			int xs3_part_count = (int) Math.ceil((double) (file_size)
					/ (double) xs3_part_size);
			JSONArray jsonArray = new JSONArray();  
			JSONObject jsonObject_1 = new JSONObject();  
			jsonObject_1.put("total_num", xs3_part_count);  
			jsonObject_1.put("upload_id", uploadId);  
			
			JSONArray jsonArray_sub = new JSONArray();
			for (int part_no = 0; part_no < xs3_part_count; part_no++) {
				
				long xs3_offset_bytes = xs3_part_size * part_no;
				
				long part_size = xs3_part_size < (file_size - xs3_offset_bytes) ? xs3_part_size
						: (file_size - xs3_offset_bytes);

				java.util.Date expiration = new java.util.Date();
				long milliSeconds = expiration.getTime();
				milliSeconds += 1000 * 60 * 5;
				expiration.setTime(milliSeconds);
				
				GeneratePresignedUrlRequest xs3_genurl_req = new GeneratePresignedUrlRequest(
						xs3_bucketname, file_name);
				xs3_genurl_req.setMethod(HttpMethod.PUT);
				xs3_genurl_req.setExpiration(expiration);
				xs3_genurl_req.setContentType(file_type);
				xs3_genurl_req.addRequestParameter("uploadId", uploadId);
				xs3_genurl_req.addRequestParameter("partNumber", String.valueOf(part_no));

				URL url = xs3_client.generatePresignedUrl(xs3_genurl_req);
				System.out.println(url.toString());
				
			
				JSONObject jsonObject = new JSONObject();  
		        jsonObject.put("part_idx", part_no);  
		        jsonObject.put("part_url", url.toString());
		        jsonObject.put("upload_len", part_size);
		        jsonObject.put("part_begin", xs3_offset_bytes);
		        jsonObject.put("part_end", xs3_offset_bytes + part_size);
		        
		        jsonArray_sub.add(jsonObject); 
			}
			
			jsonObject_1.put("multi_list", jsonArray_sub);
			jsonArray.add(jsonObject_1);
			
			return jsonArray.toString();

		} catch (AmazonServiceException ase) {
			System.out.println("xs3_svr_error_message:" + ase.getMessage());
			System.out.println("xs3_svr_status_code:  " + ase.getStatusCode());
			System.out.println("xs3_svr_error_code:   " + ase.getErrorCode());
			System.out.println("xs3_svr_error_type:   " + ase.getErrorType());
			System.out.println("xs3_svr_request_id:   " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("xs3_clt_error_message:" + ace.getMessage());
		}

		return null;
	}

	public static String xs3_init_multi_upload(String xs3_objname, int file_size, String file_type) {
		AWSCredentials xs3_credentials = new BasicAWSCredentials(
				xs3_access_key, xs3_secret_key);
		ClientConfiguration xs3_clientconfig = new ClientConfiguration();
		xs3_clientconfig.setProtocol(Protocol.HTTP);
   
		S3ClientOptions xs3_client_options = new S3ClientOptions();
		xs3_client_options.setPathStyleAccess(true);

		xs3_client = new AmazonS3Client(xs3_credentials, xs3_clientconfig);
		xs3_client.setEndpoint(xs3_endpoint);
		xs3_client.setS3ClientOptions(xs3_client_options);

		try {
			InitiateMultipartUploadRequest xs3_multi_req = new InitiateMultipartUploadRequest(
					xs3_bucketname, xs3_objname);
			xs3_multi_req.setCannedACL(CannedAccessControlList.PublicRead);
			ObjectMetadata xs3_meta = new ObjectMetadata();
			xs3_meta.setContentType(file_type);
			xs3_multi_req.setObjectMetadata(xs3_meta);
			
			InitiateMultipartUploadResult xs3_multi_res = xs3_client
					.initiateMultipartUpload(xs3_multi_req);

			String xs3_multi_uploadid = xs3_multi_res.getUploadId();
            
			String json_urls = gen_part_url(xs3_multi_uploadid, file_size,xs3_objname,file_type);
			return json_urls;

		} catch (AmazonServiceException ase) {
			System.out.println("xs3_svr_error_message:" + ase.getMessage());
			System.out.println("xs3_svr_status_code:  " + ase.getStatusCode());
			System.out.println("xs3_svr_error_code:   " + ase.getErrorCode());
			System.out.println("xs3_svr_error_type:   " + ase.getErrorType());
			System.out.println("xs3_svr_request_id:   " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("xs3_clt_error_message:" + ace.getMessage());
		}
		return null;
	}
	
	public static List<PartETag> listPartsXml(String obj_name, String uploadId) {
		List<PartETag> parts = new ArrayList<PartETag>();
		BufferedInputStream xs3_instream = null;
		BufferedOutputStream xs3_outstream = null;
		try {
			URL url = new URL("http://" + xs3_endpoint + "/" + xs3_bucketname
					+ "/" + obj_name + "?uploadId=" + uploadId);
			HttpURLConnection http_conn = (HttpURLConnection) url
					.openConnection();
			http_conn.setDoOutput(true);
			http_conn.setRequestMethod("GET");

			System.out.println("xs3_http_status : "
					+ http_conn.getResponseCode());

			System.out.println("xs3_http_headers: "
					+ http_conn.getHeaderFields());


			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(http_conn.getInputStream());
			NodeList list = null;
			list = document
					.getElementsByTagName("ListMultipartUploadResult");
			if (null == list) {
				System.out.println("ERR no root element!");
				return null;
			} else {
				NodeList partList = document.getElementsByTagName("Part");
				if (null == partList) {
					System.out.println("ERR no initList element!");
					return null;
				} else {
					for (int i = 0; i < partList.getLength(); i++) {
						Node item = partList.item(i);
						NodeList chiList = item.getChildNodes();
						if (null != chiList) {
							int PartNumber = 0;
							String eTag = null;
							for (int j = 0; j < chiList.getLength(); j++) {
								Node childitem = chiList.item(j);
								if (childitem.getNodeType() == Node.ELEMENT_NODE) {
															
									
									if("PartNumber".equals(childitem.getNodeName()))
									{
										System.out.println(childitem.getFirstChild().getNodeValue());
										PartNumber = Integer.parseInt(childitem.getFirstChild().getNodeValue());
									}
									
									if("ETag".equals(childitem.getNodeName()))
									{
										System.out.println(childitem.getFirstChild().getNodeValue());
										eTag = childitem.getFirstChild().getNodeValue();
									}
									
								}
							}
							PartETag ptag = new PartETag(PartNumber, eTag);
							parts.add(ptag);
						}
						
					}
				}
			}
			http_conn.disconnect();
			return parts;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static void xs3_coplete_multi_upload(String xs3_objname,
			String uploadId) {
		AWSCredentials xs3_credentials = new BasicAWSCredentials(
				xs3_access_key, xs3_secret_key);
		ClientConfiguration xs3_clientconfig = new ClientConfiguration();
		xs3_clientconfig.setProtocol(Protocol.HTTP);

		S3ClientOptions xs3_client_options = new S3ClientOptions();
		xs3_client_options.setPathStyleAccess(true);

		xs3_client = new AmazonS3Client(xs3_credentials, xs3_clientconfig);
		xs3_client.setEndpoint(xs3_endpoint);
		xs3_client.setS3ClientOptions(xs3_client_options);

		try {
			List<PartETag> rest_parts = listPartsXml(xs3_objname,uploadId);
            if(null  == rest_parts)
            {
            	return;
            }
            for(PartETag item : rest_parts)
            {
            	System.out.println(item.getETag() + " -> " + item.getPartNumber());
            }
            

			CompleteMultipartUploadRequest comp_req = new CompleteMultipartUploadRequest(
					xs3_bucketname, xs3_objname, uploadId, rest_parts);
		
			CompleteMultipartUploadResult comp_result = xs3_client
					.completeMultipartUpload(comp_req);
			
			System.out.println(comp_result.getETag());
			System.out.println(comp_result.getKey());
		} catch (AmazonServiceException ase) {
			System.out.println("xs3_svr_error_message:" + ase.getMessage());
			System.out.println("xs3_svr_status_code:  " + ase.getStatusCode());
			System.out.println("xs3_svr_error_code:   " + ase.getErrorCode());
			System.out.println("xs3_svr_error_type:   " + ase.getErrorType());
			System.out.println("xs3_svr_request_id:   " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("xs3_clt_error_message:" + ace.getMessage());
		}
	}

	public static String xs3_generate_url(String xs3_objname,
			String content_type) {
		AWSCredentials xs3_credentials = new BasicAWSCredentials(
				xs3_access_key, xs3_secret_key);
		ClientConfiguration xs3_clientconfig = new ClientConfiguration();
		xs3_clientconfig.setProtocol(Protocol.HTTP);

		S3ClientOptions xs3_client_options = new S3ClientOptions();
		xs3_client_options.setPathStyleAccess(true);

		xs3_client = new AmazonS3Client(xs3_credentials, xs3_clientconfig);
		xs3_client.setEndpoint(xs3_endpoint);
		xs3_client.setS3ClientOptions(xs3_client_options);

		try {
			java.util.Date expiration = new java.util.Date();

			long milliSeconds = expiration.getTime();
			milliSeconds += 1000 * 60 * 5;
			expiration.setTime(milliSeconds);

			GeneratePresignedUrlRequest xs3_genurl_req = new GeneratePresignedUrlRequest(
					xs3_bucketname, xs3_objname);
			xs3_genurl_req.setMethod(HttpMethod.PUT);
			xs3_genurl_req.setExpiration(expiration);
			xs3_genurl_req.setContentType(content_type);
			xs3_genurl_req.addRequestParameter("x-amz-acl", "public-read");

			URL url = xs3_client.generatePresignedUrl(xs3_genurl_req);

			System.out.println(url.toString());
			return url.toString();

		} catch (AmazonServiceException ase) {
			System.out.println("xs3_svr_error_message:" + ase.getMessage());
			System.out.println("xs3_svr_status_code:  " + ase.getStatusCode());
			System.out.println("xs3_svr_error_code:   " + ase.getErrorCode());
			System.out.println("xs3_svr_error_type:   " + ase.getErrorType());
			System.out.println("xs3_svr_request_id:   " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("xs3_clt_error_message:" + ace.getMessage());
		}

		return null;
	}

	public static void xs3_upload_by_url(URL xs3_url, String file_path)
			throws IOException {

		BufferedInputStream xs3_instream = null;
		BufferedOutputStream xs3_outstream = null;
		try {
			HttpURLConnection http_conn = (HttpURLConnection) xs3_url
					.openConnection();
			http_conn.setDoOutput(true);
			http_conn.setRequestMethod("PUT");

			http_conn.setRequestProperty("Content-Type", "image/jpeg");
			http_conn.setRequestProperty("x-amz-acl", "public-read");

			xs3_outstream = new BufferedOutputStream(
					http_conn.getOutputStream());
			xs3_instream = new BufferedInputStream(new FileInputStream(
					new File(file_path)));

			byte[] buffer = new byte[1024];
			int xs3_offset = 0;
			while ((xs3_offset = xs3_instream.read(buffer)) != -1) {
				xs3_outstream.write(buffer, 0, xs3_offset);
				xs3_outstream.flush();
			}

			System.out.println("xs3_http_status : "
					+ http_conn.getResponseCode());
			System.out.println("xs3_http_headers: "
					+ http_conn.getHeaderFields());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != xs3_outstream)
				xs3_outstream.close();
			if (null != xs3_instream)
				xs3_instream.close();
		}
	}

}
