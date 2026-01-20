package com.meteor.common.utils.image;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  图片裁剪工具类
 *
 * @author Programmer
 * @date 2026-01-20 13:29
 */
public class ImageCropUtil {

    public static InputStream cropToSquare(InputStream inputStream, String format) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BizException(CommonErrorCode.IMAGE_PROCESS_ERROR);
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int side = Math.min(width, height);

            int x = (width - side) / 2;
            int y = (height - side) / 2;

            BufferedImage cropped = image.getSubimage(x, y, side, side);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(cropped, format, os);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.IMAGE_PROCESS_ERROR);
        }
    }

}
