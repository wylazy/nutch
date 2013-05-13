package org.apache.nutch.parse.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.html.DOMContentUtils;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.SimpleHttpClient;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ContentHtmlParser implements HtmlParseFilter {

	public static final Logger logger = LoggerFactory.getLogger(ContentHtmlParser.class);
	private static final String PARSE_CONTENT_URL = "parse.content.url";
	private static final String PARSE_CONTENT_CLASS = "parse.content.class";
	
	private String baseUrl;
	private String contentClass;
	private String storageUrl;
	
	private Configuration conf;
	private DOMContentUtils domUtils;
	
	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
		
		domUtils = new DOMContentUtils(conf);
		if (conf != null) {
			storageUrl = conf.get("storage.webpage.url", null);
			baseUrl = conf.get(PARSE_CONTENT_URL, null);
			contentClass = conf.get(PARSE_CONTENT_CLASS, null);
		} else {
			logger.warn("D51CTOHtmlParser Conf is null");
		}
	}

	@Override
	public ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, DocumentFragment doc) {
	//	logger.info("51CTO Filter : " + content.getUrl() + " ContentType = " + new String(content.getContentType()));

		if (baseUrl == null || contentClass == null || storageUrl == null|| baseUrl.equals("") || contentClass.equals("")) {
			logger.info("D51CTOHtmlParser url is not set or class is not set, this will skip all url");
			return parseResult;
		}
		
		if (content.getUrl().startsWith(baseUrl)) {
			
			String url = content.getUrl();
			String title = parseResult.get(url).getData().getTitle();
			String mainContent = getByClass(contentClass, doc);
			
			if (mainContent != null && !mainContent.equals("")) {
				List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("url", url));
				params.add(new BasicNameValuePair("title", title));
				params.add(new BasicNameValuePair("content", mainContent));
				try {
					SimpleHttpClient.getInstance().post(storageUrl, params);
				} catch (Exception e) {
					logger.warn("Cannot storage web page " + url, e);
				}
			}
		}
		return parseResult;
	}

	public String getByClass(String clazz, Node node) {
		if (node != null) {
			if (node.hasAttributes()) {
				Node nodeAttr = node.getAttributes().getNamedItem("class");
				if (nodeAttr != null && nodeAttr.getNodeValue().contains(clazz)) {
					//System.out.println(nodeAttr.getNodeValue() + ": " + node.getTextContent());
					
					StringBuffer sb = new StringBuffer();
					domUtils.getText(sb, node);
					return sb.toString();
				}
			}
			
			NodeList childs = node.getChildNodes();
			for (int i = childs.getLength(); i >= 0; i--) {
				String content = getByClass(clazz, childs.item(i));
				if (content != null) {
					return content;
				}
			}
		}
		return null;
	}
	

	
	public static void main(String [] args) throws SAXException, IOException {
		String html = new String("<html><head><title> </title>"
	               + "</head><body> "
	               + "<a href=\"/\"> separate this "
	               + "<a href=\"ok\"> from this"
	               + "</a></a>"
	               + "<div class='content blue'>Database<p>f</p></div>"
	               + "</body></html>");
		
		Configuration conf = NutchConfiguration.create();
		
	//	FileSystem fs = FileSystem.getLocal(conf);
		//Path path = new Path("content/data");
		//fs.create(path);
		
		DOMFragmentParser parser= new DOMFragmentParser();
		DocumentFragment node = new HTMLDocumentImpl().createDocumentFragment();
		parser.parse(new InputSource(new ByteArrayInputStream(html.getBytes())), node);
		
		ContentHtmlParser htmlParser = new ContentHtmlParser();
		htmlParser.setConf(conf);
		htmlParser.getByClass("content", node);
	}
}
