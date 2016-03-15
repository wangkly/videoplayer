package com.videoplayer.wangkly.myvideoplayer.util;
public class Util{

    /**
     * 计算百分比
     * @param y
     * @param z
     * @return
     */
    public static String myPercent(int y, int z) {
        String baifenbi = "";// 接受百分比的值
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        DecimalFormat df1 = new DecimalFormat("##.00%");
        baifenbi = df1.format(fen);
        return baifenbi;
    }
    /**
     * 计算百分比
     * @param y
     * @param z
     * @return
     */
    public static int Percent(int y, int z) {
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        int result =new Double(fen*100).intValue();
        return result ;
    }

    /**
     * 获取网络图片资源
     *
     * @param url
     * @return
     */
    public static Bitmap getHttpBitmap(String url) {
        URL myFileURL;
        Bitmap bitmap = null;
        try {
            myFileURL = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            // 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            // 连接设置获得数据流
            conn.setDoInput(true);
            // 不使用缓存
            conn.setUseCaches(false);
            // 这句可有可无，没有影响
            // conn.connect();
            // 得到数据流
            InputStream is = conn.getInputStream();
            // 解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            // 关闭数据流
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 获取本地视频缩率图
     * @param filePath
     * @param width
     * @param height
     * @param kind
     * @return
     */
    public static Bitmap getVidioBitmap(String filePath, int width, int height,
                                        int kind) {
        Bitmap bitmap = null;
        //ThumbnailUtils截取图片（原始比例）
        //Create a video thumbnail for a video
        bitmap = ThumbnailUtils.createVideoThumbnail(filePath, kind);
        //將原图片转换为指定大小；
        //Creates a centered bitmap of the desired size.
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        //放回bitmap对象；
        return bitmap;
    }

}