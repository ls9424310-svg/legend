package com.hackathon.manager.util;

import java.awt.image.BufferedImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {
    
    public static BufferedImage generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Set color: Black if bit is set, White otherwise
                    int color = bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF;
                    image.setRGB(x, y, color);
                }
            }
            return image;
        } catch (Exception e) {
            System.err.println("Error generating QR Code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
