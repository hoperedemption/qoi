package cs107;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils(){}

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2){
        if(a1 == null && a2 == null) {
            return false;
        }
        assert a1 != null;
        assert a2 != null;

        if(a1.length != a2.length) {
            return false;
        }
        for(int i = 0; i < a1.length; ++i) {
            if(a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }



    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2){
        if(a1 == null && a2 == null) {
            return false;
        }
        assert a1 != null;
        assert a2 != null;

        if(a1.length != a2.length) {
            return false;
        }
        for(int i = 0; i < a1.length; ++i) {
            if(!equals(a1[i], a2[i])) {
                return false;
            }
        }
        return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value){
        byte [] a = new byte[1];
        a[0] = value;

        return a;
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes){
        assert bytes != null;
        assert bytes.length == 4;

        int value = 0;

        int firstPosition = bytes[0];
        firstPosition = ((firstPosition << 24) & 0xFF_00_00_00);


        int secondPosition = bytes[1];
        secondPosition = ((secondPosition << 16) & 0x00_FF_00_00);

        int thirdPosition = bytes[2];
        thirdPosition = ((thirdPosition << 8) & 0x00_00_FF_00);

        int fourthPosition = bytes[3] & 0x00_00_00_FF;

        value = firstPosition | secondPosition | thirdPosition | fourthPosition;

        return value;

    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value){
        byte firstPosition = (byte)((value >>> 24));
        byte secondPosition = (byte)((value >>> 16));
        byte thirdPosition = (byte)((value >>> 8));
        byte fourthPosition = (byte)(value);

        byte[] a = {firstPosition, secondPosition, thirdPosition, fourthPosition};

        return a;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes != null;

        byte[] tab = new byte[bytes.length];
        for(int i = 0; i<tab.length; ++i){
            tab[i] = bytes[i];
        }

        return tab;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[] ... tabs){
        assert tabs != null;

        int nbTabs = tabs.length;
        int totalLength = 0;
        for (byte[] tab : tabs) {
            assert tab != null;
            totalLength += tab.length;
        }

        byte[] tabFinal = new byte[totalLength];

        int indTabFinal = 0;
        for(int i = 0; i < tabs.length; ++i) {
            for(int h = 0; h < tabs[i].length; ++h){
                tabFinal[indTabFinal] = tabs[i][h];
                ++indTabFinal;
            }
        }
        return tabFinal;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     * @param input (byte[]) - Array to extract from
     * @param start (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     * start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length){
        assert input != null;
        assert start >= 0 && start < input.length;
        assert length >= 0;
        assert start + length <= input.length;

        byte[] extracted = new byte[length];
        for(int i = 0; i < length; ++i){
            extracted[i] = input[start + i];
        }

        return extracted;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     * or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int ... sizes) {
        assert input != null;
        assert sizes != null;

        int sum = 0;
        for(int size : sizes) {
            sum += size;
        }

        assert sum == input.length;

        byte[][] partitions = new byte[sizes.length][];

        int j = 0;
        for(int i = 0; i < sizes.length; ++i) {
            partitions[i] = new byte[sizes[i]];
            for(int k = 0; k < partitions[i].length; ++k) {
                partitions[i][k] = input[j];
                ++j;
            }
        }

        return partitions;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input){
        assert input != null;

        int m = input.length;
        int n = input[0].length;

        for(int[] line : input) {
            assert line != null;
            assert line.length == n;
        }

        byte[][] output = new byte[n*m][4];

        int k = 0;
        for(int i = 0; i < m; ++i) {
            for(int j = 0; j < n; ++j) {
                byte[] temp = fromInt(input[i][j]);
                for(int v = 0; v < 3; ++v) {
                    output[k][v] = temp[v + 1];
                }
                output[k][3] = temp[0];
                ++k;
            }
        }

        return output;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     * @param input (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     * or input's length differs from width * height
     * or height is invalid
     * or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width){
        assert input != null;
        assert input.length == width*height;

        byte[][] copy = new byte[input.length][input[0].length];
        for(int i = 0; i < input.length; ++i) {
            assert  input[i] != null;
            assert input[i].length == 4;

            for(int j = 0; j < input[i].length; ++j)
            {
                copy[i][j] = input[i][j];
            }
        }

        int[][] output = new int[height][width];

        int colonne = 0;
        int ligne = 0;

        for(int i = 0; i<height*width; ++i){

            byte tmp = input[i][3];

            for(int v =3; v>0; --v){
                copy[i][v] = copy[i][v-1];
            }
            copy[i][0] = tmp;

            int val = toInt(copy[i]);
            output[ligne][colonne] = val;

            ++colonne;

            if(colonne % width == 0){
                ++ligne;
                colonne = 0;
            }
        }

        return output;

    }

}