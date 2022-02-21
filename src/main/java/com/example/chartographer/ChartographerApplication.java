package com.example.chartographer;

import com.example.chartographer.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class ChartographerApplication {

    public static void main(String[] args) throws IOException {

        Config.pathToContent = args.length > 0 ? args[0] : System.getProperty("user.dir") + File.separator + "content";

        if (!Files.isDirectory(Path.of(Config.pathToContent))) {
            Files.createDirectory(Path.of(Config.pathToContent));
        }

        SpringApplication.run(ChartographerApplication.class, args);
    }
}
