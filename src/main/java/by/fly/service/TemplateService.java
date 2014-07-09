package by.fly.service;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class TemplateService {

    @Autowired
    private VelocityEngine velocityEngine;

    public String mergeTemplate(String templateName, Context context) {
        final Template template = velocityEngine.getTemplate(templateName, "UTF-8");
        final StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

}
