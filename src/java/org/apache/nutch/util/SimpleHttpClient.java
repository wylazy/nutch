package org.apache.nutch.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SimpleHttpClient {

	private static SimpleHttpClient simpleClient = new SimpleHttpClient();
	private DefaultHttpClient httpClient;
	
	private SimpleHttpClient() {
		httpClient = new DefaultHttpClient();
	}
	
	public static SimpleHttpClient getInstance() {
		return simpleClient;
	}
	
	public String get(String url) throws ClientProtocolException, IOException {
		return get(url, null);
	}
	
	public String get(String url, List<BasicNameValuePair> params) throws ClientProtocolException, IOException {
		
		if (params != null && !params.isEmpty()) {
			if (url.indexOf('?') > 0) {
				url += '&';
			} else {
				url += '?';
			}
			url += URLEncodedUtils.format(params, "UTF-8");
		}
		
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpGet);
		return EntityUtils.toString(response.getEntity(), "utf-8");
	}
	
	public String post(String url, List<BasicNameValuePair> params) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		HttpResponse response = httpClient.execute(httpPost);
		return EntityUtils.toString(response.getEntity(), "utf-8");
	}
	
	public static void main(String [] args) throws ClientProtocolException, IOException {
		SimpleHttpClient client = SimpleHttpClient.getInstance();
		String url = "http://solr.free4lab.com/addWebPage";
		List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
		
		params.add(new BasicNameValuePair("url", "a"));
		params.add(new BasicNameValuePair("title", "a"));
		params.add(new BasicNameValuePair("content", "a"));
		
		System.out.println(client.post(url, params));
	}
}
