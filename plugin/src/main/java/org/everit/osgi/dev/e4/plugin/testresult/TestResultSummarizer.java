/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.testresult;

/**
 * Summarizer of test results.
 */
public class TestResultSummarizer {

  public int errors = 0;

  public int failures = 0;

  public int ignored = 0;

  public String projectName;

  public int started = 0;

  public int tests = 0;

  public StringBuilder xmlBody = new StringBuilder();

}
