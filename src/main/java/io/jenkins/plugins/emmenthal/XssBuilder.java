/*
 * The MIT License
 *
 * Copyright (c) 2021, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.emmenthal;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;
import java.io.PrintWriter;

public class XssBuilder extends Builder {

    private String name;
    private String identity;
    private String shortDescription;
    private String longDescription;
    private String style;

    private String pattern;
    private String patternTimes;


    @DataBoundConstructor
    public XssBuilder() {
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return Util.fixNull(identity).replaceAll(" ", "");
    }

    @DataBoundSetter
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    @DataBoundSetter
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    @DataBoundSetter
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    @DataBoundSetter
    public void setPatternTimes(String patternTimes) {
        this.patternTimes = patternTimes;
    }

    public String getPatternTimes() {
        return patternTimes;
    }

    @DataBoundSetter
    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("xss")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[emmenthal] XSS Builder";
        }

        @RequirePOST
        @Restricted(DoNotUse.class)
        public FormValidation doCheckName(@QueryParameter String value) {
            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.warning("Name should not be empty");
            }

            return FormValidation.ok();
        }

        @RequirePOST
        @Restricted(DoNotUse.class)
        public FormValidation doCheckShortDescription(@QueryParameter String value) {
            Jenkins.get().checkPermission(Job.CONFIGURE);
            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }

            if (value.length() > 50) {
                return FormValidation.errorWithMarkup("The short description is too long: " + value + ". Please use the long description for that.");
            }

            return FormValidation.ok();
        }

        @RequirePOST
        @Restricted(DoNotUse.class)
        public FormValidation doCheckLongDescription(@QueryParameter String value) {
            Jenkins.get().checkPermission(Job.CONFIGURE);
            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }

            if (value.length() > 200) {
                return FormValidation.errorWithMarkup("The description is too long: <div title=\"" + value + "\">" + value.substring(0, 10) + " [...] </div>");
            }

            return FormValidation.ok();
        }
        
        @Restricted(DoNotUse.class)
        public HttpResponse doPreviewDescription(@QueryParameter String longDescription, @QueryParameter String style) {
            Jenkins.get().checkPermission(Job.CONFIGURE);
            String html = "<div style='" + style + "'>" + longDescription + "</div>";
            return (req, rsp, node) -> {
                rsp.setContentType("text/html;charset=UTF-8");
                rsp.setStatus(200);
                PrintWriter pw = rsp.getWriter();
                pw.print(html);
                pw.flush();
            };
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
