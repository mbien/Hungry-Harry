/*
 * Created on Saturday, May 15 2010 17:07
 */
package com.jogamp.hungryharry;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Hungry Harrie's configuration.
 * @author Michael Bien
 */
@XmlType(name = "")
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    public final List<Feed> feed;
    
    @XmlElement(required = true)
    public final List<Template> template;

    @XmlElement(required = true)
    public final Planet planet;

    public Config() {
        feed = null;
        template = null;
        planet = null;
    }

    @XmlType
    public static class Feed {

        @XmlAttribute
        public final String name;
        @XmlAttribute
        public final String url;

        public Feed() {
            name = null;
            url = null;
        }
    }

    @XmlType
    public static class Template {

        @XmlAttribute
        public final String keyword;

        @XmlAttribute(name="idpattern")
        public final String idpattern;

        @XmlValue
        public final String text;

        public Template() {
            keyword = null;
            text = null;
            idpattern = null;
        }
    }

    @XmlType
    public static class Planet {

        @XmlAttribute
        public final String title;

        @XmlAttribute
        public final String description;

        @XmlAttribute
        public final String author;

        @XmlAttribute
        public final String link;

        @XmlElement(name="feed")
        public final List<String> feeds;

        @XmlElement(name="template")
        public final String templatePath;

        public Planet() {
            title = null;
            description = null;
            author = null;
            link = null;
            feeds = null;
            templatePath = null;
        }
    }
}
