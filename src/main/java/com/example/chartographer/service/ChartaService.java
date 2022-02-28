package com.example.chartographer.service;

import com.example.chartographer.config.Config;
import com.example.chartographer.exception.BadRequestException;
import com.example.chartographer.exception.NotFoundException;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

@Service
public class ChartaService {

    @SneakyThrows
    public String create(int width, int height) {
        String id = UUID.randomUUID().toString();
        try (FileOutputStream fos = new FileOutputStream(Config.pathToContent + File.separator + id, true)) {
            fos.write(getBMPHeader(width, height));
            for (int i = 0; i < height; i++) {
                byte[] row = new byte[width * 3 + width % 4];
                fos.write(row);
            }
        }
        return id;
    }

    public void save(String id, int x, int y, int width, int height, InputStream stream) throws IOException {
        String tmpFile = Config.pathToContent + File.separator + UUID.randomUUID();
        try {
            Files.copy(stream, Path.of(tmpFile));
            try (RandomAccessFile raf = new RandomAccessFile(Config.pathToContent + File.separator + id, "rw");
                 RandomAccessFile rafTmp = new RandomAccessFile(tmpFile, "r")) {

                while (raf.getChannel().tryLock() == null) {
                    waitUnlock(500);
                    checkId(id);
                }

                raf.seek(18);
                int chartaW = readIntLittleEndian(raf);
                raf.seek(22);
                int chartaH = readIntLittleEndian(raf);

                checkXY(x, y, chartaW, chartaH);

                rafTmp.seek(18);
                int fragmentW = readIntLittleEndian(rafTmp);
                rafTmp.seek(22);
                int fragmentH = readIntLittleEndian(rafTmp);

                width = Math.min(width, fragmentW);
                height = Math.min(height, fragmentH);

                int rowBegin = Math.max(chartaH - y - height, 0);
                int rows = y + height > chartaH ? chartaH - y : height;

                for (int i = rowBegin; i < rowBegin + rows; i++) {
                    raf.seek(54 + (chartaW * 3L + chartaW % 4) * i + x * 3L);
                    rafTmp.seek(54 + (chartaW * 3L + chartaW % 4) * (Math.max(height + y - chartaH, 0)));
                    int pixels = x + width > chartaW ? chartaW - x : width;
                    byte[] row = new byte[3 * pixels];
                    rafTmp.read(row);
                    raf.write(row);
                }
            }
        } finally {
            delete(tmpFile);
        }
    }

    public ByteArrayResource get(String id, int x, int y, int fragmentW, int fragmentH) throws IOException {
        byte[] out = Arrays.copyOf(getBMPHeader(fragmentW, fragmentH), 54 + (fragmentW * 3 + fragmentW % 4) * fragmentH);
        try (RandomAccessFile raf = new RandomAccessFile(Config.pathToContent + File.separator + id, "r")) {

            while (raf.getChannel().tryLock(0, Integer.MAX_VALUE, true) == null) {
                waitUnlock(500);
                checkId(id);
            }

            raf.seek(18);
            int chartaW = readIntLittleEndian(raf);
            raf.seek(22);
            int chartaH = readIntLittleEndian(raf);

            checkXY(x, y, chartaW, chartaH);

            int rowBeginRead = y + fragmentH > chartaH ? 0 : chartaH - fragmentH - y;
            int rows = y + fragmentH > chartaH ? chartaH - y : fragmentH;
            int rowForWrite = y + fragmentH > chartaH ? y + fragmentH - chartaH : 0;

            for (int i = rowBeginRead; i < rowBeginRead + rows; i++) {
                raf.seek(54 + (chartaW * 3L + chartaW % 4) * i + x * 3L);
                int pixels = x + fragmentW > chartaW ? chartaW - x : fragmentW;
                int index = 54 + (fragmentW * 3 + fragmentW % 4) * rowForWrite;
                raf.read(out, index, 3 * pixels);
                rowForWrite++;
            }
        }
        return new ByteArrayResource(out);
    }

    public void delete(String fileName) throws IOException {
        while (!Files.deleteIfExists(Path.of(fileName))) {
            waitUnlock(500);
            if (Files.notExists(Path.of(fileName))) {
                return;
            }
        }
    }

    public void checkId(String id) {
        Path path = Path.of(Config.pathToContent, id);
        if (Files.notExists(path) || !Files.isRegularFile(path)) {
            throw new NotFoundException(id + " not found");
        }
    }

    private byte[] getBMPHeader(int width, int height) {
        int size = width * height * 3 + height * (width % 4);
        ByteBuffer header = ByteBuffer.allocate(54);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.putShort((short) 0x4D42);
        header.putInt(size + 54);
        header.putShort((short) 0);
        header.putShort((short) 0);
        header.putInt(54);
        header.putInt(40);
        header.putInt(width);
        header.putInt(height);
        header.putShort((short) 1);
        header.putShort((short) 24);
        header.putInt(0);
        header.putInt(size);
        header.putInt(0);
        header.putInt(0);
        header.putInt(0);
        header.putInt(0);
        header.flip();
        return header.array();
    }

    private void checkXY(int x, int y, int width, int height) {
        if (x >= width || y >= height) {
            throw new BadRequestException("X/Y out of range");
        }
    }

    private int readIntLittleEndian(RandomAccessFile raf) throws IOException {
        int a = raf.readByte() & 0xFF;
        int b = raf.readByte() & 0xFF;
        int c = raf.readByte() & 0xFF;
        int d = raf.readByte() & 0xFF;
        return (d << 24) | (c << 16) | (b << 8) | a;
    }

    private void waitUnlock(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
