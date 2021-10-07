package example.encoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SteganographyEncoder {
    private final BufferedImage bi;//bi指的是将要塞进文件的图片
    private int bitsFromColor; //bits越大，一个像素点能塞进的信息越多，最多可塞24个，不过可能改变图片了
    private int mask;

    public SteganographyEncoder(BufferedImage bufferedImage) {
        this(bufferedImage, 2);
    }

    public SteganographyEncoder(BufferedImage bufferedImage, int bitsFromColor) {
        setBitsFromColor(bitsFromColor);
        this.bi = bufferedImage;
    }

    /**
     * 这个方法对字符串进行编码，编码结果存放在bytes中，前几位存放字符串长度，后几位存放字符串内容
     * 目前还没看到有方法调用这个方法
     * @param message
     */
    public BufferedImage encodeString(String message) throws IllegalArgumentException {
        if (message == null || message.length() == 0) {
            throw new IllegalArgumentException("Message can not be empty!");
        }
        char[] characters = message.toCharArray();
        byte[] messageLen = intToByteArray(message.length());
        byte[] bytes = new byte[4 + characters.length];
        for (int i = 0; i < 4; i++) {
            bytes[i] = messageLen[i];
        }
        for (int i = 0; i < characters.length; i++) {
            bytes[i + 4] = (byte) characters[i];
        }

        return encode(bytes);
    }

    /**
     *  这里是对字节数组进行解码操作
     *  数组结构 名字长度（4） 文件长度（4）名字 文件内容
     *  目标为取出文件内容
     * @return
     * @throws DecodingException
     */
    public byte[] decodeByteArray() throws DecodingException {
        byte[] bytes = decode();
        int nameSize = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
        if (nameSize <= 0 || nameSize > (bytes.length - 8)) {
            throw new DecodingException("NameSize", nameSize);
        }
        int fileSize = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
        if (fileSize < 0 || fileSize > (bytes.length - 8)) {
            throw new DecodingException("DecodedFileSize", fileSize);
        }
        if (nameSize + fileSize > (bytes.length - 8)) {
            throw new DecodingException("NameSize and DecodedFileSize", nameSize + fileSize);
        }
        return Arrays.copyOfRange(bytes, 8 + nameSize, 8 + nameSize + fileSize);
    }
    //这个方法找decode数组的
    //不对啊，4-8是filesize，取它干嘛，存疑
    public String decodeString() {
        StringBuilder sb = new StringBuilder();
        byte[] decodedByteArray = decode();

        int messageLen = byteArrayToInt(Arrays.copyOfRange(decodedByteArray, 0, 4));

        for (int i = 0; i < messageLen; i++) {
            char character = (char) decodedByteArray[i + 4];//这里应该是i+8吧
            sb.append(character);
        }

        return sb.toString();
    }
    //这个是对文件进行编码，编码为规范的byte文件
    public BufferedImage encodeFile(File file) throws IOException {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
        byte[] sizeBytes = intToByteArray(bytes.length);

        char[] nameChars = file.getName().toCharArray();
        byte[] nameBytes = new byte[nameChars.length];
        for (int i = 0; i < nameChars.length; i++) {
            nameBytes[i] = (byte) nameChars[i];
        }
        byte[] sizeNameBytes = intToByteArray(nameBytes.length);

        byte[] finalBytes = new byte[4 + 4 + nameBytes.length + bytes.length];
        System.arraycopy(sizeNameBytes, 0, finalBytes, 0, 4);
        System.arraycopy(sizeBytes, 0, finalBytes, 4, 4);
        System.arraycopy(nameBytes, 0, finalBytes, 8, nameBytes.length);
        System.arraycopy(bytes, 0, finalBytes, 8 + nameBytes.length, bytes.length);

        return encode(finalBytes);
    }
    //这个是取出编码文件并解码存到另一个文件中
    public File decodeFile(String resultPath) throws DecodingException {
        byte[] bytes = decode();
        int nameSize = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
        if (nameSize <= 0 || nameSize > (bytes.length - 8)) {
            throw new DecodingException("NameSize", nameSize);
        }
        int fileSize = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
        if (fileSize < 0 || fileSize > (bytes.length - 8)) {
            throw new DecodingException("DecodedFileSize", fileSize);
        }
        if (nameSize + fileSize > (bytes.length - 8)) {
            throw new DecodingException("NameSize and DecodedFileSize", nameSize + fileSize);
        }
        byte[] nameBytes = Arrays.copyOfRange(bytes, 8, 8 + nameSize);
        byte[] fileBytes = Arrays.copyOfRange(bytes, 8 + nameSize, 8 + nameSize + fileSize);

        StringBuilder sb = new StringBuilder();
        for (byte nameByte : nameBytes) {
            sb.append((char) nameByte);
        }
        String name = sb.toString();
        File file = new File(resultPath + "decoded_" + name);
        try {
            FileUtils.writeByteArrayToFile(file, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public int getBitsFromColor() {
        return bitsFromColor;
    }

    //现在bitsFromColor和mask的用处未知
    public void setBitsFromColor(int bitsFromColor) {
        checkBitsFromColor(bitsFromColor);
        this.bitsFromColor = bitsFromColor;
        mask = calculateMask(bitsFromColor);
    }
    //感觉这个方法应该是判断最大能容纳的文件尺寸，一个像素点可以存6个bit的信息
    //执行3次像素点加1，可能是为了防止改变图片颜色，所以只修改较低的十几位
    //一个像素藏3个bitFromColor最后在转换为byte
    public int getMaxNoOfBytes() {
        int nrOfPixels = this.bi.getWidth() * this.bi.getHeight();
        return Math.floorDiv(nrOfPixels * bitsFromColor * 3, 8);
    }

    private BufferedImage encode(byte[] bytes) {
        int[] pixels = this.bi.getRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), null, 0, this.bi.getWidth());
        int maxNoOfBytes = getMaxNoOfBytes();
        if (bytes.length > maxNoOfBytes) {
            throw new IllegalArgumentException("File to big, max no of bytes: " + maxNoOfBytes);
        }

        int smallMask = (int) (Math.pow(2, bitsFromColor) - 1);
        int curColor = 2;
        int curPix = 0;
        int charOffset = 0;

        pixels[0] &= mask; //mask是有特殊含义的，但目前就其计算方式无法看出其特殊含义
        //接下来的步骤便是核心隐藏算法，改变像素点的大小，轻微改变，不一定一个像素点存一个字节
        //每个像素点都和mask做与运算
        //aByte是8位，smallmask是11，那么char temp 执行的四次就保留了aByte的所有信息，
        //同时可以看出每个像素的不同bit位置存储了不同的信息，那么只需将两张图异或就可以了，答案是没有，只调用了后一张图
        //做逆运算
        for (byte aByte : bytes) {
            while (charOffset < 8) {
                if (curColor < 0) {
                    curColor = 2;
                    curPix++;
                    pixels[curPix] &= mask;
                }

                char temp = (char) ((aByte >> 8 - bitsFromColor - charOffset) & smallMask);
                pixels[curPix] |= (temp << curColor * 8);

                charOffset += bitsFromColor;
                curColor--;
            }
            charOffset %= 8;
        }

        BufferedImage bufferedImage = new BufferedImage(this.bi.getWidth(), this.bi.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), pixels, 0, this.bi.getWidth());
        return bufferedImage;
    }

    //这个decode可以简单理解为encode的逆运算
    private byte[] decode() {
        int[] pixels = this.bi.getRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), null, 0, this.bi.getWidth());
        int maxNoOfBytes = getMaxNoOfBytes();
        byte[] result = new byte[maxNoOfBytes];
        int smallMask = (int) (Math.pow(2, bitsFromColor) - 1);
        int curColor = 2;
        int curPix = 0;
        int charOffset = 0;

        // TODO: Optimize this code to decode only needed number of bytes and not the
        // whole byte array
        //一个简单的想法是验证这个像素点有没有与mask进行过与运算
        //不对啊，这个想法有问题，你怎么区分进没进行过与运算，因为进行过与运算的位置都被修改了，
        //有一个不太精确的想法，存进去的一定是char，那么取出来的不是就可以退出了，但感觉还是稍微有点不精准
        //也不对啊，你字节最大也就127，和char的范围也没差多少
        //可以考虑负数
        for (int i = 0; i < maxNoOfBytes; i++) {
            byte oneByte = 0;
            while (charOffset < 8) {
                if (curColor < 0) {
                    curColor = 2;
                    curPix++;
                }
                char temp = (char) (pixels[curPix] >> (8 * curColor) & smallMask);//一次取两位
                oneByte |= temp << 8 - bitsFromColor - charOffset;//对取出的每一位左移到其应该在的位置放到byte上

                charOffset += bitsFromColor;
                curColor--;
            }
//            if(oneByte < 0) break;
            result[i] = oneByte;
            charOffset %= 8;
        }
        return result;
    }

    private void checkBitsFromColor(int bitsFromColor) {
        if (!Arrays.asList(1, 2, 4, 8).contains(bitsFromColor)) {
            throw new IllegalArgumentException("Number of used bits from color must be in set {1,2,4,8}");
        }
    }
    //mask类似掩码，除了藏信息的位置，其余位置都被设为了1，那么与运算过后，藏信息的位都被设为了0
    //那么只要取出这些位置上的元素即可，也即取出01,89,1617上的六个元素即可
    private int calculateMask(int bitsFromColor) {
        int temp = (int) (Math.pow(2, bitsFromColor) - 1);
        int mask = 0;
        for (int i = 0; i < 3; i++) {
            mask <<= 8;
            mask |= temp;
        }
        return ~mask;
    }

    private byte[] intToByteArray(int integer) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--) {
            result[3 - i] = (byte) (integer >> (i * 8));
        }
        return result;
    }

    private int byteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) {
            return 0;
        }
        int result = 0;
        int littleMask = 255;
        for (byte aByte : bytes) {
            int intFromByte = littleMask & aByte;
            result <<= 8;
            result |= intFromByte;
        }

        return result;
    }
}
