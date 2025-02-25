package cs107;

import static cs107.Helper.Image;
import static cs107.Helper.generateImage;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder(){}

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header){
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;

        byte[] headerMagic = ArrayUtils.extract(header, 0, 4);
        assert ArrayUtils.equals(headerMagic, QOISpecification.QOI_MAGIC);

        assert (header[12] == QOISpecification.RGB) || (header[12] == QOISpecification.RGBA);
        assert (header[13] == QOISpecification.ALL) || (header[13] == QOISpecification.sRGB);

        byte[] tableLargeurImage = ArrayUtils.extract(header, 4, 4);
        byte[] tableHauteurImage = ArrayUtils.extract(header, 8, 4);

        int largeurImage = ArrayUtils.toInt(tableLargeurImage);
        int hauteurImage = ArrayUtils.toInt(tableHauteurImage);
        int nbCanaux = header[12];
        int espCouleur = header[13];

        int[] decodedHeader = new int[]{largeurImage, hauteurImage, nbCanaux, espCouleur};

        return decodedHeader;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param alpha (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx){
        assert buffer != null;
        assert input != null;
        assert position>=0 && position< buffer.length;
        assert idx>=0 && idx<input.length;
        assert  input.length >=4;

        int compteur =0;

        for(int i = 0; i<3; ++i){
            if(idx+i< input.length){
                buffer[position][i] = input[idx+i];
                ++compteur;
            }
        }
        buffer[position][3] = alpha;

        return compteur;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx){
        assert buffer != null;
        assert input != null;
        assert position >= 0 && position < buffer.length;
        assert idx>=0 && idx< input.length;
        assert input.length >= 4;

        int compteur = 0;

        for(int i = 0; i<4; ++i){
            if(idx+i< input.length){
                buffer[position][i] = input[idx+i];
                ++compteur;
            }
        }

        return compteur;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk){
        assert previousPixel != null;
        assert previousPixel.length == 4;
        assert (chunk & 0b01_00_00_00) == QOISpecification.QOI_OP_DIFF_TAG;

        byte dr = (byte) (((0b00_11_00_00 & chunk) >>>4 ) -2);
        byte dg = (byte) (((0b00_00_11_00 & chunk) >>>2 ) - 2);
        byte db = (byte) ((0b00_00_00_11 & chunk) - 2);

        byte[] currentPixel = new byte[4];
        currentPixel[0] = (byte) (previousPixel[0] + dr);
        currentPixel[1] = (byte) (previousPixel[1] + dg);
        currentPixel[2] = (byte) (previousPixel[2] + db);
        currentPixel[3] = previousPixel[3];

        return currentPixel;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data){
        assert previousPixel != null;
        assert data != null;
        assert previousPixel.length == 4;
        assert (byte)(data[0]&0b10_00_00_00) == QOISpecification.QOI_OP_LUMA_TAG;

        byte dg = (byte) ((byte) (0b00_11_11_11 & data[0]) -32);
        byte drg = (byte) ((byte) ((0b11_11_00_00 & data[1]) >>> 4) -8);
        byte dbg = (byte) ((byte) (0b00_00_11_11 & data[1]) -8);

        byte dr = (byte) (drg + dg); // car drg = dr -dg donc dr = dr -dg +dg. Pareil pour la ligne suivante
        byte db = (byte) (dbg + dg);

        byte[] currentPixel = new byte[4];

        currentPixel[0] = (byte)(previousPixel[0] + dr);
        currentPixel[1] = (byte)(previousPixel[1] + dg);
        currentPixel[2] = (byte)(previousPixel[2] + db);
        currentPixel[3] = previousPixel[3];

        return currentPixel;
    }

    /**
     * Store the given pixel in the buffer multiple times
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param pixel (byte[]) - The pixel to store
     * @param chunk (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position){
        assert buffer != null;
        assert position>=0 && position <= buffer.length;
        assert pixel != null;
        assert pixel.length == 4;

        byte count = (byte) ((byte) (chunk & 0b00_11_11_11) + 1); //car il y a un décalage de -1 a l'encodage.

        assert position + count <= buffer.length;

        int compteur = 0;

        for(int i = 0; i<count; ++i){
            for(int j = 0; j<4; ++j){
                buffer[position+i][j] = pixel[j];
            }
            ++compteur;
        }

        return compteur -1;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * @param data (byte[]) - Data to decode
     * @param width (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height){
        assert data != null;
        assert width > 0 && height > 0; //positive c'est >0 ou >=0
        assert data.length > 1;

        byte[] prev = QOISpecification.START_PIXEL;
        byte tagRGB = QOISpecification.QOI_OP_RGB_TAG;
        byte tagRGBA = QOISpecification.QOI_OP_RGBA_TAG;
        byte tagDIFF = QOISpecification.QOI_OP_DIFF_TAG;
        byte tagLUMA = QOISpecification.QOI_OP_LUMA_TAG;
        byte tagINDEX = QOISpecification.QOI_OP_INDEX_TAG;
        byte tagRUN = QOISpecification.QOI_OP_RUN_TAG;

        byte[][] dataImage = new byte[width*height][4];
        byte[][] hashTable = new byte[64][4];

        int idx = 0;
        int position = 0;

        while(idx < data.length && position < dataImage.length) {
            int hashIndex = QOISpecification.hash(prev);
            hashTable[hashIndex] = prev;

            boolean flag = false;
            int compteur = 0;

            if(data[idx] == tagRGB){
                compteur = decodeQoiOpRGB(dataImage, data, prev[3], position, idx + 1); // on veut sauter le tag
                idx += compteur + 1;
            }else if(data[idx] == tagRGBA){
                compteur = decodeQoiOpRGBA(dataImage, data, position, idx + 1);
                idx += compteur + 1;
            } else {
                byte tag = (byte) (data[idx] & 0b11_00_00_00);
                if(tag == tagINDEX){
                    byte index = (byte) (data[idx] & 0b00_11_11_11);
                    dataImage[position] = hashTable[index];
                    ++idx;
                }else if(tag == tagDIFF){
                    dataImage[position] = decodeQoiOpDiff(prev, data[idx]);
                    ++idx;
                } else if(tag == tagLUMA){
                    byte[] dataLuma = ArrayUtils.extract(data, idx, 2);
                    dataImage[position] = decodeQoiOpLuma(prev, dataLuma);
                    idx += 2;
                }else {
                    byte count = (byte) (data[idx] & 0b00_11_11_11);
                    compteur = decodeQoiOpRun(dataImage, prev, count, position);
                    ++idx;
                    flag = true;

                    prev = dataImage[position];
                    position += compteur + 1;
                }
            }


            if(!flag) {
                prev = dataImage[position];
                ++position;
            }
        }

        return dataImage;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content){
        assert content != null;
        byte[] header = ArrayUtils.extract(content, 0, 14);
        byte signature[] = ArrayUtils.extract(content, content.length - 8, 8);
        assert ArrayUtils.equals(signature, QOISpecification.QOI_EOF);


        int[] decodedHeader = decodeHeader(header);
        int width = decodedHeader[0];
        int height = decodedHeader[1];

        byte channels = (byte)(decodedHeader[2]);
        byte colorSpace = (byte)(decodedHeader[3]);

        byte[][] dataImage = decodeData(ArrayUtils.extract(content, 14, content.length - 21), width, height);
        int[][] image = ArrayUtils.channelsToImage(dataImage, height, width);

        return Helper.generateImage(image, channels, colorSpace);
    }

}