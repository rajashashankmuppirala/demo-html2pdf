package com.shashank.demohtml2pdf.controller;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
public class PdfController {


    @GetMapping("/download")
    public ResponseEntity<?> downloadTransformedPdf() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(getClass().getClassLoader().getResourceAsStream("test.html"),
                outputStream);

        byte[] bytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }


    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> viewTransformedPdf() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(getClass().getClassLoader().getResourceAsStream("test.html"),
                outputStream);

        byte[] bytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .body(bytes);
    }
}
