package ru.clevertec.service.util.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.factory.TestBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

class PDFConverterTest {

    private PDFConverter converter;

    @Test
    void checkConvertWithByteArrayOutputStreamShouldBeEquals() {
        // given
        initConverter(true);
        ByteArrayInputStream expected = new ByteArrayInputStream(getContent("data"));
        String content = new String(getContent("content"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // when
        converter.convert(content, os);

        // then
        assertThat(os.toByteArray()).isEqualTo(expected.readAllBytes());
    }

    @Test
    void checkConvertWithFileOutputStreamShouldBeEquals() throws IOException {
        // given
        initConverter(true);
        String pathStr = (String) getProperty("data");
        FileInputStream fis = new FileInputStream(getFile(pathStr));
        byte[] expectedBytes = fis.readAllBytes();
        String expected = new String(expectedBytes);

        String content = new String(getContent("content"));
        File tempFile = Files.createTempFile("result", "pdf").toFile();
        FileOutputStream fos = new FileOutputStream(tempFile);

        // when
        converter.convert(content, fos);
        String actual = new String(Files.readAllBytes(tempFile.toPath()));

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkConvertWithFileOutputStreamShouldThrowRuntimeException() throws IOException {
        // given
        initConverter(false);

        String content = new String(getContent("content"));
        File tempFile = Files.createTempFile("result", "pdf").toFile();
        FileOutputStream fos = new FileOutputStream(tempFile);

        // then
        Assertions.assertThrows(RuntimeException.class, () -> converter.convert(content, fos));
    }

    private void initConverter(boolean isValid) {
        int fontSize = (int) getProperty("fontSize");
        String fontPath = (String) getProperty("fontPath");
        String templatePath;
        if (isValid) {
            templatePath = (String) getProperty("templatePath");
        } else {
            templatePath = "some/invalid/path";
        }
        converter = new PDFConverter(fontSize, templatePath, fontPath);
    }

    private File getFile(String pathStr) {
        Path path = Path.of(pathStr);
        return new File(path.toUri());
    }

    private Object getProperty(String propKey) {
        TestBeanFactory factory = TestBeanFactory.INSTANCE;
        ConfigManager manager = factory.getBean(ConfigManager.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> pdfProps = (Map<String, Object>) manager.getProperty("pdf");
        return pdfProps.get(propKey);

    }

    private byte[] getContent(String prop) {
        String pathStr = (String) getProperty(prop);
        File file = getFile(pathStr);
        byte[] bytes;
        try (FileInputStream is = new FileInputStream(file)) {
            bytes = is.readAllBytes();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
