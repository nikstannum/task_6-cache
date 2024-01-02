package ru.clevertec.service.util.converter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * PDF converter. Implements {@link ru.clevertec.service.util.converter.Converter}
 */
@RequiredArgsConstructor
public class PDFConverter implements Converter {

    private final int fontSize;
    private final String templatePath;
    private final String fontPath;

    /**
     * Converts content into a PDF document and redirects the data to the specified output stream.
     * Closing the output stream is the responsibility of the person using the method.
     *
     * @param content      content that is written in a PDF document
     * @param outputStream output stream where the PDF document is redirected
     */
    @Override
    public void convert(String content, OutputStream outputStream) {
        File templateFile = getFile(templatePath);
        PDDocument background = null;
        try {
            background = Loader.loadPDF(templateFile);
            PDPage page = background.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(background, page, AppendMode.APPEND, true);
            contentStream.beginText();
            File fontFile = getFile(fontPath);
            PDType0Font font = PDType0Font.load(background, fontFile);
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(50, 700);
            contentStream.setLeading(14.5f);
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.replaceAll("\r", "").replaceAll("\t", "");
                contentStream.showText(line);
                contentStream.newLine();
            }
            contentStream.endText();
            contentStream.close();
            background.save(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(Objects.requireNonNull(background));
        }
    }

    private void closeResources(PDDocument background) {
        try {
            background.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String pathStr) {
        Path path = Path.of(pathStr);
        return new File(path.toUri());
    }
}
