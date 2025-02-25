package cs107;

import java.sql.Array;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){
        assert image != null;
        assert image.channels() == QOISpecification.RGB ||image.channels() == QOISpecification.RGBA;
        assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;

        byte[] magicNumber = QOISpecification.QOI_MAGIC;
        int[][] imageArray = image.data();

        int width = imageArray[0].length;
        int height = imageArray.length;

        byte[] arrayWidth = ArrayUtils.fromInt(width);
        byte[] arrayHeight = ArrayUtils.fromInt(height);

        byte[] channelsNumber = ArrayUtils.wrap(image.channels());
        byte[] colorSpace = ArrayUtils.wrap(image.color_space());


        return ArrayUtils.concat(magicNumber, arrayWidth, arrayHeight, channelsNumber, colorSpace);
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){
        assert pixel.length == 4;

        byte red = pixel[0];
        byte green = pixel[1];
        byte blue = pixel[2];
        byte tag = QOISpecification.QOI_OP_RGB_TAG;

        return ArrayUtils.concat(tag, red, green, blue);
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){
        assert pixel.length == 4;

        byte[] qoiRGB = qoiOpRGB(pixel);
        qoiRGB[0] = QOISpecification.QOI_OP_RGBA_TAG;

        byte alpha = pixel[3];
        byte[] arrayAlpha = ArrayUtils.wrap(alpha);


        return ArrayUtils.concat(qoiRGB, arrayAlpha);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){
        assert index >= 0 && index <= 63;

        return ArrayUtils.wrap((byte)(QOISpecification.QOI_OP_INDEX_TAG | index));
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){
        assert diff != null;
        assert diff.length == 3;

        for(int i = 0; i < diff.length; ++i) {
            assert -3 < diff[i] && diff[i] < 2;
            diff[i] = (byte)(diff[i] + 2);
        }

        byte dr = diff[0];
        byte dg = diff[1];
        byte db = diff[2];

        byte result = (byte)(QOISpecification.QOI_OP_DIFF_TAG | (dr << 4) | (dg << 2) | (db));

        return ArrayUtils.wrap(result);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff != null;
        assert diff.length == 3;

        byte dr = diff[0];
        byte dg = diff[1];
        byte db = diff[2];

        byte drg = (byte)(dr - dg);
        byte dbg = (byte)(db - dg);

        assert -33 < dg && dg < 32;
        assert -9 < drg && drg < 8;
        assert -9 < dbg && dbg < 8;

        dg = (byte)(dg + 32);
        drg = (byte)(drg + 8);
        dbg = (byte)(dbg + 8);

        byte[] result = new byte[2];

        result[0] = (byte)(QOISpecification.QOI_OP_LUMA_TAG | dg);
        result[1] = (byte)((drg << 4) | dbg);

        return result;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert count >= 1 && count <= 62;
        count = (byte)(count - 1);

        return ArrayUtils.wrap((byte)(QOISpecification.QOI_OP_RUN_TAG | count) );
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        assert image != null;

        for(byte[] b : image) {
            assert b != null;
            assert b.length == 4;
        }

        byte[] prev = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte [64][4];
        byte count = 0;

        ArrayList<byte[]> res = new ArrayList<byte[]>();

        for(int i = 0; i < image.length; ++i) {
            if(ArrayUtils.equals(prev, image[i])) {
                ++count;
                if(count == 62 || i == image.length - 1) {
                    byte[] run = qoiOpRun(count);
                    res.add(run);
                    count = 0;
                }
            } else {
                if(count > 0) {
                    byte[] run = qoiOpRun(count);
                    res.add(run);
                }
                count = 0;

                byte hashIndex = QOISpecification.hash(image[i]);
                if(ArrayUtils.equals(hashTable[hashIndex], image[i])) {
                    byte[] index = qoiOpIndex(hashIndex);
                    res.add(index);
                } else {
                    hashTable[hashIndex] = image[i];

                    if(image[i][3] == prev[3]) {
                        byte dr = (byte)(image[i][0] - prev[0]);
                        byte dg = (byte)(image[i][1] - prev[1]);
                        byte db = (byte)(image[i][2] - prev[2]);

                        boolean flag = true;
                        byte[] diff = {dr, dg, db};
                        for (byte b : diff) {
                            if (!(-3 < b && b < 2)) {
                                flag = false;
                                break;
                            }
                        }

                        if(flag) {
                            byte[] difference = qoiOpDiff(diff);
                            res.add(difference);
                        } else {
                            byte drg = (byte)(dr - dg);
                            byte dbg = (byte)(db - dg);

                            if((-33 < dg && dg < 32) && (-9 < drg && drg < 8) && (-9 < dbg && dbg < 8)) {
                                byte[] luma = qoiOpLuma(diff);
                                res.add(luma);
                            } else {
                                byte[] rgb = qoiOpRGB(image[i]);
                                res.add(rgb);
                            }
                        }
                    } else {
                        byte[] rgba = qoiOpRGBA(image[i]);
                        res.add(rgba);
                    }
                }
            }

            prev = image[i];
        }

        byte[][] ans = res.toArray(new byte[0][]);

        return ArrayUtils.concat(ans);
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image != null;

        byte[] header = qoiHeader(image);

        int[][] intDataImage = image.data();
        byte [][] byteDataImage = ArrayUtils.imageToChannels(intDataImage);
        byte[] data = encodeData(byteDataImage);

        byte[] signature = QOISpecification.QOI_EOF;

        return ArrayUtils.concat(header, data, signature);
    }

}