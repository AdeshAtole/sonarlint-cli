/*
 * SonarLint CLI
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarlint.cli.report;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sonarsource.sonarlint.core.IssueListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ResourceReportTest {
  private final static Path RESOURCE = Paths.get("resource");
  private ResourceReport resourceReport;

  @Before
  public void setUp() {
    resourceReport = new ResourceReport(RESOURCE);
  }

  @Test
  public void testIssuesLines() {
    IssueListener.Issue i1 = createTestIssue("file1", "rule1", "MAJOR", 10);
    IssueListener.Issue i2 = createTestIssue("file1", "rule1", "MAJOR", 11);
    resourceReport.addIssue(i1);
    resourceReport.addIssue(i2);

    assertThat(resourceReport.getIssues()).containsOnly(i1, i2);
    assertThat(resourceReport.getIssuesAtLine(10)).containsExactly(i1);
    assertThat(resourceReport.getIssuesPerLine()).containsOnly(
      entry(i1.getStartLine(), Collections.singletonList(i1)),
      entry(i2.getStartLine(), Collections.singletonList(i2)));
    assertThat(resourceReport.getName()).isEqualTo("resource");
    assertThat(resourceReport.getPath()).isEqualTo(RESOURCE);
  }

  @Test
  public void testCategoryReport() {
    IssueListener.Issue i1 = createTestIssue("file1", "rule1", "MAJOR", 10);
    IssueListener.Issue i2 = createTestIssue("file1", "rule1", "MINOR", 11);
    IssueListener.Issue i3 = createTestIssue("file1", "rule2", "MINOR", 11);
    IssueListener.Issue i4 = createTestIssue("file1", "rule2", "MINOR", 12);
    resourceReport.addIssue(i1);
    resourceReport.addIssue(i2);
    resourceReport.addIssue(i3);
    resourceReport.addIssue(i4);

    List<Severity> l = new LinkedList<>();
    l.add(Severity.MINOR);
    l.add(Severity.MAJOR);
    Collections.sort(l);

    List<CategoryReport> categoryReports = resourceReport.getCategoryReports();
    assertThat(categoryReports).hasSize(3);

    // sort first by severity, then by rule key
    assertThat(categoryReports).extracting("ruleKey").containsExactly("rule1", "rule1", "rule2");
    assertThat(categoryReports).extracting("severity").containsExactly(Severity.MAJOR, Severity.MINOR, Severity.MINOR);

    // grouping
    assertThat(categoryReports.get(0).getTotal().getCountInCurrentAnalysis()).isEqualTo(1);
    assertThat(categoryReports.get(1).getTotal().getCountInCurrentAnalysis()).isEqualTo(1);
    assertThat(categoryReports.get(2).getTotal().getCountInCurrentAnalysis()).isEqualTo(2);
  }

  private static IssueListener.Issue createTestIssue(String filePath, String ruleKey, String severity, int line) {
    IssueListener.Issue issue = new IssueListener.Issue();
    issue.setStartLine(line);
    issue.setFilePath(Paths.get(filePath));
    issue.setRuleKey(ruleKey);
    issue.setSeverity(severity);
    return issue;
  }
}