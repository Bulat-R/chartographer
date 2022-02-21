package com.example.chartographer;

import com.example.chartographer.config.Config;
import com.example.chartographer.exception.BadRequestException;
import com.example.chartographer.exception.NotFoundException;
import com.example.chartographer.service.ChartaService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChartaServiceTest {

    @Autowired
    private ChartaService service;

    private static String testCharta = "";

    @BeforeAll
    public static void testContentPath() {
        Config.pathToContent = Paths.get("src", "test", "resources").toString();
    }

    @Test
    @Order(1)
    public void create() throws IOException {
        testCharta = service.create(122, 165);
        String actual = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, testCharta)));
        String expected = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, "122x165_black.bmp")));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    public void save_white_to_charta() throws IOException {
        service.save(testCharta, 12, 26, 1500, 1500,
                Files.newInputStream(Path.of(Config.pathToContent, "150x89_white.bmp")));
        String actual = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, testCharta)));
        String expected = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, "black_white.bmp")));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    public void save_red_1pxl_line_to_charta() throws IOException {
        service.save(testCharta, 121, 26, 1, 1500,
                Files.newInputStream(Path.of(Config.pathToContent, "150x89_red.bmp")));
        String actual = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, testCharta)));
        String expected = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, "plus_red_1pxl.bmp")));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(4)
    public void save_check_wrong_XY() throws IOException {
        Assertions.assertThrows(BadRequestException.class, () -> service.save(testCharta, 122, 26, 1, 1500,
                Files.newInputStream(Path.of(Config.pathToContent, "150x89_red.bmp"))));

        Assertions.assertThrows(BadRequestException.class, () -> service.save(testCharta, 0, 165, 1, 1500,
                Files.newInputStream(Path.of(Config.pathToContent, "150x89_red.bmp"))));
    }

    @Test
    @Order(5)
    public void get() throws IOException {
        ByteArrayResource fragment2x2 = service.get(testCharta, 11, 25, 2, 2);
        String actual = DigestUtils.md5Hex(fragment2x2.getByteArray());
        String expected = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, "fragment_2x2.bmp")));
        Assertions.assertEquals(expected, actual);

        ByteArrayResource fragment3x2 = service.get(testCharta, 120, 114, 3, 2);
        actual = DigestUtils.md5Hex(fragment3x2.getByteArray());
        expected = DigestUtils.md5Hex(Files.readAllBytes(Path.of(Config.pathToContent, "fragment_3x2.bmp")));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(6)
    public void get_check_wrong_XY() {
        Assertions.assertThrows(BadRequestException.class, () -> service.get(testCharta, 122, 26, 1, 1500));

        Assertions.assertThrows(BadRequestException.class, () -> service.get(testCharta, 0, 165, 1, 1500));
    }

    @Test
    @Order(7)
    public void checkId_wrong_id() {
        Assertions.assertThrows(NotFoundException.class, () -> service.checkId("blablabla"));
    }

    @Test
    @Order(8)
    public void checkId() {
        Assertions.assertDoesNotThrow(() -> service.checkId("fragment_3x2.bmp"));

        Assertions.assertDoesNotThrow(() -> service.checkId("122x165_black.bmp"));
    }

    @Test
    @Order(9)
    public void delete() throws IOException {
        Files.deleteIfExists(Path.of(Config.pathToContent, testCharta));
        Assertions.assertTrue(Files.notExists(Path.of(Config.pathToContent, testCharta)));
    }
}
