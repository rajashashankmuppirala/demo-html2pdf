package com.shashank.demohtml2pdf.controller;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlCanvas;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLCanvasElement;
import com.itextpdf.html2pdf.HtmlConverter;
import com.shashank.demohtml2pdf.model.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.xhtmlrenderer.pdf.ITextRenderer;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@RestController
public class PdfController {

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    ServletContext servletContext;

    @GetMapping("/download")
    public ResponseEntity<?> downloadTransformedPdf() throws IOException {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(getClass().getClassLoader().getResourceAsStream("test.html"),
                    outputStream);

            byte[] bytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);
        } finally {
            if(outputStream != null){
                outputStream.close();
                outputStream.flush();
            }

        }
    }


    @GetMapping(value = "/pdfView", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewTransformedPdf() throws IOException {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(getClass().getClassLoader().getResourceAsStream("test.html"),
                    outputStream);

            byte[] bytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .body(bytes);
        }finally {
            if(outputStream != null){
                outputStream.close();
                outputStream.flush();
            }
        }
    }

    @GetMapping(value = "/pdf/table/itext", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewTransformedTablePdf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream outputStream = null;

        try {
            outputStream = new ByteArrayOutputStream();
            List<Row> rows = new ArrayList<>();
            rows.add(Row.builder().col1("col11").col2("col12").col3("col13").col4("col14").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col23").col4("col24").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col33").col4("col34").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col43").col4("col44").build());

            WebContext context = new WebContext(request, response, servletContext);
            String chart = getChart(context);

            context.setVariable("rows", rows);
            context.setVariable("chart",chart );
            String rowsHtml = templateEngine.process("test-table", context);

            HtmlConverter.convertToPdf(rowsHtml,
                    outputStream);

            byte[] bytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .body(bytes);

        }finally {
            if(outputStream != null){
                outputStream.close();
                outputStream.flush();
            }
        }
    }

    @GetMapping(value = "/pdf/table/flyingSaucer", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewTransformedTablePdfFlyingSaucer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream outputStream = null;

        try {
            outputStream = new ByteArrayOutputStream();
            List<Row> rows = new ArrayList<>();
            rows.add(Row.builder().col1("col11").col2("col12").col3("col13").col4("col14").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col23").col4("col24").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col33").col4("col34").build());
            rows.add(Row.builder().col1("col21").col2("col22").col3("col43").col4("col44").build());

            WebContext context = new WebContext(request, response, servletContext);
            String chart = getChart(context);

            context.setVariable("rows", rows);
            context.setVariable("chart",chart );
            String rowsHtml = templateEngine.process("test-table", context);

            ITextRenderer pdfRenderer = new ITextRenderer();
            pdfRenderer.setDocumentFromString(createXHtml(rowsHtml));
            pdfRenderer.layout();
            pdfRenderer.createPDF(outputStream);

            byte[] bytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .body(bytes);

        }finally {
            if(outputStream != null){
                outputStream.close();
                outputStream.flush();
            }
        }
    }

    private String getChart(WebContext context ) throws IOException {
        String chartHtml = templateEngine.process("test-chartsjs", context);
        String chartPng = null;
        WebClient webClient = null;
        try {
            webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage html = webClient.loadHtmlCodeIntoCurrentWindow(chartHtml);
            webClient.waitForBackgroundJavaScript(20000);

            Window window = html.getPage().getEnclosingWindow().getScriptableObject();
            int i = 0;
            do {
                i = window.animateAnimationsFrames();
            } while (i > 0);

            for (DomElement elem : html.getPage().getElementsByTagName("canvas")) {
                HTMLCanvasElement canvas = (HTMLCanvasElement) ((HtmlCanvas) elem).getScriptableObject();
                chartPng = canvas.toDataURL("image/png");
                break;
            }
            return chartPng;
        }finally {
            if(null!= webClient){
                webClient.close();
            }

        }
    }

    private String createXHtml(String inputHTML) {
        Document document = Jsoup.parse(inputHTML);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.html();
    }
}
