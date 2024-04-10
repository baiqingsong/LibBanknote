package com.dawn.banknote;



/**
 * 纸钞机操作的工具类
 */
class BanknoteSerialUtil {
    private static String currentCommand = "80";//当前发送指令，80，00循环
    /**
     * 获取纸钞机是否连接，开机首先发送
     */
    public static String getBanknoteConnect(){
        int[] data = {0x80, 0x01, 0x11};
        currentCommand = "00";
        return "7F800111" + new CrcUtil().getCrc(data);
    }

    /**
     * 设置当前通讯协议版本
     */
    public static String getProtocol(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x02, 0x06, 0x08};
        String sendStr = "7F" + currentCommand + "020608" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 获取当前通道配置
     */
    public static String getConfigure(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x05};
        String sendStr = "7F" + currentCommand + "0105" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 设置当前通道配置
     */
    public static String setConfigure(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x03, 0x02, 0xFF, 0x00};
        String sendStr = "7F" + currentCommand + "0302FF00" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 只要第一种
     */
    public static String setConfigure1(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x03, 0x02, 0x1F, 0x00};
        String sendStr = "7F" + currentCommand + "03021F00" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }
    /**
     * 只要第两种
     */
    public static String setConfigure2(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x03, 0x02, 0x3F, 0x00};
        String sendStr = "7F" + currentCommand + "03023F00" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 设置使能
     */
    public static String setEnable(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x0A};
        String sendStr = "7F" + currentCommand + "010A" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 设置禁能
     */
    public static String setDisable(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x09};
        String sendStr = "7F" + currentCommand + "0109" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 获取状态，循环查询，等待收币，压币指令
     */
    public static String getStatus(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x07};
        String sendStr = "7F" + currentCommand + "0107" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 拒收操作
     */
    public static String getReject(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x08};
        String sendStr = "7F" + currentCommand + "0108" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 暂时保存在纸钞机中，等待主机选择接收
     */
    public static String getHold(){
        int[] data = {Integer.parseInt(currentCommand, 16), 0x01, 0x18};
        String sendStr = "7F" + currentCommand + "0118" + new CrcUtil().getCrc(data);
        if("00".equals(currentCommand)){
            currentCommand = "80";
        }else{
            currentCommand = "00";
        }
        return sendStr;
    }

    /**
     * 字符串转为字节数组
     */
    public static byte[] toByteArray(String hexString) {
        if (hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toUpperCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xFF);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xFF);
            byteArray[i] = (byte) (high << 4 | low & 0xFF);
            k += 2;
        }
        return byteArray;
    }

}
