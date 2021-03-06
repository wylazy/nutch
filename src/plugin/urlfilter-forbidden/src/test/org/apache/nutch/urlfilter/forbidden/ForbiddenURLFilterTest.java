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

import java.io.IOException;
import java.io.StringReader;

import org.apache.nutch.urlfilter.forbidden.ForbiddenURLFilter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * JUnit test for <code>SuffixURLFilter</code>.
 *
 * @author Andrzej Bialecki
 */
public class ForbiddenURLFilterTest extends TestCase {
  private static final String suffixes =
    "# this is a comment\n" +
    "\n" +
    "2002\n" +
    "2005";
  
  private static final String[] urls = new String[] {
    "http://database.51cto.com/20090405",
    "http://www.example.com/test.js?foo=2005&baz=bar#12333",
    "http://www.google.com/2002"
  };
  
  private static final boolean[] accept = new boolean[] {
	  true,
	  false,
	  false
  };
  
  private ForbiddenURLFilter filter = null;
  
  public ForbiddenURLFilterTest(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(ForbiddenURLFilter.class);
  }
  
  public static void main(String[] args) {
    TestRunner.run(suite());
  }
  
  public void setUp() throws IOException {
    filter = new ForbiddenURLFilter(new StringReader(suffixes));
  }
  
  public void testModeAccept() {
    for (int i = 0; i < urls.length; i++) {
      assertTrue(accept[i] == (urls[i].equals(filter.filter(urls[i]))));
    }
  }

}
