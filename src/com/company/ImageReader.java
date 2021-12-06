package com.company;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageReader {

    private static ImageReader singleton;

    public static ImageReader getInstance() {
        if (singleton == null) {
            singleton = new ImageReader();
        }
        return singleton;
    }

    private final int imageWidth = 352;
    private final int imageHeight = 288;
    private final int pixel_num;

    private ImageReader() {
        pixel_num = imageWidth * imageHeight;
    }

    ArrayList<File> FolderConfig(String image_folder_path) {
        ArrayList<File> list = new ArrayList<>();

        File selectedFolder = new File(image_folder_path);
        File[] files = selectedFolder.listFiles();
        if (files != null) {
            Arrays.sort(files, (o1, o2) -> {
                if (o1.getName().length() == o2.getName().length()) {
                    return o1.getName().compareTo(o2.getName());
                }
                return o1.getName().length() - o2.getName().length();
            });

            for (File file : files) {
                if (file.getAbsolutePath().endsWith(".rgb")) {
                    list.add(file);
                    // System.out.println(file.getName());
                }
            }
        }

        System.out.println("Total Images Added: " + list.size());
        return list;
    }

    public BufferedImage BImgFromFile(File imageFile) {
        if (!imageFile.getAbsolutePath().endsWith(".rgb")) {
            return null;
        }
        try {
            InputStream fileStream = new FileInputStream(imageFile);
            int len = (int) imageFile.length();
            byte[] bytes = new byte[len];

            int offset = 0;
            int numberRead;
            // Read the file stream
            while (offset < bytes.length && (numberRead=fileStream.read(bytes, offset,len-offset)) >= 0) {
                offset = offset + numberRead;
            }
            fileStream.close();

            // Create new buffered image object
            BufferedImage newImage = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_RGB);
            // Read the image pixels
            int index = 0;
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    byte r = bytes[index];
                    byte g = bytes[index+(pixel_num)];
                    byte b = bytes[index+(2*(pixel_num))];
                    int pixel = 0XFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                    newImage.setRGB(x,y,pixel);
                    index = index + 1;
                }
            }
            return newImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedInputStream BWavFromFile(String filename) {
        File file = new File(filename);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            for (File f : files) {
                if (f.getName().endsWith(".wav")) {
                    filename = f.getAbsolutePath();
                    file = new File(filename);
                    break;
                }
            }
        }
        if (file.isFile()) {
            try {
                InputStream inputStream = new FileInputStream(filename);
                return new BufferedInputStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
