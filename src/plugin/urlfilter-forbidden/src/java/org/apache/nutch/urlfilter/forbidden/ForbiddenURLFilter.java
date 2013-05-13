/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.urlfilter.forbidden;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.net.*;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.SuffixStringMatcher;

import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * 
 * @author wylazy
 * 
 */
public class ForbiddenURLFilter implements URLFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ForbiddenURLFilterTest.class);

	private List<String> forbids;
	private Configuration conf;
	private String attributeFile;
	
	public ForbiddenURLFilter() throws IOException {

	}

	public ForbiddenURLFilter(Reader reader) throws IOException {
		readConfiguration(reader);
	}

	public String filter(String url) {
		if (url == null)
			return null;

		for (String forbid : forbids) {
			if (url.toLowerCase().contains(forbid)) {
				LOG.info("Reject " + url);
				return null;
			}
		}

		
		return url;
	}

	public void readConfiguration(Reader reader) throws IOException {

		forbids = new LinkedList<String>();
		
		// handle missing config file
		if (reader == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Missing urlfilter.forbidden.file, all URLs will be rejected!");
			}
			return;
		}

		BufferedReader in = new BufferedReader(reader);
		
		String line;

		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0 || line.charAt(0) == '#')
				continue;
			forbids.add(line.toLowerCase());
		}

	}

	public static void main(String args[]) throws IOException {

		ForbiddenURLFilter filter;
		if (args.length >= 1)
			filter = new ForbiddenURLFilter(new FileReader(args[0]));
		else {
			filter = new ForbiddenURLFilter();
			filter.setConf(NutchConfiguration.create());
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = in.readLine()) != null) {
			String out = filter.filter(line);
			if (out != null) {
				System.out.println("ACCEPTED " + out);
			} else {
				System.out.println("REJECTED " + out);
			}
		}
	}

	public void setConf(Configuration conf) {
		this.conf = conf;

		String pluginName = "urlfilter-forbidden";
		Extension[] extensions = PluginRepository.get(conf).getExtensionPoint(URLFilter.class.getName()).getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = extensions[i];
			if (extension.getDescriptor().getPluginId().equals(pluginName)) {
				attributeFile = extension.getAttribute("file");
				break;
			}
		}
		if (attributeFile != null && attributeFile.trim().equals(""))
			attributeFile = null;
		if (attributeFile != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Attribute \"file\" is defined for plugin "
						+ pluginName + " as " + attributeFile);
			}
		} else {
			// if (LOG.isWarnEnabled()) {
			// LOG.warn("Attribute \"file\" is not defined in plugin.xml for
			// plugin "+pluginName);
			// }
		}

		String file = conf.get("urlfilter.forbidden.file");
		
		// attribute "file" takes precedence if defined
		if (attributeFile != null)
			file = attributeFile;
		Reader reader = conf.getConfResourceAsReader(file);

		try {
			readConfiguration(reader);
		} catch (IOException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage());
			}
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Configuration getConf() {
		return this.conf;
	}

}
